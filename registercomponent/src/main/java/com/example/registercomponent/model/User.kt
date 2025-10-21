package com.example.registercomponent.model

import java.io.Serializable

data class User(
    var id: String? = null,
    var fullName: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var address: String? = null,
    var password: String? = null,
    var province: String? = null,
    var district: String? = null,
    var createdAt: Long = System.currentTimeMillis()
) : Serializable