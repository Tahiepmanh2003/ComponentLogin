package com.library.homecomponent.component

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.library.homecomponent.R
import com.library.homecomponent.databinding.ActivityMainWithDrawerBinding

class HomeComponent(
    private val activity: AppCompatActivity,
    private val config: HomeConfig
) {
    private var binding: ActivityMainWithDrawerBinding? = null
    private var callback: HomeCallback? = null
    private var isBackButtonShown = false // Biến theo dõi trạng thái của nút trái (Menu/Back)

    fun attachTo(parent: ViewGroup) {
        if (binding == null) {
            binding = ActivityMainWithDrawerBinding.inflate(LayoutInflater.from(activity), parent, true)
            setupViewPager()
            setupBottomNavigation()
            setupDrawer()
            setupToolbar()
        }
    }

    fun setCallback(callback: HomeCallback) {
        this.callback = callback
    }

    // --- Các hàm thiết lập (private) ---

    private fun setupViewPager() {
        binding?.let {
            // Không cho phép người dùng vuốt ngang ViewPager
            it.viewpager2.isUserInputEnabled = false
            // Dùng FragmentStateAdapter để quản lý các Fragment
            val pagerAdapter = ScreenSlidePagerAdapter(activity, config.fragments)
            it.viewpager2.adapter = pagerAdapter
            it.viewpager2.offscreenPageLimit = config.offscreenPageLimit
            it.viewpager2.setCurrentItem(config.initialPage, false)
        }
    }

    private fun setupBottomNavigation() {
        binding?.let { b ->
            b.bottomNavigation.setOnItemSelectedListener { item ->
                // Hiển thị loading trước khi chuyển trang để tạo cảm giác mượt mà
                showLoading(true)
                // Dùng postDelayed để đảm bảo ProgressBar kịp hiển thị
                Handler(Looper.getMainLooper()).postDelayed({
                    val position = when (item.itemId) {
                        R.id.nav_home -> 0
                        R.id.nav_cart -> 1
                        R.id.nav_feedback -> 2
                        R.id.nav_contact -> 3
                        R.id.nav_order -> 4
                        else -> -1
                    }
                    if (position != -1) {
                        b.viewpager2.currentItem = position
                        updateToolbarForPage(position)
                        // Khi quay về các trang chính, luôn đảm bảo nút bên trái là nút Menu
                        showBackButton(false)
                    }
                    // Ẩn loading sau khi đã chuyển trang
                    showLoading(false)
                }, 150) // Delay 150ms
                true
            }
        }
    }

    private fun setupDrawer() {
        binding?.layoutAccountInfo?.setOnClickListener { callback?.onAccountInfoClicked() }
        binding?.layoutLocation?.setOnClickListener { callback?.onLocationClicked() }
        binding?.layoutChangePassword?.setOnClickListener { callback?.onChangePasswordClicked() }
        binding?.layoutLogout?.setOnClickListener { callback?.onLogoutClicked() }
        binding?.layoutPrivacyPolicy?.setOnClickListener { callback?.onPrivacyPolicyClicked() }
    }

    private fun setupToolbar() {
        binding?.toolbar?.apply {
            imgBack.setOnClickListener {
                if (isBackButtonShown) {
                    // Nếu đang là nút BACK, gọi callback back
                    callback?.onToolbarBackClicked()
                } else {
                    // Nếu đang là nút MENU, gọi callback menu và mở drawer
                    callback?.onToolbarMenuClicked()
                    openDrawer()
                }
            }

            imgCart.setOnClickListener { callback?.onToolbarCartClicked() }
        }
    }

    // --- Các hàm điều khiển (public) ---

    fun updateUserInfo(name: String, phone: String, email: String) {
        binding?.tvUserName?.text = name
        binding?.tvUserPhone?.text = phone
        binding?.tvUserEmail?.text = email
    }

    fun openDrawer() {
        binding?.drawerLayout?.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
    }

    fun showLoading(show: Boolean) {
        binding?.let {
            it.progressBar.visibility = if (show) View.VISIBLE else View.GONE
            // Ẩn nội dung khi loading để tránh người dùng tương tác
            it.viewpager2.visibility = if (show) View.INVISIBLE else View.VISIBLE
        }
    }

    /**
     * Hàm quan trọng: Thay đổi vai trò của nút bên trái (Menu/Back).
     * @param show true để nút trở thành nút BACK, false để trở thành nút MENU.
     */
    fun showBackButton(show: Boolean) {
        isBackButtonShown = show
        binding?.toolbar?.apply {
            if (show) {
                imgBack.setImageResource(R.drawable.ic_arrow_back)
            } else {
//                imgBack.setImageResource(R.drawable.ic_menu)
            }
        }
        lockDrawer(show)
    }

    /**
     * Khóa hoặc mở khóa NavigationDrawer.
     */
    fun lockDrawer(lock: Boolean) {
        val mode = if (lock) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED
        binding?.drawerLayout?.setDrawerLockMode(mode)
    }

    /**
     * Chuyển đến một trang cụ thể trong ViewPager.
     * @param pageIndex Vị trí trang muốn đến.
     * @param smoothScroll true để có hiệu ứng cuộn, false để chuyển ngay lập tức.
     */
    fun navigateToPage(pageIndex: Int, smoothScroll: Boolean = false) {
        binding?.viewpager2?.setCurrentItem(pageIndex, smoothScroll)
        // Cập nhật cả BottomNavigation nếu trang đó nằm trong BottomNav
        if (pageIndex in 0..4) {
            binding?.bottomNavigation?.menu?.getItem(pageIndex)?.isChecked = true
        }
    }

    /**
     * Xử lý sự kiện nhấn nút back của hệ thống.
     * Dự án chính sẽ gọi hàm này từ onBackPressed() của Activity.
     * @return true nếu sự kiện đã được xử lý bởi component (ví dụ: đóng drawer),
     *         false nếu component không xử lý, để Activity tự quyết định (ví dụ: hiện dialog thoát).
     */
    fun handleOnBackPressed(): Boolean {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            closeDrawer()
            return true // Đã xử lý: đóng drawer
        }
        return false // Chưa xử lý: để Activity lo
    }

    fun destroy() {
        binding = null
        callback = null
    }

    // --- Các hàm private tiện ích ---

    private fun updateToolbarForPage(position: Int) {
        val title = when (position) {
            0 -> activity.getString(R.string.home)
            1 -> activity.getString(R.string.cart)
            2 -> activity.getString(R.string.feedback)
            3 -> activity.getString(R.string.contact)
            4 -> activity.getString(R.string.order)
            else -> ""
        }
        setToolbarTitle(title)
    }

    fun setToolbarTitle(title: String) {
        binding?.toolbar?.tvTitle?.text = title
    }

    // --- Lớp Adapter nội bộ cho ViewPager2 ---
    private inner class ScreenSlidePagerAdapter(
        activity: AppCompatActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}
