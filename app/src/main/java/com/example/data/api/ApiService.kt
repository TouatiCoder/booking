package com.example.data.api

import com.example.data.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login.php")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponseDto>

    @POST("auth/register.php")
    suspend fun register(@Body request: Map<String, String>): Response<AuthResponseDto>

    @POST("delete_account.php")
    suspend fun deleteAccount(@Body request: Map<String, String>): Response<DefaultResponseDto>

    @GET("conversations.php")
    suspend fun getConversations(@Query("user_id") userId: String): Response<ConversationsResponseDto>

    @POST("conversations.php")
    suspend fun createConversation(@Body request: Map<String, String>): Response<CreateConversationResponseDto>

    @GET("messages.php")
    suspend fun getMessages(@Query("conversation_id") conversationId: String): Response<MessagesResponseDto>

    @POST("messages.php")
    suspend fun sendMessage(@Body request: Map<String, String>): Response<CreateMessageResponseDto>

    @GET("reviews.php?action=can_review")
    suspend fun canReview(@Query("user_id") userId: String, @Query("property_id") propertyId: String): Response<CanReviewResponseDto>

    @GET("reviews.php")
    suspend fun getReviews(@Query("property_id") propertyId: String): Response<ReviewsResponseDto>

    @POST("reviews.php")
    suspend fun submitReview(@Body request: Map<String, String>): Response<DefaultResponseDto>

    @GET("settings.php")
    suspend fun getSettings(): Response<Map<String, String>>

    @GET("guides.php")
    suspend fun getGuides(@Query("city_id") cityId: String? = null): Response<GuidesResponseDto>

    @GET("guides.php")
    suspend fun getGuideById(@Query("id") id: String): Response<GuideResponseDto>

    @GET("guides.php?action=my_profile")
    suspend fun getMyGuideProfile(@Query("user_id") userId: String): Response<GuideResponseDto>

    @POST("guides.php")
    suspend fun createGuideProfile(@Body request: Map<String, String>): Response<DefaultResponseDto>

    @GET("guide_bookings.php")
    suspend fun getTravelerGuideBookings(@Query("traveler_id") travelerId: String): Response<GuideBookingsResponseDto>

    @GET("guide_bookings.php")
    suspend fun getGuideBookings(@Query("guide_id") guideId: String): Response<GuideBookingsResponseDto>

    @POST("guide_bookings.php")
    suspend fun bookGuide(@Body request: Map<String, String>): Response<DefaultResponseDto>

    @GET("guide_reviews.php?action=can_review")
    suspend fun canReviewGuide(@Query("user_id") userId: String, @Query("guide_id") guideId: String): Response<CanReviewResponseDto>

    @GET("guide_reviews.php")
    suspend fun getGuideReviews(@Query("guide_id") guideId: String): Response<GuideReviewsResponseDto>

    @POST("guide_reviews.php")
    suspend fun submitGuideReview(@Body request: Map<String, String>): Response<DefaultResponseDto>

    @GET("cities.php")
    suspend fun getCities(): Response<List<CityDto>>

    @GET("categories.php")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("properties.php")
    suspend fun getProperties(): Response<List<PropertyDto>>

    @GET("property.php")
    suspend fun getProperty(@Query("id") id: String): Response<PropertyDto>

    @POST("host/add_property.php")
    suspend fun addProperty(@Body property: PropertyDto): Response<DefaultResponseDto>

    @POST("host/update_property.php")
    suspend fun updateProperty(@Body property: PropertyDto): Response<DefaultResponseDto>

    @POST("host/delete_property.php")
    suspend fun deleteProperty(@Query("id") id: String): Response<DefaultResponseDto>

    @POST("reserve.php")
    suspend fun addReservation(@Body reservation: ReservationDto): Response<DefaultResponseDto>

    @GET("my_reservations.php")
    suspend fun getMyReservations(): Response<List<ReservationDto>>

    @POST("toggle_favorite.php")
    suspend fun toggleFavorite(@Body request: Map<String, String>): Response<DefaultResponseDto>

    @GET("favorites.php")
    suspend fun getFavorites(): Response<List<PropertyDto>>

    @GET("notifications.php")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @Multipart
    @POST("host/upload_image.php")
    suspend fun uploadImage(@Part file: MultipartBody.Part, @Part("property_id") propertyId: RequestBody): Response<DefaultResponseDto>
}
