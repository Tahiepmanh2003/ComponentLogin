package com.library.homecomponent.component

import androidx.fragment.app.Fragment

/**
 * Lớp cấu hình để khởi tạo HomeComponent.
 *
 * @property fragments Danh sách các Fragment sẽ được hiển thị trong ViewPager2.
 *                     Thứ tự của Fragment trong danh sách này phải tương ứng với
 *                     thứ tự của các item trong Bottom Navigation Menu.
 * @property initialPage Vị trí trang ban đầu sẽ được hiển thị (mặc định là 0, tức trang Home).
 * @property offscreenPageLimit Số lượng trang được giữ lại trong bộ nhớ ở mỗi bên của trang hiện tại.
 */
data class HomeConfig(
    val fragments: List<Fragment>,
    val initialPage: Int = 0,
    val offscreenPageLimit: Int = 1
)