package com.example.registercomponent.utils

import android.util.Patterns
import com.example.registercomponent.component.RegisterConfig
import com.library.registercomponent.databinding.ComponentRegisterBinding

object ValidationUtils {

    /**
     * Hàm kiểm tra toàn bộ dữ liệu đầu vào của form đăng ký.
     * Sẽ hiển thị lỗi trực tiếp lên các EditText nếu có.
     *
     * @param binding Đối tượng ViewBinding của component để truy cập các EditText.
     * @param config Đối tượng cấu hình để biết các quy tắc đặc biệt (ví dụ: địa chỉ có bắt buộc không).
     * @return Trả về true nếu tất cả dữ liệu hợp lệ, ngược lại trả về false.
     */
    fun validateRegistrationForm(binding: ComponentRegisterBinding, config: RegisterConfig): Boolean {
        // Lấy dữ liệu từ binding
        val fullName = binding.edtFullName.text.toString().trim()
        val phone = binding.edtPhoneRegister.text.toString().trim()
        val email = binding.edtEmailRegister.text.toString().trim()
        val address = binding.edtAddressRegister.text.toString().trim()
        val password = binding.edtPasswordRegister.text.toString()
        val confirmPassword = binding.edtConfirmPassword.text.toString()

        // Reset lỗi cũ trước khi kiểm tra
        binding.edtFullName.error = null
        binding.edtPhoneRegister.error = null
        binding.edtEmailRegister.error = null
        binding.edtAddressRegister.error = null
        binding.edtPasswordRegister.error = null
        binding.edtConfirmPassword.error = null

        // Bắt đầu kiểm tra tuần tự
        if (fullName.isEmpty()) {
            binding.edtFullName.error = "Vui lòng nhập họ và tên"
            return false
        }

        if (phone.isEmpty()) {
            binding.edtPhoneRegister.error = "Vui lòng nhập số điện thoại"
            return false
        }

        if (!phone.matches(Regex("^0[0-9]{9,10}$"))) {
            binding.edtPhoneRegister.error = "Số điện thoại không hợp lệ"
            return false
        }

        if (email.isEmpty()) {
            binding.edtEmailRegister.error = "Vui lòng nhập email"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmailRegister.error = "Email không hợp lệ"
            return false
        }

        // Sử dụng config để kiểm tra xem địa chỉ có bắt buộc không
        if (config.addressIsRequired && address.isEmpty()) {
            binding.edtAddressRegister.error = "Vui lòng nhập địa chỉ"
            return false
        }

        if (password.isEmpty()) {
            binding.edtPasswordRegister.error = "Vui lòng nhập mật khẩu"
            return false
        }

        if (password.length < 6) {
            binding.edtPasswordRegister.error = "Mật khẩu phải có ít nhất 6 ký tự"
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.edtConfirmPassword.error = "Vui lòng xác nhận mật khẩu"
            return false
        }

        if (password != confirmPassword) {
            binding.edtConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            return false
        }

        return true
    }
}