package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface LocalDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    @Query("SELECT * FROM properties")
    fun getAllPropertiesFlow(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE isActive = 1")
    fun getActivePropertiesFlow(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE hostEmail = :email")
    fun getPropertiesByHostFlow(email: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: String): PropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProperties(properties: List<PropertyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProperty(property: PropertyEntity)

    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    @Query("SELECT * FROM cities")
    fun getCitiesFlow(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCities(cities: List<CityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCity(city: CityEntity)

    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCity(id: String)

    @Query("SELECT * FROM reservations WHERE userEmail = :email ORDER BY checkInDate DESC")
    fun getReservationsByUserFlow(email: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations")
    fun getAllReservationsFlow(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReservations(reservations: List<ReservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReservation(reservation: ReservationEntity)

    @Query("SELECT * FROM favorites WHERE userEmail = :email")
    fun getFavoritesByUserFlow(email: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userEmail = :email AND propertyId = :propertyId")
    suspend fun getFavorite(email: String, propertyId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavorites(favorites: List<FavoriteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userEmail = :email AND propertyId = :propertyId")
    suspend fun deleteFavorite(email: String, propertyId: String)

    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getNotificationsByUserFlow(email: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotifications(notifications: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotification(notification: NotificationEntity)

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE hostEmail = :email ORDER BY startDate DESC")
    suspend fun getSubscriptionsByHost(email: String): List<SubscriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSubscription(subscription: SubscriptionEntity)
}
