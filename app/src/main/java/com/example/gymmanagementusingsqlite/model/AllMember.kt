package com.example.gymmanagementusingsqlite.model

import androidx.core.app.GrammaticalInflectionManagerCompat.GrammaticalGender

data class AllMember(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val age: String = "",
    val weight: String = "",
    val mobile: String = "",
    val address: String = "",
    val dateOfJoining: String = "",
    val membership: String = "",
    val expiryDate: String = "",
    val discount: String = "",
    val total: String = "",
    val image: String = "",  // Add the imagePath field
    val status: String = ""


)