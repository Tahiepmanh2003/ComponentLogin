package com.library.homecomponent.component

interface HomeCallback {
    // Sự kiện từ Navigation Drawer
    fun onAccountInfoClicked()
    fun onLocationClicked()
    fun onChangePasswordClicked()
    fun onLogoutClicked()
    fun onPrivacyPolicyClicked()

    // Sự kiện từ Toolbar
    fun onToolbarMenuClicked() // Khi nhấn vào icon menu (hamburger icon)
    fun onToolbarNotificationClicked() // Khi nhấn vào icon chuông (trong drawer)
    fun onToolbarCartClicked() // Khi nhấn vào icon giỏ hàng
    fun onToolbarBackClicked() // Khi nhấn vào nút back

}