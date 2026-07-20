package com.example.futbolnomade.data.security

data class BiometricCredentials(
    val uid: String,
    val email: String,
    val password: String
)

data class BiometricAccountInfo(
    val uid: String,
    val email: String
)