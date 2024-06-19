package com.d3if3022.plantbuddy.model

data class Tanaman(
    val user_email: String,
    val id: Int,
    val nama: String,
    val tag: String,
    val lokasi: String,
    val deskripsi: String,
    val image_id: String,
    val created_at: String
)