package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val role: String = "guest", // "guest", "client", "host", "admin"
    val passwordHash: String = "" // Simple storage for demo simulation
)

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val city: String = "",
    val address: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val propertyType: String = "", // "Riad", "Kasbah", "Villa", "Apartment", "Camp"
    val amenities: String = "", // Comma separated list, e.g. "Pool, Wi-Fi, Spa, Tour, Breakfast"
    val imageUrls: String = "", // Comma separated list of URLs
    val hostEmail: String = "",
    val isActive: Boolean = true,
    val isSuggested: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: String = "",
    val propertyId: String = "",
    val userEmail: String = "",
    val checkInDate: Long = 0L,
    val checkOutDate: Long = 0L,
    val guests: Int = 1,
    val totalPrice: Double = 0.0,
    val status: String = "Upcoming" // "Upcoming", "Completed"
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String = "", // UUID from PHP API
    val userEmail: String = "",
    val propertyId: String = ""
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String = "", // UUID from PHP API
    val hostEmail: String = "",
    val startDate: Long = 0L,
    val isFreeTrial: Boolean = true,
    val status: String = "Expired" // "Active", "Expired"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String = "",
    val userEmail: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String = "",
    val name_ar: String = "",
    val name_fr: String = "",
    val name_en: String = "",
    val name_es: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val createdAt: Long = 0L
) {
    fun getLocalizedName(lang: String): String {
        return when (lang) {
            "ar" -> name_ar
            "fr" -> name_fr
            "es" -> name_es
            else -> name_en
        }
    }
}
