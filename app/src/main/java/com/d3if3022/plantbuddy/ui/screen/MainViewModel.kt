package com.d3if3022.plantbuddy.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.d3if3022.plantbuddy.model.Tanaman
import com.d3if3022.plantbuddy.network.Api
import com.d3if3022.plantbuddy.network.ApiStatus
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Tanaman>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    var errorMessageNoToast = mutableStateOf<String?>(null)
        private set

    var querySuccess = mutableStateOf(false)
        private set

    var isUploading = mutableStateOf(false)
        private set


    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = Api.userService.getAllData(userId)
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainVM", "data error: ${e.message}")
                errorMessageNoToast.value = when (e.message) {
                    "HTTP 404 " -> "Anda belum memasukkan data."
                    else -> "Gagal memuat data."
                }
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(
        email: String,
        nama: String,
        tag: String,
        lokasi: String,
        deskripsi: String,
        bitmap: Bitmap
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val namaPart = RequestBody.create("text/plain".toMediaTypeOrNull(), nama)
                val tagPart = RequestBody.create("text/plain".toMediaTypeOrNull(), tag)
                val lokasiPart = RequestBody.create("text/plain".toMediaTypeOrNull(), lokasi)
                val deskripsiPart = RequestBody.create("text/plain".toMediaTypeOrNull(), deskripsi)
                val userEmailPart = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
                        isUploading.value = true
                val result =
                    withTimeout(20000L) {
                        Api.userService.addData(
                            namaPart,
                            tagPart,
                            lokasiPart,
                            deskripsiPart,
                            userEmailPart,
                            bitmap.toMultipartBody()
                        )
                    }
                        isUploading.value = false
                Log.d("MainVM", "result: $result")
                querySuccess.value = true
                retrieveData(email)
            } catch (e: Exception) {
                Log.d("MainVM", "save error: ${e.message}")
                errorMessage.value = when (e.message) {
                    "HTTP 500 " -> "Database idle, harap inputkan kembali data."
                    else -> "Terjadi kesalahan, harap coba lagi"
                }
            }
        }
    }


    fun deleteData(email: String, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Api.userService.deleteData(id, email)
                querySuccess.value = true
                retrieveData(email)
            } catch (e: Exception) {
                Log.d("MainVM", "delete error: ${e.message}")
                errorMessage.value = when (e.message) {
                    "HTTP 500 " -> "Database idle, harap inputkan kembali data."
                    else -> "Request timeout atau terjadi kesalahan, harap coba lagi.   "
                }
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 30, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size
        )
        return MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
    }

    fun clearMessage() {
        errorMessage.value = null
        querySuccess.value = false
        isUploading.value = false
    }

}