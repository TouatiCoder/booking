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
