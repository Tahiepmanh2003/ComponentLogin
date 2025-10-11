package com.library.logincomponent

data class LoginConfig(
    val allowRegistration: Boolean = true,
    val showForgotPassword: Boolean = true,
    val minPasswordLength: Int = 6,
    val phoneLength: Int = 10,
    val maxLoginAttempts: Int = 5,
    val blockTimeMinutes: Int = 2,
    val hotlineNumber: String = "0878260833",
    val firebasePath: String = "users",
    val logoResId: Int? = null,
    val backgroundResId: Int? = null
) {
    companion object {
        fun createDefault() = LoginConfig()
    }
}