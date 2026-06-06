package com.example.data.repository

import android.content.Context
import com.example.data.dao.AppDao
import com.example.data.model.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class StaysRepository private constructor(private val context: Context) {

    val appDao = AppDao(context)

    // Authentication States
    private val _currentUserState = MutableStateFlow<UserEntity?>(null)
    val currentUserState: StateFlow<UserEntity?> = _currentUserState.asStateFlow()

    // Localization States - Supports "en", "ar", "fr", "es"
    private val sharedPrefs = context.getSharedPreferences("zellige_stays_prefs", Context.MODE_PRIVATE)
    
    private val _currentLanguageState = MutableStateFlow(sharedPrefs.getString("selected_lang", "") ?: "")
    val currentLanguageState: StateFlow<String> = _currentLanguageState.asStateFlow()

    private val _isFirstLaunchState = MutableStateFlow(sharedPrefs.getBoolean("is_first_launch", true))
    val isFirstLaunchState: StateFlow<Boolean> = _isFirstLaunchState.asStateFlow()

    init {
        // Pre-populate database with default items on startup
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
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

    // Auth Actions
    suspend fun registerUser(fullName: String, phone: String, email: String, passwordHash: String, role: String): Boolean {
        return withContext(Dispatchers.IO) {
            val existing = appDao.getUserByEmail(email)
            if (existing != null) return@withContext false
            val user = UserEntity(
                email = email,
                fullName = fullName,
                phone = phone,
                role = role,
                passwordHash = passwordHash
            )
            appDao.insertUser(user)
            _currentUserState.value = user
            true
        }
    }

    suspend fun loginUser(email: String, passwordHash: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = appDao.getUserByEmail(email)
            if (user != null && user.passwordHash == passwordHash) {
                _currentUserState.value = user
                true
            } else {
                false
            }
        }
    }

    fun logout() {
        _currentUserState.value = null
    }

    // Helper to change user role (for Host transitions)
    suspend fun promoteToHost(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = appDao.getUserByEmail(email)
            if (user != null) {
                val updated = user.copy(role = "host")
                appDao.insertUser(updated)
                if (_currentUserState.value?.email == email) {
                    _currentUserState.value = updated
                }
                
                // Add default trial subscription for host
                val subscription = SubscriptionEntity(
                    hostEmail = email,
                    startDate = System.currentTimeMillis(),
                    isFreeTrial = true,
                    status = "Active"
                )
                appDao.insertSubscription(subscription)
                true
            } else {
                false
            }
        }
    }

    // Properties
    fun getActiveProperties(): Flow<List<PropertyEntity>> = appDao.getActivePropertiesFlow()
    fun getAllProperties(): Flow<List<PropertyEntity>> = appDao.getAllPropertiesFlow()
    fun getPropertiesByHost(hostEmail: String): Flow<List<PropertyEntity>> = appDao.getPropertiesByHostFlow(hostEmail)
    
    suspend fun getPropertyById(id: String): PropertyEntity? = appDao.getPropertyById(id)

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
            val id = "property_" + System.currentTimeMillis()
            val property = PropertyEntity(
                id = id,
                title = title,
                description = description,
                city = city,
                address = address,
                price = price,
                rating = 4.5 + (0.5 * Math.random()), // Random rating between 4.5 and 5.0
                propertyType = propertyType,
                amenities = amenities,
                imageUrls = imageUrls,
                hostEmail = hostEmail,
                isActive = true,
                latitude = latitude,
                longitude = longitude
            )
            appDao.insertProperty(property)
            true
        }
    }

    suspend fun uploadImage(uri: android.net.Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val storage = FirebaseStorage.getInstance()
                val ref = storage.reference.child("property_images/${UUID.randomUUID()}")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            } catch (e: Exception) {
                android.util.Log.e("StaysRepository", "Firebase Storage Upload failed, using mock placeholder: ${e.message}")
                val placeholders = listOf(
                    "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=600&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&auto=format&fit=crop&q=80"
                )
                placeholders.random()
            }
        }
    }

    suspend fun updateProperty(property: PropertyEntity) {
        withContext(Dispatchers.IO) {
            appDao.updateProperty(property)
        }
    }

    suspend fun deleteProperty(property: PropertyEntity) {
        withContext(Dispatchers.IO) {
            appDao.deleteProperty(property)
        }
    }

    // Favorites
    fun getFavorites(userEmail: String): Flow<List<PropertyEntity>> {
        return appDao.getFavoritesByUserFlow(userEmail).flatMapLatest { favs ->
            appDao.getAllPropertiesFlow().map { properties ->
                val favIds = favs.map { it.propertyId }.toSet()
                properties.filter { it.id in favIds }
            }
        }
    }

    suspend fun isFavorite(userEmail: String, propertyId: String): Boolean {
        return withContext(Dispatchers.IO) {
            appDao.getFavorite(userEmail, propertyId) != null
        }
    }

    suspend fun toggleFavorite(userEmail: String, propertyId: String) {
        withContext(Dispatchers.IO) {
            val existing = appDao.getFavorite(userEmail, propertyId)
            if (existing != null) {
                appDao.deleteFavorite(userEmail, propertyId)
            } else {
                appDao.insertFavorite(FavoriteEntity(userEmail = userEmail, propertyId = propertyId))
            }
        }
    }

    // Reservations
    fun getReservationsByUser(userEmail: String): Flow<List<ReservationEntity>> = appDao.getReservationsByUserFlow(userEmail)
    fun getAllReservations(): Flow<List<ReservationEntity>> = appDao.getAllReservationsFlow()

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
            appDao.insertReservation(reservation)
            
            // Add notification
            appDao.insertNotification(NotificationEntity(
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
            appDao.getSubscriptionsByHost(hostEmail).firstOrNull()
        }
    }

    suspend fun activateSubscription(hostEmail: String) {
        withContext(Dispatchers.IO) {
            val subscription = SubscriptionEntity(
                hostEmail = hostEmail,
                startDate = System.currentTimeMillis(),
                isFreeTrial = false,
                status = "Active"
            )
            appDao.insertSubscription(subscription)
            
            // Re-activate host properties
            val properties = appDao.getAllPropertiesFlow().first().filter { it.hostEmail == hostEmail }
            for (prop in properties) {
                appDao.updateProperty(prop.copy(isActive = true))
            }
        }
    }

    // Cities
    fun getAllCities(): Flow<List<CityEntity>> = appDao.getAllCitiesFlow()

    suspend fun addCity(city: CityEntity) {
        withContext(Dispatchers.IO) {
            appDao.insertCity(city)
        }
    }

    suspend fun updateCity(city: CityEntity) {
        withContext(Dispatchers.IO) {
            appDao.updateCity(city)
        }
    }

    suspend fun deleteCity(cityId: String) {
        withContext(Dispatchers.IO) {
            appDao.deleteCity(cityId)
        }
    }

    // Seed Data
    private suspend fun seedDatabaseIfEmpty() {
        val count = appDao.getUserByEmail("admin@zellige.com")
        if (count == null) {
            // Seed Admin
            val adminUser = UserEntity(
                email = "admin@zellige.com",
                fullName = "Ayoub Touati",
                phone = "+212 600 000000",
                role = "admin",
                passwordHash = "admin123"
            )
            appDao.insertUser(adminUser)

            // Seed Demo Host
            val hostUser = UserEntity(
                email = "host@zellige.com",
                fullName = "Rachid El Guerrouj",
                phone = "+212 611 223344",
                role = "host",
                passwordHash = "host123"
            )
            appDao.insertUser(hostUser)

            val trialSubscription = SubscriptionEntity(
                hostEmail = "host@zellige.com",
                startDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 5), // Registered 5 days ago
                isFreeTrial = true,
                status = "Active"
            )
            appDao.insertSubscription(trialSubscription)

            // Seed Cities
            val defaultCities = listOf(
                CityEntity("marrakech", "مراكش", "Marrakech", "Marrakech", "Marrakech", "https://images.unsplash.com/photo-1597212618440-8062a4dfd60c?w=500&auto=format&fit=crop&q=80", true, true, System.currentTimeMillis()),
                CityEntity("casablanca", "الدار البيضاء", "Casablanca", "Casablanca", "Casablanca", "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?w=500&auto=format&fit=crop&q=80", true, false, System.currentTimeMillis()),
                CityEntity("rabat", "الرباط", "Rabat", "Rabat", "Rabat", "https://images.unsplash.com/photo-1559589689-577aabd1db4f?w=500&auto=format&fit=crop&q=80", true, false, System.currentTimeMillis()),
                CityEntity("agadir", "أكادير", "Agadir", "Agadir", "Agadir", "https://images.unsplash.com/photo-1583212292454-1fe6229603b7?w=500&auto=format&fit=crop&q=80", true, false, System.currentTimeMillis()),
                CityEntity("tangier", "طنجة", "Tanger", "Tangier", "Tánger", "https://images.unsplash.com/photo-1578351586550-93a5ec9cfb16?w=500&auto=format&fit=crop&q=80", true, true, System.currentTimeMillis()),
                CityEntity("chefchaouen", "شفشاون", "Chefchaouen", "Chefchaouen", "Chefchaouen", "https://images.unsplash.com/photo-1548786811-dd6e453ccae2?w=500&auto=format&fit=crop&q=80", true, true, System.currentTimeMillis())
            )
            appDao.insertCities(defaultCities)

            // Seed Accommodations
            val defaultProperties = listOf(
                PropertyEntity(
                    id = "p1",
                    title = "Riad Celestial Dar Zellij",
                    description = "A meticulously restored 17th-century luxury Moroccan mansion in the heart of Marrakech Medina. Featuring pristine hand-cut zellige mosaic art, an emerald-lit courtyard pool, custom carved cedarwood ceilings, and a high-contrast private hammam. Relax under the star-lit sky on our golden panoramic roof terrace overlooking the ancient Koutoubia mosque.",
                    city = "Marrakech",
                    address = "12 Derb El Halfaoui, Bab Doukkala",
                    price = 180.00,
                    rating = 4.95,
                    propertyType = "Riad",
                    amenities = "Pool, Spa (Hammam), Wi-Fi, Breakfast, AC, Fireplace",
                    imageUrls = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80",
                    hostEmail = "host@zellige.com",
                    isActive = true,
                    isSuggested = true,
                    latitude = 31.6295,
                    longitude = -7.9811
                ),
                PropertyEntity(
                    id = "p2",
                    title = "Royal Bab Marrakech Villa",
                    description = "An estate built on the outskirts of Rabat, inspired by the monumental Bab Marrakech gate. Built with historic sun-baked earth colors, and complete with a luxury 20-meter infinity pool, private citrus garden, professional Moroccan chef, and standard modern automation.",
                    city = "Rabat",
                    address = "Km 9 Route de l'Ourika",
                    price = 450.00,
                    rating = 4.88,
                    propertyType = "Villa",
                    amenities = "Pool, Gym, Kitchen, Wi-Fi, Chef Service, Free Parking",
                    imageUrls = "https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600&auto=format&fit=crop&q=80",
                    hostEmail = "host@zellige.com",
                    isActive = true,
                    isSuggested = true,
                    latitude = 34.0209,
                    longitude = -6.8416
                ),
                PropertyEntity(
                    id = "p3",
                    title = "Aromatic Blue Nest Riad",
                    description = "Stay in a beautiful sky-blue room surrounded by aromatic mint gardens, authentic cobalt pottery, and gorgeous architectural niches in the iconic blue streets of Chefchaouen. Includes high-speed fiber internet and a delicious traditional breakfast on the rooftop.",
                    city = "Chefchaouen",
                    address = "42 Avenue Hassan II, Old Medina",
                    price = 75.00,
                    rating = 4.91,
                    propertyType = "Riad",
                    amenities = "Wi-Fi, Breakfast, Rooftop Terrace, Heating, Pet Friendly",
                    imageUrls = "https://images.unsplash.com/photo-1548786811-dd6e453ccae2?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1564507592333-c60657eea523?w=600&auto=format&fit=crop&q=80",
                    hostEmail = "host@zellige.com",
                    isActive = true,
                    isSuggested = false,
                    latitude = 35.1688,
                    longitude = -5.2636
                ),
                PropertyEntity(
                    id = "p4",
                    title = "Saharan Luxury Golden Dunes Camp",
                    description = "Sleep in magnificent heavy canvas nomadic tents among the silent Erg Chebbi dunes. Complete with full en-suite restrooms, King size mattresses, Moroccan red rugs, camel desert trekking, and a stellar evening campfire carrying traditional Gnawa percussion.",
                    city = "Agadir", // Placed in Agadir sector for direct flight connections
                    address = "Merzouga Erg Chebbi Desert Sector",
                    price = 220.00,
                    rating = 4.98,
                    propertyType = "Camp",
                    amenities = "Desert Tour, Dinner Included, Breakfast, Firepit, Bathhouse",
                    imageUrls = "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1486916856992-e4db22c8df33?w=600&auto=format&fit=crop&q=80",
                    hostEmail = "host@zellige.com",
                    isActive = true,
                    isSuggested = true,
                    latitude = 30.4278,
                    longitude = -9.5981
                ),
                PropertyEntity(
                    id = "p5",
                    title = "Habibi Ocean View Penthouse",
                    description = "A magnificent contemporary Moroccan penthouse with direct views of the Atlantic Ocean and Gibraltar strait. Built with custom arches, fully modern ceramic kitchens, private hot tub, and modern security infrastructure.",
                    city = "Tangier",
                    address = "88 Rue de la Kasbah, Cliffside Section",
                    price = 130.00,
                    rating = 4.75,
                    propertyType = "Apartment",
                    amenities = "Ocean View, Wi-Fi, Kitchen, AC, Jacuzzi, Elevators",
                    imageUrls = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600&auto=format&fit=crop&q=80",
                    hostEmail = "host@zellige.com",
                    isActive = true,
                    isSuggested = false,
                    latitude = 35.7595,
                    longitude = -5.8340
                ),
                PropertyEntity(
                    id = "p6",
                    title = "Grand Boulevard Hassan II Suites",
                    description = "Spacious modern apartment right in the premium center of Casablanca. Convenient walking distance to the spectacular Hassan II Mosque grand plaza, luxury shopping malls, and elite business districts.",
                    city = "Casablanca",
                    address = "143 Boulevard Moulay Youssef",
                    price = 110.00,
                    rating = 4.67,
                    propertyType = "Apartment",
                    amenities = "Wi-Fi, Kitchen, AC, Parking, Elevator, Washing Machine",
                    imageUrls = "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&auto=format&fit=crop&q=80,https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=600&auto=format&fit=crop&q=80",
                    hostEmail = "host@zellige.com",
                    isActive = true,
                    isSuggested = false,
                    latitude = 33.5731,
                    longitude = -7.5898
                )
            )
            appDao.insertProperties(defaultProperties)
        }
    }

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
