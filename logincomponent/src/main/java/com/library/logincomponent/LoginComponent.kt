package com.library.logincomponent

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.library.logincomponent.databinding.ComponentLoginBinding
import com.library.logincomponent.model.User
import com.library.logincomponent.utils.ValidationUtils

/**
 * LoginComponent - Reusable Login Component with COP approach
 *
 * Usage:
 * val loginComponent = LoginComponent(context, config)
 * loginComponent.attachToView(parentView)
 * loginComponent.setCallback(callback)
 */
class LoginComponent(
    private val context: Context,
    private val config: LoginConfig = LoginConfig.createDefault()
) {
    private var binding: ComponentLoginBinding? = null
    private var callback: LoginCallback? = null
    private var database: DatabaseReference? = null
    private var progressDialog: Dialog? = null
    private var isPasswordVisible = false
    private var countdownTimer: CountDownTimer? = null

    // Login attempt tracking
    private val PREF_NAME = "login_component_prefs"
    private val KEY_FAILED_ATTEMPTS = "failed_attempts"
    private val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
    private val KEY_BLOCK_UNTIL = "block_until"
    private val DEFAULT_HINT = "Vui lòng nhập số điện thoại đã đăng ký"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        database = FirebaseDatabase.getInstance().reference
    }

    /**
     * Attach component to a parent view
     */
    fun attachToView(parentView: android.view.ViewGroup) {
        binding = ComponentLoginBinding.inflate(LayoutInflater.from(context), parentView, true)
        setupUI()
        setupListeners()
        checkBlockStatus()
    }

    /**
     * Get the root view of component
     */
    fun getView(): View? {
        if (binding == null) {
            binding = ComponentLoginBinding.inflate(LayoutInflater.from(context), null, false)
            setupUI()
            setupListeners()
            checkBlockStatus()
        }
        return binding?.root
    }

    /**
     * Set callback for component events
     */
    fun setCallback(callback: LoginCallback) {
        this.callback = callback
    }

    /**
     * Setup UI based on configuration
     */
    private fun setupUI() {
        binding?.apply {
            // Apply configuration
            if (!config.allowRegistration) {
                tvRegister.visibility = View.GONE
                root.findViewById<View>(R.id.ll_register_section)?.visibility = View.GONE
            }

            if (!config.showForgotPassword) {
                tvForgotPassword.visibility = View.GONE
            }

            // Custom logo/background if provided
            config.logoResId?.let {
                imgLogo.setImageResource(it)
            }

            config.backgroundResId?.let {
                root.setBackgroundResource(it)
            }

            // Set hotline
            tvSupportText.text = "Vui lòng liên hệ hotline ${config.hotlineNumber} nếu bạn cần hỗ trợ."
        }
    }

    /**
     * Setup click listeners
     */
    private fun setupListeners() {
        binding?.apply {
            btnLogin.setOnClickListener {
                performLogin()
            }

            tvRegister.setOnClickListener {
                callback?.onNavigateToRegister()
            }

            imgShowPassword.setOnClickListener {
                togglePasswordVisibility()
            }

            tvForgotPassword.setOnClickListener {
                callback?.onForgotPassword()
                Toast.makeText(context, "Vui lòng liên hệ hotline: ${config.hotlineNumber}", Toast.LENGTH_LONG).show()
            }

            tvCallNow.setOnClickListener {
                callback?.onCallHotline(config.hotlineNumber)
                Toast.makeText(context, "Gọi: ${config.hotlineNumber}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Check if account is currently blocked
     */
    private fun checkBlockStatus() {
        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0)
        val remaining = blockUntil - System.currentTimeMillis()
        if (remaining > 0) {
            startCountdown(remaining)
        } else {
            binding?.tvMaybeCountdown?.text = DEFAULT_HINT
            binding?.btnLogin?.isEnabled = true
        }
    }

    /**
     * Perform login action
     */
    private fun performLogin() {
        val phone = binding?.edtPhone?.text.toString().trim()
        val password = binding?.edtPassword?.text.toString().trim()

        if (isBlocked()) {
            Toast.makeText(context, "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau ${config.blockTimeMinutes} phút.", Toast.LENGTH_LONG).show()
            return
        }

        // Validate input
        val phoneError = ValidationUtils.getPhoneError(phone, config.phoneLength)
        if (phoneError != null) {
            binding?.edtPhone?.error = phoneError
            return
        }

        val passwordError = ValidationUtils.getPasswordError(password, config.minPasswordLength)
        if (passwordError != null) {
            binding?.edtPassword?.error = passwordError
            return
        }

        // Perform authentication
        authenticateUser(phone, password)
    }

    /**
     * Authenticate user with Firebase
     */
    private fun authenticateUser(phone: String, password: String) {
        showLoading(true)

        database?.child(config.firebasePath)?.orderByChild("phone")?.equalTo(phone)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    showLoading(false)

                    if (dataSnapshot.exists()) {
                        var authenticated = false
                        for (userSnapshot in dataSnapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            user?.id = userSnapshot.key

                            if (user?.password == password) {
                                authenticated = true
                                resetLoginAttempts()
                                callback?.onLoginSuccess(user)
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }

                        if (!authenticated) {
                            recordFailedAttempt()
                            callback?.onLoginFailure("Mật khẩu không đúng!")
                            Toast.makeText(context, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        recordFailedAttempt()
                        callback?.onLoginFailure("Số điện thoại chưa được đăng ký!")
                        Toast.makeText(context, "Số điện thoại chưa được đăng ký!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showLoading(false)
                    val errorMsg = "Lỗi kết nối: ${databaseError.message}"
                    callback?.onLoginFailure(errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Toggle password visibility
     */
    private fun togglePasswordVisibility() {
        binding?.apply {
            if (isPasswordVisible) {
                edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imgShowPassword.setImageResource(R.drawable.ic_eye_closed)
            } else {
                edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgShowPassword.setImageResource(R.drawable.ic_eye_open)
            }
            isPasswordVisible = !isPasswordVisible
            edtPassword.setSelection(edtPassword.text?.length ?: 0)
        }
    }

    /**
     * Show/hide loading dialog
     */
    private fun showLoading(show: Boolean) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = Dialog(context)
                progressDialog?.setContentView(R.layout.dialog_progress)
                progressDialog?.setCancelable(false)
                progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
            progressDialog?.show()
        } else {
            progressDialog?.dismiss()
        }
    }

    /**
     * Record failed login attempt
     */
    private fun recordFailedAttempt() {
        val editor = prefs.edit()
        val now = System.currentTimeMillis()
        val lastTime = prefs.getLong(KEY_LAST_ATTEMPT_TIME, 0)
        val prevAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

        val blockTime = config.blockTimeMinutes * 60 * 1000L
        val failedAttempts = if (now - lastTime > blockTime) 1 else prevAttempts + 1

        editor.putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
        editor.putLong(KEY_LAST_ATTEMPT_TIME, now)

        if (failedAttempts >= config.maxLoginAttempts) {
            val blockUntil = now + blockTime
            editor.putLong(KEY_BLOCK_UNTIL, blockUntil)
            editor.apply()

            Toast.makeText(
                context,
                "Bạn đã nhập sai quá nhiều lần. Tài khoản bị khóa trong ${config.blockTimeMinutes} phút.",
                Toast.LENGTH_LONG
            ).show()

            startCountdown(blockTime)
        } else {
            editor.apply()
        }
    }

    /**
     * Check if currently blocked
     */
    private fun isBlocked(): Boolean {
        val blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0)
        return System.currentTimeMillis() < blockUntil
    }

    /**
     * Reset login attempts
     */
    private fun resetLoginAttempts() {
        prefs.edit().clear().apply()
        stopCountdown()
        binding?.tvMaybeCountdown?.text = DEFAULT_HINT
        binding?.btnLogin?.isEnabled = true
    }

    /**
     * Start countdown timer
     */
    private fun startCountdown(remainingMillis: Long) {
        if (remainingMillis <= 0L) {
            resetLoginAttempts()
            return
        }

        binding?.btnLogin?.isEnabled = false
        binding?.tvMaybeCountdown?.setTextColor(Color.parseColor("#D32F2F"))

        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSec = millisUntilFinished / 1000
                val minutes = totalSec / 60
                val seconds = totalSec % 60
                binding?.tvMaybeCountdown?.text = "Bạn có thể thử lại sau: ${minutes}m ${seconds}s"
            }

            override fun onFinish() {
                resetLoginAttempts()
            }
        }.start()
    }

    /**
     * Stop countdown timer
     */
    private fun stopCountdown() {
        countdownTimer?.cancel()
        countdownTimer = null
        binding?.btnLogin?.isEnabled = true
        binding?.tvMaybeCountdown?.setTextColor(Color.parseColor("#757575"))
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        stopCountdown()
        progressDialog?.dismiss()
        binding = null
        callback = null
    }

    /**
     * Pre-fill phone number
     */
    fun setPhoneNumber(phone: String) {
        binding?.edtPhone?.setText(phone)
    }

    /**
     * Clear all fields
     */
    fun clearFields() {
        binding?.edtPhone?.text?.clear()
        binding?.edtPassword?.text?.clear()
    }
}