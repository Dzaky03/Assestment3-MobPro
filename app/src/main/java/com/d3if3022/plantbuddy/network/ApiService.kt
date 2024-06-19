package com.d3if3022.plantbuddy.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.d3if3022.plantbuddy.model.MessageResponse
import com.d3if3022.plantbuddy.model.Tanaman
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://plant-buddy-3022.vercel.app/"


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()



private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface UserApi {
    @Multipart
    @POST("plants/")
    suspend fun addData(
        @Part("nama") namaTanaman: RequestBody,
        @Part("tag") tag: RequestBody,
        @Part("lokasi") lokasi: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("user_email") userEmail: RequestBody,
        @Part file: MultipartBody.Part
    ): Tanaman
    @GET("plants/")
    suspend fun getAllData(
        @Query("email") email: String,
    ): List<Tanaman>

    @DELETE("plants/{plant_id}")
    suspend fun deleteData(
        @Path("plant_id") id: Int,
        @Query("email") email: String
    ): MessageResponse
}


object Api {
    val userService: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    fun getImageUrl(imageId: String): String{
        return BASE_URL + "plants/images/$imageId"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }