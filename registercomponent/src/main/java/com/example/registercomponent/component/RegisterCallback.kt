package com.example.registercomponent.component

import com.example.registercomponent.model.User

interface RegisterCallback {
    /**
     * Được gọi khi người dùng nhấn nút "Đăng ký" và dữ liệu đầu vào là hợp lệ.
     * @param user Đối tượng User chứa thông tin người dùng đã nhập.
     */
    fun onRegisterClicked(user: User)

    /**
     * Được gọi khi người dùng nhấn nút "Quay lại Đăng nhập".
     */
    fun onBackToLoginCLicked()
}