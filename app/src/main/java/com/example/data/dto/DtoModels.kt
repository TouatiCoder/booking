package com.example.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CityDto(
    val id: String,
    val name_ar: String,
    val name_fr: String,
    val name_en: String,
    val name_es: String,
    val imageUrl: String?,
    val is_active: Int,
    val is_featured: Int
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    val id: String,
    val name_ar: String,
    val name_fr: String,
    val name_en: String,
    val name_es: String,
    val iconUrl: String?
)

@JsonClass(generateAdapter = true)
data class PropertyDto(
    val id: String,
    val host_id: String,
    val title: String,
    val description: String?,
    val price_per_night: Double,
    val city_id: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val property_type: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val max_guests: Int,
    val status: String,
    val images: List<String>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ReservationDto(
    val id: String,
    val property_id: String,
    val traveler_id: String,
    val host_id: String,
    val start_date: String,
    val end_date: String,
    val total_price: Double,
    val status: String
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    val user_id: String,
    val title: String,
    val message: String,
    val is_read: Int,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class DefaultResponseDto(
    val success: Boolean,
    val message: String
)
