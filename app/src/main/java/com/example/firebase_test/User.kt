package com.example.firebase_test

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val username: String? = null,
    val email: String? = null)
