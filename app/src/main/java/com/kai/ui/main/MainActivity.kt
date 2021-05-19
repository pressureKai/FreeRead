package com.kai.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.SparseArray
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.appcompat.widget.SwitchCompat
import androidx.core.util.isEmpty
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.widget.load.ChargeLoadMoreListener
import com.kai.base.widget.load.ListPageLoader
import com.kai.base.widget.load.RefreshDataListener
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.customToast
import com.kai.common.extension.getScreenWidth
import com.kai.common.utils.RxNetworkObserver
import com.kai.common.utils.ScreenUtils
import com.kai.common.utils.SharedPreferenceUtils
import com.kai.crawler.Crawler
import com.kai.crawler.entity.book.SearchBook
import com.kai.entity.User
import com.kai.ui.forgetpassword.ForgetPasswordActivity
import com.kai.ui.fragments.ranking.BookRankingFragment
import com.kai.ui.fragments.recommend.BookRecommendFragment
import com.kai.ui.fragments.shelf.BookShelfFragment
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import skin.support.SkinCompatManager
import skin.support.widget.SkinCompatSupportable

/**
 *# app - 首页
 *@author pressureKai
 *@date  2021/4/13
 */
class MainActivity : BaseMvpActivity<MainContract.View, MainPresenter>(), MainContract.View,
        RefreshDataListener, ChargeLoadMoreListener, SkinCompatSupportable {
    companion object {
        const val CODE_FROM_FONTS = 0x11
        const val IS_DAY = "is_day"
    }

    private lateinit var listPageLoader: ListPageLoader<SearchBook>
    private var drawLayoutIsOpen = false
    private var isDay = true
    private var currentUser: User? = null

    private var fragments: SparseArray<Fragment>? = null
    private var icons: SparseArray<ImageView>? = null
    private var textViews: SparseArray<TextView>? = null

    private var mCurrentIndex = 0
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }


    private fun onPageChange(position: Int) {
        textViews?.let {
            for (value in 0.until(it.size())) {
                if (value == position) {
                    it.get(value).typeface = Typeface.DEFAULT_BOLD
                } else {
                    it.get(value).typeface = Typeface.DEFAULT
                }
            }
        }

    }

    private fun initFragment() {

        view_pager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
               onPageChange(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
        if (icons == null) {
            icons = SparseArray()
        }

        icons?.let {
            if (it.isEmpty()) {
                it.put(0, shelf_image)
                it.put(1, recommend_image)
                it.put(2, ranking_image)
            }
        }



        if (textViews == null) {
            textViews = SparseArray()
        }

        textViews?.let {
            if (it.isEmpty()) {
                it.put(0, shelf_text)
                it.put(1, recommend_text)
                it.put(2, ranking_text)
            }
        }

        if (fragments == null) {
            fragments = SparseArray()
        }

        fragments?.let {
            if (it.isEmpty()) {
                it.put(0, BookShelfFragment.newInstance())
                it.put(1, BookRecommendFragment.newInstance())
                it.put(2, BookRankingFragment.newInstance())
            }
            view_pager.offscreenPageLimit = 3
            view_pager.adapter = TabAdapter(supportFragmentManager)
        }

        shelf_layout.setOnClickListener {
            view_pager.currentItem = 0
        }

        recommend_layout.setOnClickListener {
            view_pager.currentItem = 1
        }

        ranking_layout.setOnClickListener {
            view_pager.currentItem = 2
        }


    }

    override fun initView() {
        RxNetworkObserver.register(this)
        initFragment()
        mPresenter?.getLoginCurrentUser()
//        initImmersionBar(fitSystem = false, color = R.color.app_background)
//        val layoutParams = content.layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.topMargin = ScreenUtils.getStatusBarHeight()
        draw_content.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0)
        draw_content.layoutParams.width = ((getScreenWidth() / 6f) * 5).toInt()
        draw_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (SkinCompatManager.getInstance().curSkinName != "night") {
                    val d = (0.5 * slideOffset).toFloat()
                    shadow_view.alpha = d
                } else {
                    val d = (0.2 * slideOffset).toFloat()
                    shadow_view.alpha = d
                }

            }

            override fun onDrawerOpened(drawerView: View) {
                if (SkinCompatManager.getInstance().curSkinName != "night") {
                    shadow_view.alpha = 0.5f
                } else {
                    shadow_view.alpha = 0.2f
                }
                drawLayoutIsOpen = true

            }

            override fun onDrawerClosed(drawerView: View) {
                drawLayoutIsOpen = false
                shadow_view.alpha = 0f
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })


        val settingIcon = setting.findViewById<ImageView>(R.id.icon)
        val settingName = setting.findViewById<TextView>(R.id.name)
        settingIcon.setImageResource(R.drawable.setting)
        settingName.text = "设置"
        setting.setOnClickListener {

        }


        val aboutIcon = about.findViewById<ImageView>(R.id.icon)
        val aboutName = about.findViewById<TextView>(R.id.name)
        aboutIcon.setImageResource(R.drawable.about)
        aboutName.text = "关于"
        about.setOnClickListener {

        }


        val dayNightModelIcon = day_night_model.findViewById<ImageView>(R.id.icon)
        val dayNightModelName = day_night_model.findViewById<TextView>(R.id.name)
        val dayNightModelNextIcon = day_night_model.findViewById<ImageView>(R.id.next_icon)
        val dayNightModelSwitchView = day_night_model.findViewById<SwitchCompat>(R.id.switch_view)


        SharedPreferenceUtils.getInstance()?.let {
            isDay = it.getBoolean(IS_DAY, true)
        }

        dayNightModelName.text = "夜间模式"
        dayNightModelNextIcon.visibility = View.GONE
        dayNightModelSwitchView.visibility = View.VISIBLE
        dayNightModelSwitchView.isChecked = !isDay
        dayNightModelIcon.setImageResource(R.drawable.night)
        dayNightModelSwitchView.setOnClickListener {
            SharedPreferenceUtils.getInstance()?.let {


                it.putBoolean(IS_DAY, !isDay)
                isDay = it.getBoolean(IS_DAY, true)
                dayNightModelSwitchView.isChecked = !isDay


                if (isDay) {
                    SkinCompatManager.getInstance().restoreDefaultTheme()
                } else {
                    SkinCompatManager.getInstance().loadSkin(
                            "night",
                            SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN
                    )
                }


            }
        }

        val scanIcon = scan.findViewById<ImageView>(R.id.icon)
        val scanName = scan.findViewById<TextView>(R.id.name)
        scanIcon.setImageResource(R.drawable.scan)
        scanName.text = "扫描本地书籍"
        scan.setOnClickListener {

        }


        val wifiIcon = wifi.findViewById<ImageView>(R.id.icon)
        val wifiName = wifi.findViewById<TextView>(R.id.name)
        wifiIcon.setImageResource(R.drawable.wifi)
        wifiName.text = "Wi-Fi传书"
        wifi.setOnClickListener {

        }


        val bluetoothIcon = bluetooth.findViewById<ImageView>(R.id.icon)
        val bluetoothName = bluetooth.findViewById<TextView>(R.id.name)
        bluetoothIcon.setImageResource(R.drawable.bluetooth)
        bluetoothName.text = "蓝牙传书"
        bluetooth.setOnClickListener {
            ARouter.getInstance().build("/app/bluetooth").navigation()
        }


        val fontIcon = font.findViewById<ImageView>(R.id.icon)
        val fontName = font.findViewById<TextView>(R.id.name)
        fontIcon.setImageResource(R.drawable.font)
        fontName.text = "全局字体"
        font.setOnClickListener {
            ARouter
                    .getInstance()
                    .build("/app/fonts")
                    .navigation()
        }


        val unRegisterIcon = un_register.findViewById<ImageView>(R.id.icon)
        val unRegisterName = un_register.findViewById<TextView>(R.id.name)
        unRegisterIcon.setImageResource(R.drawable.un_register)
        unRegisterName.text = "注销账号"
        un_register.setOnClickListener {
            currentUser?.let {
                postStickyEvent(
                        it.account,
                        ForgetPasswordActivity.FORGET_PASSWORD_CODE,
                        ForgetPasswordActivity::class.java.name
                )
                ARouter
                        .getInstance()
                        .build("/app/forgetPassword")
                        .withBoolean("unRegister", true)
                        .navigation()
            }
            if (currentUser == null) {
                customToast("请登录")
            }
        }


        val quitAppIcon = quit_app.findViewById<ImageView>(R.id.icon)
        val quitAppName = quit_app.findViewById<TextView>(R.id.name)
        quitAppIcon.setImageResource(R.drawable.quit)
        quitAppName.text = "退出应用"
        quit_app.setOnClickListener {
            finish()
        }


        val skinIcon = skin.findViewById<ImageView>(R.id.icon)
        val skinName = skin.findViewById<TextView>(R.id.name)
        skinIcon.setImageResource(R.drawable.skin)
        skinName.text = "换肤"
        skin.setOnClickListener {

        }

        val sourceIcon = source.findViewById<ImageView>(R.id.icon)
        val sourceName = source.findViewById<TextView>(R.id.name)
        sourceIcon.setImageResource(R.drawable.liabrary)
        sourceName.text = "书源"
        source.setOnClickListener {

        }



        info_layout.setOnClickListener {
            ARouter.getInstance().build("/app/login").navigation()
        }

    }

    override fun createPresenter(): MainPresenter? {
        return MainPresenter()
    }

    override fun onLoadBookRecommend(list: List<String>) {

    }

    override fun onGetLoginUser(user: BaseEntity<User>) {
        when (user.code) {
            BaseEntity.ENTITY_FAIL_CODE -> {
                name.text = "请登录"
            }
            BaseEntity.ENTITY_SUCCESS_CODE -> {
                val user = user.data as User
                name.text = user.account
                currentUser = user
            }
        }
    }


    /**
     * @desc pageLoader加载更多数据接口
     */
    override fun onLoadMore(pageIndex: Int, pageSize: Int) {

    }


    override fun onBackPressed() {
        if (!drawLayoutIsOpen) {
            draw_layout.openDrawer(Gravity.LEFT)
        } else {
            draw_layout.closeDrawer(Gravity.LEFT)
        }

    }

    /**
     * @desc pageLoader 刷新数据接口
     */
    override fun onRefresh() {
        Crawler.search("深空彼岸")
                .doOnError {
                    listPageLoader.loadData(responseState = ListPageLoader.DATA_STATE_ERROR)
                }.subscribe {
                    listPageLoader.loadData(it)
                }
    }


    override fun onDestroy() {
        super.onDestroy()
        RxNetworkObserver.unregister()
    }

    override fun couldLoadMore(pageIndex: Int, totalPage: Int): Boolean {
        return pageIndex < totalPage
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!drawLayoutIsOpen) {
            content.dispatchTouchEvent(ev)
            false
        } else {
            ev?.let {
                return if (ev.action == MotionEvent.ACTION_DOWN) {
                    if (it.rawX > ((getScreenWidth() / 6f) * 5).toInt()) {
                        draw_layout.closeDrawer(Gravity.LEFT)
                        false
                    } else {
                        super.dispatchTouchEvent(ev)
                    }
                } else {
                    super.dispatchTouchEvent(ev)
                }
            } ?: run {
                return super.dispatchTouchEvent(ev)
            }
        }
    }


    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun applySkin() {
        initImmersionBar(fitSystem = false)
        if (drawLayoutIsOpen) {
            if (SkinCompatManager.getInstance().curSkinName == "night") {
                shadow_view.alpha = 0.2f
            } else {
                shadow_view.alpha = 0.5f
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        if (baseEntity.code == CODE_FROM_FONTS) {
            EventBus.getDefault().removeStickyEvent(baseEntity)
            val intent = intent
            overridePendingTransition(0, 0)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mPresenter?.getLoginCurrentUser()
    }




    inner class TabAdapter(fm: FragmentManager) :
            FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {

            return fragments!!.get(position)
        }

        override fun getCount(): Int {
            return fragments!!.size()
        }
    }


}