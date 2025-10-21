package com.example.registercomponent.component

import android.content.Context
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.registercomponent.model.User
import com.example.registercomponent.utils.ValidationUtils
import com.library.registercomponent.R
import com.library.registercomponent.databinding.ComponentRegisterBinding

class RegisterComponent(
    private val context: Context,
    private val config: RegisterConfig = RegisterConfig.createDefault()
) {
    private var binding: ComponentRegisterBinding? = null
    private var callback: RegisterCallback? = null
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    /**
     * Gắn component vào một ViewGroup cha.
     */
    fun attachTo(parent: ViewGroup) {
        if (binding == null) {
            binding = ComponentRegisterBinding.inflate(LayoutInflater.from(context), parent, true)
            setupUI()
            setupListeners()
        }
    }

    /**
     * Đăng ký một callback để lắng nghe sự kiện.
     */
    fun setCallback(callback: RegisterCallback) {
        this.callback = callback
    }

    /**
     * Thiết lập giao diện dựa trên đối tượng config đã được truyền vào.
     */
    private fun setupUI() {
        binding?.apply {
            if (!config.showLogo) {
                imgLogo.visibility = View.GONE
            }

            if (!config.showBackToLoginButton) {
                tvBackToLogin.visibility = View.GONE
            }

            config.customLogoResId?.let {
                imgLogo.setImageResource(it)
            }
        }
    }

    /**
     * Thiết lập các listener cho các View.
     */
    private fun setupListeners() {
        binding?.btnRegister?.setOnClickListener {
            handleRegisterAttempt()
        }

        binding?.tvBackToLogin?.setOnClickListener {
            callback?.onBackToLoginCLicked()
        }

        binding?.imgShowPasswordRegister?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(binding!!.edtPasswordRegister, binding!!.imgShowPasswordRegister, isPasswordVisible)
        }

        binding?.imgShowConfirmPassword?.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(binding!!.edtConfirmPassword, binding!!.imgShowConfirmPassword, isConfirmPasswordVisible)
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút đăng ký.
     * Nhiệm vụ: chỉ validate và gọi callback.
     */
    private fun handleRegisterAttempt() {
        binding ?: return
        val isFormValid = ValidationUtils.validateRegistrationForm(binding!!, config)
        if (!isFormValid) {
            return
        }
        val fullName = binding!!.edtFullName.text.toString().trim()
        val phone = binding!!.edtPhoneRegister.text.toString().trim()
        val email = binding!!.edtEmailRegister.text.toString().trim()
        val address = binding!!.edtAddressRegister.text.toString().trim()
        val password = binding!!.edtPasswordRegister.text.toString()

        val user = User(
            fullName = fullName,
            phone = phone,
            email = email,
            address = address.ifEmpty { null },
            password = password
        )

        callback?.onRegisterClicked(user)
    }

    /**
     * Xử lý ẩn hiện mật khẩu
     */
    private fun togglePasswordVisibility(editText: android.widget.EditText, imageView: android.widget.ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye_open)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye_closed)
        }
        editText.setSelection(editText.text?.length ?: 0)
    }


    /**
     * Dọn dẹp tài nguyên khi không còn sử dụng.
     * Dự án chính sẽ phải gọi hàm này (ví dụ trong onDestroy của Activity).
     */
    fun destroy() {
        binding = null
        callback = null
    }
}