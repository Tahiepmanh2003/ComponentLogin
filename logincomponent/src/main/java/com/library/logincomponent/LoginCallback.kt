package com.library.logincomponent

import com.library.logincomponent.model.User

interface LoginCallback {
    fun onLoginSuccess(user: User)
    fun onLoginFailure(errorMessage: String)
    fun onNavigateToRegister()
    fun onForgotPassword()
    fun onCallHotline(phoneNumber: String)
}