package com.example.basicbankingapp

data class User(
    val uid : String = "",
    val displayName:String?="",
    val displayEmail:String?="",
    val imageUrl:String="",
    val newUser: Boolean=true
)