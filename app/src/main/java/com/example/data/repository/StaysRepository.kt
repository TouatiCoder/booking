package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.api.ApiClient
import com.example.data.api.ApiService
import com.example.data.dto.*
import com.example.data.local.AppDatabase
import com.example.data.local.LocalDao
import com.example.data.model.*
import com.example.data.preferences.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class StaysRepository private constructor(private val context: Context) {

    private val localDao = AppDatabase.getDatabase(context).localDao()
    private val tokenManager = TokenManager(context)
    private val apiService = ApiClient.create(tokenManager)

    // Authentication States
    private val _currentUserState = MutableStateFlow<UserEntity?>(null)
    val currentUserState: StateFlow<UserEntity?> = _currentUserState.asStateFlow()

    // Localization States - Supports "en", "ar", "fr", "es"
    private val sharedPrefs = context.getSharedPreferences("zellige_stays_prefs", Context.MODE_PRIVATE)

    private val _currentLanguageState = MutableStateFlow(sharedPrefs.getString("selected_lang", "") ?: "")
    val currentLanguageState: StateFlow<String> = _currentLanguageState.asStateFlow()

    private val _isFirstLaunchState = MutableStateFlow(sharedPrefs.getBoolean("is_first_launch", true))
    val isFirstLaunchState: StateFlow<Boolean> = _isFirstLaunchState.asStateFlow()

    private val _brandingState = MutableStateFlow<Map<String, String>>(emptyMap())
    val brandingState: StateFlow<Map<String, String>> = _brandingState.asStateFlow()

    init {
        // Load initial data and cached user on startup
        CoroutineScope(Dispatchers.IO).launch {
            tokenManager.getToken()?.let {
                // E.g., re-validate token or parse user info. For now, keep simple if offline
                // Assuming we stored user email in preferences or rely on local db logic if needed
                // Real app should have /me endpoint. We will just load properties silently.
            }
            refreshCities()
            refreshProperties()
            refreshBranding()
        }
    }

    suspend fun refreshBranding() {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSettings()
                if (response.isSuccessful && response.body() != null) {
                    _brandingState.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveLanguage(langCode: String) {
        _currentLanguageState.value = langCode
        sharedPrefs.edit().putString("selected_lang", langCode).apply()
    }

    suspend fun completeFirstLaunch() {
        _isFirstLaunchState.value = false
        sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
    }

    // Admin and Subscriptions flows
    fun getAllUsers(): Flow<List<UserEntity>> = localDao.getAllUsersFlow()
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>> = localDao.getAllSubscriptionsFlow()

    suspend fun insertSubscription(subscription: SubscriptionEntity) {
        withContext(Dispatchers.IO) {
            localDao.saveSubscription(subscription)
        }
    }
    suspend fun registerUser(fullName: String, phone: String, email: String, passwordHash: String, role: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf(
                    "name" to fullName,
                    "email" to email,
                    "password" to passwordHash,
                    "role" to role,
                    "phone" to phone
                )
                val response = apiService.register(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    val authDto = response.body()!!
                    tokenManager.saveToken(authDto.token)
                    authDto.user?.let { userDto ->
                        val userEntity = mapUserDto(userDto).copy(phone = phone)
                        localDao.saveUser(userEntity)
                        _currentUserState.value = userEntity
                    }
                    true
                } else {
                    val errorMsg = response.body()?.message ?: "Unknown error"
                    android.util.Log.e("StaysRepository", "Registration failed: $errorMsg")
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun loginUser(email: String, passwordHash: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf("email" to email, "password" to passwordHash)
                val response = apiService.login(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    val authDto = response.body()!!
                    tokenManager.saveToken(authDto.token)
                    authDto.user?.let { userDto ->
                        val userEntity = mapUserDto(userDto)
                        localDao.saveUser(userEntity)
                        _currentUserState.value = userEntity
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to local DB if available
                val localUser = localDao.getUser(email)
                if (localUser != null && localUser.passwordHash == passwordHash) {
                    _currentUserState.value = localUser
                    true
                } else {
                    false
                }
            }
        }
    }

    fun logout() {
        tokenManager.clearToken()
        _currentUserState.value = null
    }

    suspend fun deleteAccount(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val user = _currentUserState.value ?: return@withContext false
                val req = mapOf("email" to user.email)
                val response = apiService.deleteAccount(req)
                if (response.isSuccessful) {
                    logout()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun promoteToHost(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = localDao.getUser(email)
            if (user != null) {
                val updated = user.copy(role = "host")
                localDao.saveUser(updated)
                if (_currentUserState.value?.email == email) {
                    _currentUserState.value = updated
                }
                val subscription = SubscriptionEntity(
                    id = "sub_${email}",
                    hostEmail = email,
                    startDate = System.currentTimeMillis(),
                    isFreeTrial = true,
                    status = "Active"
                )
                localDao.saveSubscription(subscription)
                true
            } else {
                false
            }
        }
    }

    // Properties
    fun getActiveProperties(): Flow<List<PropertyEntity>> = localDao.getActivePropertiesFlow()
    fun getAllProperties(): Flow<List<PropertyEntity>> = localDao.getAllPropertiesFlow()
    fun getPropertiesByHost(hostEmail: String): Flow<List<PropertyEntity>> = localDao.getPropertiesByHostFlow(hostEmail)

    private suspend fun refreshProperties() {
        try {
            val response = apiService.getProperties()
            if (response.isSuccessful) {
                response.body()?.let { dtoList ->
                    val entities = dtoList.map { mapPropertyDto(it) }
                    localDao.saveProperties(entities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPropertyById(id: String): PropertyEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProperty(id)
                if (response.isSuccessful) {
                    response.body()?.let { dto ->
                        val entity = mapPropertyDto(dto)
                        localDao.saveProperty(entity)
                        entity
                    }
                } else {
                    localDao.getPropertyById(id)
                }
            } catch (e: Exception) {
                localDao.getPropertyById(id)
            }
        }
    }

    suspend fun addProperty(
        title: String,
        description: String,
        city: String,
        address: String,
        price: Double,
        propertyType: String,
        amenities: String,
        imageUrls: String,
        hostEmail: String,
        latitude: Double = 31.6295,
        longitude: Double = -7.9811
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val propertyDto = PropertyDto(
                    id = "prop_" + System.currentTimeMillis(),
                    host_id = hostEmail,
                    title = title,
                    description = description,
                    price_per_night = price,
                    city_id = city,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                    property_type = propertyType,
                    bedrooms = 1,
                    bathrooms = 1,
                    max_guests = 2,
                    status = "pending",
                    images = imageUrls.split(",")
                )
                
                val response = apiService.addProperty(propertyDto)
                if (response.isSuccessful) {
                    val localProperty = mapPropertyDto(propertyDto).copy(amenities = amenities, isActive = false)
                    localDao.saveProperty(localProperty)
                    true
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                // Save locally for offline sync later if needed
                val fallbackEntity = PropertyEntity(
                    id = "prop_" + System.currentTimeMillis(),
                    title = title,
                    description = description,
                    city = city,
                    address = address,
                    price = price,
                    propertyType = propertyType,
                    amenities = amenities,
                    imageUrls = imageUrls,
                    hostEmail = hostEmail,
                    isActive = false, // Must be approved by backend
                    latitude = latitude,
                    longitude = longitude
                )
                localDao.saveProperty(fallbackEntity)
                true
            }
        }
    }

    suspend fun uploadImage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val propertyIdBody = "temp_id".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadImage(body, propertyIdBody)
                // Returning a mock path as PHP API actually processes it
                "http://10.0.2.2/api/uploads/properties/${file.name}"
            } catch (e: Exception) {
                android.util.Log.e("StaysRepository", "Upload failed: ${e.message}")
                "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80"
            }
        }
    }

    suspend fun updateProperty(property: PropertyEntity) {
        withContext(Dispatchers.IO) {
            localDao.saveProperty(property)
            try {
                val dto = PropertyDto(
                    id = property.id,
                    host_id = property.hostEmail,
                    title = property.title,
                    description = property.description,
                    price_per_night = property.price,
                    city_id = property.city,
                    address = property.address,
                    latitude = property.latitude,
                    longitude = property.longitude,
                    property_type = property.propertyType,
                    bedrooms = 1, bathrooms = 1, max_guests = 2,
                    status = if(property.isActive) "approved" else "pending",
                    images = listOf(property.imageUrls)
                )
                apiService.updateProperty(dto)
            } catch (ignore: Exception) {}
        }
    }

    suspend fun deleteProperty(property: PropertyEntity) {
        withContext(Dispatchers.IO) {
            localDao.deleteProperty(property)
            try {
                apiService.deleteProperty(property.id)
            } catch (ignore: Exception) {}
        }
    }

    // Favorites
    fun getFavorites(userEmail: String): Flow<List<PropertyEntity>> {
        return localDao.getFavoritesByUserFlow(userEmail).flatMapLatest { favs ->
            localDao.getAllPropertiesFlow().map { properties ->
                val favIds = favs.map { it.propertyId }.toSet()
                properties.filter { it.id in favIds }
            }
        }
    }

    suspend fun isFavorite(userEmail: String, propertyId: String): Boolean {
        return withContext(Dispatchers.IO) {
            localDao.getFavorite(userEmail, propertyId) != null
        }
    }

    suspend fun toggleFavorite(userEmail: String, propertyId: String) {
        withContext(Dispatchers.IO) {
            val existing = localDao.getFavorite(userEmail, propertyId)
            if (existing != null) {
                localDao.deleteFavorite(userEmail, propertyId)
            } else {
                localDao.saveFavorite(FavoriteEntity(id = "${userEmail}_${propertyId}", userEmail = userEmail, propertyId = propertyId))
            }
            try {
                apiService.toggleFavorite(mapOf("property_id" to propertyId))
            } catch (ignore: Exception) {}
        }
    }

    suspend fun getConversations(userId: String): List<ConversationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getConversations(userId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getMessages(conversationId: String): List<MessageDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getMessages(conversationId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun createConversation(propertyId: String, clientId: String, hostId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf("property_id" to propertyId, "client_id" to clientId, "host_id" to hostId)
                val res = apiService.createConversation(req)
                if (res.isSuccessful) res.body()?.data?.get("conversation_id") else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun sendMessage(conversationId: String, senderId: String, message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf("conversation_id" to conversationId, "sender_id" to senderId, "message" to message)
                val res = apiService.sendMessage(req)
                res.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getReviews(propertyId: String): List<ReviewDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getReviews(propertyId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun canReview(userId: String, propertyId: String): CanReviewResponseDto? {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.canReview(userId, propertyId)
                if (res.isSuccessful) res.body() else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun submitReview(propertyId: String, userId: String, reservationId: String, rating: Int, comment: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf(
                    "property_id" to propertyId,
                    "user_id" to userId,
                    "reservation_id" to reservationId,
                    "rating" to rating.toString(),
                    "comment" to comment
                )
                val res = apiService.submitReview(req)
                res.isSuccessful && res.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }

    // Guides API
    suspend fun getGuides(cityId: String? = null): List<GuideDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getGuides(cityId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getGuideById(id: String): GuideDto? {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getGuideById(id)
                if (res.isSuccessful) res.body()?.data else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getMyGuideProfile(userId: String): GuideDto? {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getMyGuideProfile(userId)
                if (res.isSuccessful) res.body()?.data else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun createGuideProfile(userId: String, pricePerDay: String, description: String, languages: String, cityId: String?, phone: String, specialties: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf(
                    "action" to "create_profile",
                    "user_id" to userId,
                    "price_per_day" to pricePerDay,
                    "description" to description,
                    "languages" to languages,
                    "city_id" to (cityId ?: ""),
                    "phone" to phone,
                    "specialties" to specialties
                )
                val res = apiService.createGuideProfile(req)
                res.isSuccessful && res.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun bookGuide(travelerId: String, guideId: String, date: String, totalPrice: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf(
                    "traveler_id" to travelerId,
                    "guide_id" to guideId,
                    "date" to date,
                    "total_price" to totalPrice
                )
                val res = apiService.bookGuide(req)
                res.isSuccessful && res.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getTravelerGuideBookings(travelerId: String): List<GuideBookingDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getTravelerGuideBookings(travelerId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getGuideBookings(guideId: String): List<GuideBookingDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getGuideBookings(guideId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getGuideReviews(guideId: String): List<GuideReviewDto> {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.getGuideReviews(guideId)
                if (res.isSuccessful) res.body()?.data ?: emptyList() else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun canReviewGuide(userId: String, guideId: String): CanReviewResponseDto? {
        return withContext(Dispatchers.IO) {
            try {
                val res = apiService.canReviewGuide(userId, guideId)
                if (res.isSuccessful) res.body() else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun submitGuideReview(guideId: String, userId: String, bookingId: String, rating: Int, comment: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = mapOf(
                    "guide_id" to guideId,
                    "user_id" to userId,
                    "booking_id" to bookingId,
                    "rating" to rating.toString(),
                    "comment" to comment
                )
                val res = apiService.submitGuideReview(req)
                res.isSuccessful && res.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }

    // Reservations
    fun getReservationsByUser(userEmail: String): Flow<List<ReservationEntity>> = localDao.getReservationsByUserFlow(userEmail)
    fun getAllReservations(): Flow<List<ReservationEntity>> = localDao.getAllReservationsFlow()

    suspend fun createReservation(
        propertyId: String,
        userEmail: String,
        checkIn: Long,
        checkOut: Long,
        guests: Int,
        totalPrice: Double
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val id = "res_" + System.currentTimeMillis()
            val reservation = ReservationEntity(
                id = id,
                propertyId = propertyId,
                userEmail = userEmail,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                guests = guests,
                totalPrice = totalPrice,
                status = "Upcoming"
            )
            localDao.saveReservation(reservation)
            
            try {
                val dto = ReservationDto(
                    id = id,
                    property_id = propertyId,
                    traveler_id = userEmail,
                    host_id = "", // resolved backend side or fetched via relation
                    start_date = checkIn.toString(),
                    end_date = checkOut.toString(),
                    total_price = totalPrice,
                    status = "Upcoming"
                )
                apiService.addReservation(dto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            localDao.saveNotification(NotificationEntity(
                id = "notif_" + System.currentTimeMillis(),
                userEmail = userEmail,
                title = "Booking Confirmed",
                message = "Your booking for stay has been placed successfully!",
                timestamp = System.currentTimeMillis()
            ))
            true
        }
    }

    // Subscriptions
    suspend fun getHostSubscription(hostEmail: String): SubscriptionEntity? {
        return withContext(Dispatchers.IO) {
            localDao.getSubscriptionsByHost(hostEmail).firstOrNull()
        }
    }

    suspend fun activateSubscription(hostEmail: String) {
        withContext(Dispatchers.IO) {
            val subscription = SubscriptionEntity(
                id = "sub_${hostEmail}",
                hostEmail = hostEmail,
                startDate = System.currentTimeMillis(),
                isFreeTrial = false,
                status = "Active"
            )
            localDao.saveSubscription(subscription)
            
            val properties = localDao.getPropertiesByHostFlow(hostEmail).first()
            for (prop in properties) {
                localDao.saveProperty(prop.copy(isActive = true))
            }
        }
    }

    // Cities
    fun getAllCities(): Flow<List<CityEntity>> = localDao.getCitiesFlow()

    private suspend fun refreshCities() {
        try {
            val response = apiService.getCities()
            if(response.isSuccessful){
                response.body()?.let { dtoList ->
                    val entities = dtoList.map { mapCityDto(it) }
                    localDao.saveCities(entities)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun addCity(city: CityEntity) {
        withContext(Dispatchers.IO) {
            localDao.saveCity(city)
        }
    }

    suspend fun updateCity(city: CityEntity) {
        withContext(Dispatchers.IO) {
            localDao.saveCity(city)
        }
    }

    suspend fun deleteCity(cityId: String) {
        withContext(Dispatchers.IO) {
            localDao.deleteCity(cityId)
        }
    }

    // Mappers
    private fun mapUserDto(dto: UserDto) = UserEntity(
        email = dto.email,
        fullName = dto.name,
        role = dto.role
    )

    private fun mapCityDto(dto: CityDto) = CityEntity(
        id = dto.id,
        name_ar = dto.name_ar,
        name_fr = dto.name_fr,
        name_en = dto.name_en,
        name_es = dto.name_es,
        imageUrl = dto.imageUrl ?: "",
        isActive = dto.is_active == 1,
        isFeatured = dto.is_featured == 1
    )

    private fun mapPropertyDto(dto: PropertyDto) = PropertyEntity(
        id = dto.id,
        title = dto.title,
        description = dto.description ?: "",
        city = dto.city_id,
        address = dto.address,
        price = dto.price_per_night,
        propertyType = dto.property_type,
        imageUrls = dto.images?.joinToString(",") ?: "",
        hostEmail = dto.host_id,
        latitude = dto.latitude,
        longitude = dto.longitude,
        rating = dto.rating ?: 0.0,
        totalReviews = dto.total_reviews ?: 0,
        isActive = dto.status == "approved"
    )

    companion object {
        @Volatile
        private var INSTANCE: StaysRepository? = null

        fun getInstance(context: Context): StaysRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StaysRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}

