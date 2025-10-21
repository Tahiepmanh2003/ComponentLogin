package com.example.registercomponent.component

import androidx.annotation.DrawableRes

/**
 * Lớp cấu hình để tùy biến giao diện của RegisterComponent.
 *
 * @property showLogo Hiển thị hoặc ẩn logo (mặc định là true).
 * @property showBackToLoginButton Hiển thị hoặc ẩn nút "Quay lại Đăng nhập" (mặc định là true).
 * @property customLogoResId Cho phép thay thế logo mặc định bằng một drawable khác.
 * @property addressIsRequired Bắt buộc người dùng phải nhập địa chỉ (mặc định là false).
 */
data class RegisterConfig(
    val showLogo: Boolean = true,
    val showBackToLoginButton: Boolean = true,
    @DrawableRes val customLogoResId: Int? = null,
    val addressIsRequired: Boolean = false
) {
    companion object {
        /**
         * Tạo một cấu hình mặc định.
         */
        fun createDefault(): RegisterConfig {
            return RegisterConfig()
        }
    }
}
