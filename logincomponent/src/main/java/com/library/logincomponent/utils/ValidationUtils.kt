package com.library.logincomponent.utils

object ValidationUtils {
    fun isValidPhone(phone: String, requiredLength: Int = 10): Boolean {
        return phone.isNotEmpty() && phone.length >= requiredLength && phone.matches(Regex("^[0-9]+$"))
    }

    fun isValidPassword(password: String, minLength: Int = 6): Boolean {
        return password.isNotEmpty() && password.length >= minLength
    }

    fun getPhoneError(phone: String, requiredLength: Int): String? {
        return when {
            phone.isEmpty() -> "Vui lòng nhập số điện thoại"
            phone.length < requiredLength -> "Số điện thoại không hợp lệ"
            !phone.matches(Regex("^[0-9]+$")) -> "Số điện thoại chỉ chứa số"
            else -> null
        }
    }

    fun getPasswordError(password: String, minLength: Int): String? {
        return when {
            password.isEmpty() -> "Vui lòng nhập mật khẩu"
            password.length < minLength -> "Mật khẩu phải có ít nhất $minLength ký tự"
            else -> null
        }
    }
}