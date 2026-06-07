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
data class GuideDto(
    val id: String,
    val user_id: String,
    val name: String?,
    val email: String?,
    val photo: String?,
    val description: String?,
    val languages: String?,
    val city_id: String?,
    val city_name: String?,
    val phone: String?,
    val whatsapp: String?,
    val price_per_day: String,
    val experience_years: Int?,
    val specialties: String?,
    val rating: String?,
    val total_reviews: Int?,
    val status: String,
    val created_at: String?
)

@JsonClass(generateAdapter = true)
data class GuideBookingDto(
    val id: String,
    val traveler_id: String,
    val guide_id: String,
    val date: String,
    val total_price: String,
    val payment_status: String,
    val status: String,
    val guide_name: String?,
    val traveler_name: String?,
    val traveler_email: String?,
    val created_at: String?
)

@JsonClass(generateAdapter = true)
data class GuideReviewDto(
    val id: String,
    val guide_id: String,
    val user_id: String,
    val booking_id: String,
    val rating: Int,
    val comment: String,
    val created_at: String?,
    val user_name: String?
)

@JsonClass(generateAdapter = true)
data class GuidesResponseDto(
    val success: Boolean,
    val data: List<GuideDto>?
)

@JsonClass(generateAdapter = true)
data class GuideResponseDto(
    val success: Boolean,
    val data: GuideDto?
)

@JsonClass(generateAdapter = true)
data class GuideBookingsResponseDto(
    val success: Boolean,
    val data: List<GuideBookingDto>?
)

@JsonClass(generateAdapter = true)
data class GuideReviewsResponseDto(
    val success: Boolean,
    val data: List<GuideReviewDto>?
)

@JsonClass(generateAdapter = true)
data class ReviewDto(
    val id: String,
    val property_id: String,
    val user_id: String,
    val reservation_id: String,
    val rating: Int,
    val comment: String,
    val is_hidden: Int = 0,
    val is_reported: Int = 0,
    val created_at: String? = null,
    val user_name: String? = null
)

@JsonClass(generateAdapter = true)
data class ReviewsResponseDto(
    val success: Boolean,
    val data: List<ReviewDto>
)

@JsonClass(generateAdapter = true)
data class CanReviewResponseDto(
    val success: Boolean,
    val can_review: Boolean,
    val reservation_id: String? = null
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
    val images: List<String>? = emptyList(),
    val rating: Double? = 0.0,
    val total_reviews: Int? = 0
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
    val status: String,
    val payment_method: String = "credit_card"
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
    val success: Boolean = true,
    val message: String? = null,
    val token: String = "",
    val user: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    val id: String,
    val property_id: String,
    val client_id: String,
    val host_id: String,
    val property_title: String? = null,
    val client_name: String? = null,
    val host_name: String? = null,
    val created_at: String? = null
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    val id: String,
    val conversation_id: String,
    val sender_id: String,
    val message: String,
    val is_read: Int = 0,
    val created_at: String? = null
)

@JsonClass(generateAdapter = true)
data class ConversationsResponseDto(
    val success: Boolean,
    val data: List<ConversationDto>
)

@JsonClass(generateAdapter = true)
data class MessagesResponseDto(
    val success: Boolean,
    val data: List<MessageDto>
)

@JsonClass(generateAdapter = true)
data class CreateConversationResponseDto(
    val success: Boolean,
    val data: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class CreateMessageResponseDto(
    val success: Boolean,
    val data: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class DefaultResponseDto(
    val success: Boolean,
    val message: String
)
