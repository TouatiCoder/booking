package com.example.data.dao

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AppDao(private val context: Context) {

    private val db: FirebaseFirestore?
    private val auth: FirebaseAuth?

    // Local Memory Fallback Cache for absolute robustness (offline / sandbox demo environments)
    private val _usersCache = MutableStateFlow<Map<String, UserEntity>>(emptyMap())
    private val _propertiesCache = MutableStateFlow<Map<String, PropertyEntity>>(emptyMap())
    private val _reservationsCache = MutableStateFlow<Map<String, ReservationEntity>>(emptyMap())
    private val _favoritesCache = MutableStateFlow<Map<String, FavoriteEntity>>(emptyMap())
    private val _subscriptionsCache = MutableStateFlow<Map<String, SubscriptionEntity>>(emptyMap())
    private val _notificationsCache = MutableStateFlow<Map<String, NotificationEntity>>(emptyMap())
    private val _citiesCache = MutableStateFlow<Map<String, CityEntity>>(emptyMap())

    init {
        var initializedDb: FirebaseFirestore? = null
        var initializedAuth: FirebaseAuth? = null
        try {
            // Soft check if Firebase is initialized. If not, auto-initialize with a dynamic profile
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:390954535169:android:aabc1234720")
                    .setApiKey("MockApiKeyForStaysAppToShowCaseSleekUiBeautifully")
                    .setProjectId("zellige-stays-app")
                    .setStorageBucket("zellige-stays-app.appspot.com")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            initializedDb = FirebaseFirestore.getInstance()
            initializedAuth = FirebaseAuth.getInstance()
            Log.d("AppDao", "Firebase successfully initialized for AppDao")
        } catch (e: Exception) {
            Log.e("AppDao", "Firebase initialization failed - entering highly robust fallback sandbox mode: ${e.message}")
        }
        db = initializedDb
        auth = initializedAuth
    }

    // Helper conversion utilities
    private fun Any?.toDoubleValue(): Double = when (this) {
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun Any?.toIntValue(): Int = when (this) {
        is Number -> this.toInt()
        is String -> this.toIntOrNull() ?: 0
        else -> 0
    }

    private fun Any?.toBooleanValue(default: Boolean = false): Boolean = when (this) {
        is Boolean -> this
        is String -> this.toBoolean()
        is Number -> this.toInt() != 0
        else -> default
    }

    private fun mapToUser(data: Map<String, Any?>): UserEntity = UserEntity(
        email = data["email"] as? String ?: "",
        fullName = data["fullName"] as? String ?: "",
        phone = data["phone"] as? String ?: "",
        role = data["role"] as? String ?: "guest",
        passwordHash = data["passwordHash"] as? String ?: ""
    )

    private fun mapToProperty(data: Map<String, Any?>): PropertyEntity = PropertyEntity(
        id = data["id"] as? String ?: "",
        title = data["title"] as? String ?: "",
        description = data["description"] as? String ?: "",
        city = data["city"] as? String ?: "",
        address = data["address"] as? String ?: "",
        price = data["price"].toDoubleValue(),
        rating = data["rating"].toDoubleValue(),
        propertyType = data["propertyType"] as? String ?: "",
        amenities = data["amenities"] as? String ?: "",
        imageUrls = data["imageUrls"] as? String ?: "",
        hostEmail = data["hostEmail"] as? String ?: "",
        isActive = data["isActive"].toBooleanValue(true),
        isSuggested = data["isSuggested"].toBooleanValue(false),
        latitude = data["latitude"].toDoubleValue(),
        longitude = data["longitude"].toDoubleValue()
    )

    private fun mapToReservation(data: Map<String, Any?>): ReservationEntity = ReservationEntity(
        id = data["id"] as? String ?: "",
        propertyId = data["propertyId"] as? String ?: "",
        userEmail = data["userEmail"] as? String ?: "",
        checkInDate = (data["checkInDate"] as? Number)?.toLong() ?: 0L,
        checkOutDate = (data["checkOutDate"] as? Number)?.toLong() ?: 0L,
        guests = data["guests"].toIntValue(),
        totalPrice = data["totalPrice"].toDoubleValue(),
        status = data["status"] as? String ?: "Upcoming"
    )

    private fun mapToFavorite(data: Map<String, Any?>): FavoriteEntity = FavoriteEntity(
        id = data["id"] as? String ?: "",
        userEmail = data["userEmail"] as? String ?: "",
        propertyId = data["propertyId"] as? String ?: ""
    )

    private fun mapToSubscription(data: Map<String, Any?>): SubscriptionEntity = SubscriptionEntity(
        id = data["id"] as? String ?: "",
        hostEmail = data["hostEmail"] as? String ?: "",
        startDate = (data["startDate"] as? Number)?.toLong() ?: 0L,
        isFreeTrial = data["isFreeTrial"].toBooleanValue(true),
        status = data["status"] as? String ?: "Expired"
    )

    private fun mapToNotification(data: Map<String, Any?>): NotificationEntity = NotificationEntity(
        id = data["id"] as? String ?: "",
        userEmail = data["userEmail"] as? String ?: "",
        title = data["title"] as? String ?: "",
        message = data["message"] as? String ?: "",
        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
    )

    private fun mapToCity(data: Map<String, Any?>): CityEntity = CityEntity(
        id = data["id"] as? String ?: "",
        name_ar = data["name_ar"] as? String ?: "",
        name_fr = data["name_fr"] as? String ?: "",
        name_en = data["name_en"] as? String ?: "",
        name_es = data["name_es"] as? String ?: "",
        imageUrl = data["imageUrl"] as? String ?: "",
        isActive = data["isActive"].toBooleanValue(true),
        isFeatured = data["isFeatured"].toBooleanValue(false),
        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
    )

    // Fire & Forget helper to synchronize Firestore modifications with the thread-safe local fallback cache
    private inline fun <K, V> updateCache(stateFlow: MutableStateFlow<Map<K, V>>, key: K, value: V) {
        stateFlow.update { it + (key to value) }
    }

    private inline fun <K, V> deleteFromCache(stateFlow: MutableStateFlow<Map<K, V>>, key: K) {
        stateFlow.update { it - key }
    }

    // ==========================================
    // 1. USERS COLLECTION
    // ==========================================
    suspend fun getUserByEmail(email: String): UserEntity? {
        if (db == null) return _usersCache.value[email]
        return try {
            val snapshot = db.collection("users").document(email).get().await()
            if (snapshot.exists()) {
                val user = mapToUser(snapshot.data ?: emptyMap())
                updateCache(_usersCache, email, user)
                user
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AppDao", "getUserByEmail Firestore error: ${e.message}")
            _usersCache.value[email]
        }
    }

    suspend fun insertUser(user: UserEntity) {
        updateCache(_usersCache, user.email, user)
        if (db == null) return
        try {
            db.collection("users").document(user.email).set(user).await()
            // Connect to Firebase Authentication under the hood
            auth?.let {
                try {
                    it.createUserWithEmailAndPassword(user.email, user.passwordHash).await()
                } catch (ae: Exception) {
                    // If user already exists in auth, try signing in to sync
                    try {
                        it.signInWithEmailAndPassword(user.email, user.passwordHash).await()
                    } catch (ignore: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("AppDao", "insertUser Firestore error: ${e.message}")
        }
    }

    suspend fun updateUser(user: UserEntity) {
        insertUser(user)
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> {
        if (db == null) return _usersCache.map { it.values.toList() }
        return callbackFlow {
            val listener = db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_usersCache.value.values.toList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val users = snapshot.documents.map { doc ->
                            mapToUser(doc.data ?: emptyMap())
                        }
                        // Update cache
                        _usersCache.update { current ->
                            val newMap = current.toMutableMap()
                            users.forEach { newMap[it.email] = it }
                            newMap
                        }
                        trySend(users)
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    // ==========================================
    // 2. PROPERTIES COLLECTION
    // ==========================================
    fun getAllPropertiesFlow(): Flow<List<PropertyEntity>> {
        if (db == null) return _propertiesCache.map { it.values.toList() }
        return callbackFlow {
            val listener = db.collection("properties")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_propertiesCache.value.values.toList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.map { doc ->
                            mapToProperty(doc.data ?: emptyMap())
                        }
                        _propertiesCache.update { current ->
                            val newMap = current.toMutableMap()
                            items.forEach { newMap[it.id] = it }
                            newMap
                        }
                        trySend(items)
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    fun getActivePropertiesFlow(): Flow<List<PropertyEntity>> {
        return getAllPropertiesFlow().map { list -> list.filter { it.isActive }.sortedByDescending { it.rating } }
    }

    fun getPropertiesByHostFlow(hostEmail: String): Flow<List<PropertyEntity>> {
        return getAllPropertiesFlow().map { list -> list.filter { it.hostEmail == hostEmail } }
    }

    suspend fun getPropertyById(id: String): PropertyEntity? {
        if (db == null) return _propertiesCache.value[id]
        return try {
            val snapshot = db.collection("properties").document(id).get().await()
            if (snapshot.exists()) {
                val property = mapToProperty(snapshot.data ?: emptyMap())
                updateCache(_propertiesCache, id, property)
                property
            } else {
                null
            }
        } catch (e: Exception) {
            _propertiesCache.value[id]
        }
    }

    suspend fun insertProperty(property: PropertyEntity) {
        updateCache(_propertiesCache, property.id, property)
        if (db == null) return
        try {
            db.collection("properties").document(property.id).set(property).await()
        } catch (e: Exception) {
            Log.e("AppDao", "insertProperty error: ${e.message}")
        }
    }

    suspend fun insertProperties(properties: List<PropertyEntity>) {
        properties.forEach { insertProperty(it) }
    }

    suspend fun updateProperty(property: PropertyEntity) {
        insertProperty(property)
    }

    suspend fun deleteProperty(property: PropertyEntity) {
        deleteFromCache(_propertiesCache, property.id)
        if (db == null) return
        try {
            db.collection("properties").document(property.id).delete().await()
        } catch (e: Exception) {
            Log.e("AppDao", "deleteProperty error: ${e.message}")
        }
    }

    // ==========================================
    // 3. RESERVATIONS COLLECTION
    // ==========================================
    fun getAllReservationsFlow(): Flow<List<ReservationEntity>> {
        if (db == null) return _reservationsCache.map { it.values.toList() }
        return callbackFlow {
            val listener = db.collection("reservations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_reservationsCache.value.values.toList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.map { doc ->
                            mapToReservation(doc.data ?: emptyMap())
                        }
                        _reservationsCache.update { current ->
                            val newMap = current.toMutableMap()
                            items.forEach { newMap[it.id] = it }
                            newMap
                        }
                        trySend(items)
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    fun getReservationsByUserFlow(userEmail: String): Flow<List<ReservationEntity>> {
        return getAllReservationsFlow().map { list -> list.filter { it.userEmail == userEmail }.sortedByDescending { it.checkInDate } }
    }

    fun getReservationsByPropertyFlow(propertyId: String): Flow<List<ReservationEntity>> {
        return getAllReservationsFlow().map { list -> list.filter { it.propertyId == propertyId } }
    }

    suspend fun insertReservation(reservation: ReservationEntity) {
        updateCache(_reservationsCache, reservation.id, reservation)
        if (db == null) return
        try {
            db.collection("reservations").document(reservation.id).set(reservation).await()
        } catch (e: Exception) {
            Log.e("AppDao", "insertReservation error: ${e.message}")
        }
    }

    suspend fun updateReservation(reservation: ReservationEntity) {
        insertReservation(reservation)
    }

    // ==========================================
    // 4. FAVORITES COLLECTION
    // ==========================================
    fun getFavoritesByUserFlow(userEmail: String): Flow<List<FavoriteEntity>> {
        if (db == null) return _favoritesCache.map { list -> list.values.filter { it.userEmail == userEmail } }
        return callbackFlow {
            val listener = db.collection("favorites")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_favoritesCache.value.values.filter { it.userEmail == userEmail })
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.map { doc ->
                            mapToFavorite(doc.data ?: emptyMap())
                        }
                        _favoritesCache.update { current ->
                            val newMap = current.toMutableMap()
                            items.forEach { newMap[it.id] = it }
                            newMap
                        }
                        trySend(items)
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getFavorite(userEmail: String, propertyId: String): FavoriteEntity? {
        val compositeId = "${userEmail}_${propertyId}"
        if (db == null) return _favoritesCache.value[compositeId]
        return try {
            val snapshot = db.collection("favorites").document(compositeId).get().await()
            if (snapshot.exists()) {
                val fav = mapToFavorite(snapshot.data ?: emptyMap())
                updateCache(_favoritesCache, compositeId, fav)
                fav
            } else {
                null
            }
        } catch (e: Exception) {
            _favoritesCache.value[compositeId]
        }
    }

    suspend fun insertFavorite(favorite: FavoriteEntity) {
        val compositeId = "${favorite.userEmail}_${favorite.propertyId}"
        val fixedFavorite = favorite.copy(id = compositeId)
        updateCache(_favoritesCache, compositeId, fixedFavorite)
        if (db == null) return
        try {
            db.collection("favorites").document(compositeId).set(fixedFavorite).await()
        } catch (e: Exception) {
            Log.e("AppDao", "insertFavorite error: ${e.message}")
        }
    }

    suspend fun deleteFavorite(userEmail: String, propertyId: String) {
        val compositeId = "${userEmail}_${propertyId}"
        deleteFromCache(_favoritesCache, compositeId)
        if (db == null) return
        try {
            db.collection("favorites").document(compositeId).delete().await()
        } catch (e: Exception) {
            Log.e("AppDao", "deleteFavorite error: ${e.message}")
        }
    }

    // ==========================================
    // 5. SUBSCRIPTIONS COLLECTION
    // ==========================================
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>> {
        if (db == null) return _subscriptionsCache.map { it.values.toList() }
        return callbackFlow {
            val listener = db.collection("subscriptions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_subscriptionsCache.value.values.toList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.map { doc ->
                            mapToSubscription(doc.data ?: emptyMap())
                        }
                        _subscriptionsCache.update { current ->
                            val newMap = current.toMutableMap()
                            items.forEach { newMap[it.id] = it }
                            newMap
                        }
                        trySend(items)
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSubscriptionsByHost(hostEmail: String): List<SubscriptionEntity> {
        if (db == null) return _subscriptionsCache.value.values.filter { it.hostEmail == hostEmail }.sortedByDescending { it.startDate }
        return try {
            val snapshot = db.collection("subscriptions")
                .whereEqualTo("hostEmail", hostEmail)
                .get().await()
            val list = snapshot.documents.map { doc ->
                mapToSubscription(doc.data ?: emptyMap())
            }
            _subscriptionsCache.update { current ->
                val newMap = current.toMutableMap()
                list.forEach { newMap[it.id] = it }
                newMap
            }
            list.sortedByDescending { it.startDate }
        } catch (e: Exception) {
            _subscriptionsCache.value.values.filter { it.hostEmail == hostEmail }.sortedByDescending { it.startDate }
        }
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity) {
        // Create an ID if empty
        val subId = subscription.id.ifEmpty() { "sub_${subscription.hostEmail}" }
        val fixedSub = subscription.copy(id = subId)
        updateCache(_subscriptionsCache, subId, fixedSub)
        if (db == null) return
        try {
            db.collection("subscriptions").document(subId).set(fixedSub).await()
        } catch (e: Exception) {
            Log.e("AppDao", "insertSubscription error: ${e.message}")
        }
    }

    // ==========================================
    // 6. NOTIFICATIONS COLLECTION
    // ==========================================
    fun getNotificationsByUserFlow(userEmail: String): Flow<List<NotificationEntity>> {
        if (db == null) return _notificationsCache.map { list -> list.values.filter { it.userEmail == userEmail }.sortedByDescending { it.timestamp } }
        return callbackFlow {
            val listener = db.collection("notifications")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_notificationsCache.value.values.filter { it.userEmail == userEmail })
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.map { doc ->
                            mapToNotification(doc.data ?: emptyMap())
                        }
                        _notificationsCache.update { current ->
                            val newMap = current.toMutableMap()
                            items.forEach { newMap[it.id] = it }
                            newMap
                        }
                        trySend(items.sortedByDescending { it.timestamp })
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun insertNotification(notification: NotificationEntity) {
        updateCache(_notificationsCache, notification.id, notification)
        if (db == null) return
        try {
            db.collection("notifications").document(notification.id).set(notification).await()
        } catch (e: Exception) {
            Log.e("AppDao", "insertNotification error: ${e.message}")
        }
    }

    // ==========================================
    // 7. CITIES COLLECTION
    // ==========================================
    fun getAllCitiesFlow(): Flow<List<CityEntity>> {
        if (db == null) return _citiesCache.map { it.values.toList() }
        return callbackFlow {
            val listener = db.collection("cities")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(_citiesCache.value.values.toList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.map { doc ->
                            mapToCity(doc.data ?: emptyMap())
                        }
                        _citiesCache.update { current ->
                            val newMap = current.toMutableMap()
                            items.forEach { newMap[it.id] = it }
                            newMap
                        }
                        trySend(items)
                    }
                }
            awaitClose { listener.remove() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun insertCity(city: CityEntity) {
        updateCache(_citiesCache, city.id, city)
        if (db == null) return
        try {
            db.collection("cities").document(city.id).set(city).await()
        } catch (e: Exception) {
            Log.e("AppDao", "insertCity error: ${e.message}")
        }
    }

    suspend fun updateCity(city: CityEntity) {
        insertCity(city)
    }

    suspend fun deleteCity(cityId: String) {
        deleteFromCache(_citiesCache, cityId)
        if (db == null) return
        try {
            db.collection("cities").document(cityId).delete().await()
        } catch (e: Exception) {
            Log.e("AppDao", "deleteCity error: ${e.message}")
        }
    }

    suspend fun insertCities(cities: List<CityEntity>) {
        cities.forEach { city ->
            updateCache(_citiesCache, city.id, city)
            if (db == null) return@forEach
            try {
                db.collection("cities").document(city.id).set(city).await()
            } catch (e: Exception) {
                Log.e("AppDao", "insertCities error: ${e.message}")
            }
        }
    }
}
