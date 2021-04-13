package com.kai.ui.main

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.appcompat.widget.SwitchCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.widget.load.ChargeLoadMoreListener
import com.kai.base.widget.load.PageLoader
import com.kai.base.widget.load.RefreshDataListener
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.getScreenWidth
import com.kai.common.utils.RxNetworkObserver
import com.kai.common.utils.ScreenUtils
import com.kai.common.utils.SharedPreferenceUtils
import com.kai.crawler.Crawler
import com.kai.crawler.entity.book.SearchBook
import com.kai.entity.User
import com.kai.ui.bookdetail.BookDetailActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.rxjava3.core.Observable
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

    private lateinit var pageLoader: PageLoader<SearchBook>
    private var drawLayoutIsOpen = false
    private var isDay = true
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        RxNetworkObserver.register(this)
        mPresenter?.loadBookRecommend()
        mPresenter?.getLoginCurrentUser()
        initImmersionBar(fitSystem = false, color = R.color.app_background)
        val testBaseQuickAdapter = TestBaseQuickAdapter()
        testBaseQuickAdapter.setOnItemClickListener { adapter, _, position ->
            val searchBook = adapter.data[position] as SearchBook
            ARouter.getInstance().build("/app/book").navigation()
            postStickyEvent(searchBook,
                    BookDetailActivity.BOOK_DETAIL,
                    BookDetailActivity::class.java.name)
        }
        pageLoader = PageLoader(
                recycler,
                refreshDataDelegate = this,
                chargeLoadMoreListener = this,
                mSmartRefreshLayout = refresh,
                mMultipleStatusView = status,
                mAdapter = testBaseQuickAdapter
        )


        val layoutParams = refresh.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = ScreenUtils.getStatusBarHeight()
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

        }


        val fontIcon = font.findViewById<ImageView>(R.id.icon)
        val fontName = font.findViewById<TextView>(R.id.name)
        fontIcon.setImageResource(R.drawable.font)
        fontName.text = "全局字体"
        font.setOnClickListener {
            ARouter.getInstance().build("/app/fonts").navigation()
        }


        val unRegisterIcon = un_register.findViewById<ImageView>(R.id.icon)
        val unRegisterName = un_register.findViewById<TextView>(R.id.name)
        unRegisterIcon.setImageResource(R.drawable.un_register)
        unRegisterName.text = "注销账号"
        un_register.setOnClickListener {

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

    override fun onGetLoginUser(user: Observable<User>) {
        user
                .doOnError {
                    name.text = "请登录 $it"
                }
                .subscribe {
                    name.text = it.account
                }
    }


    inner class TestBaseQuickAdapter :
            BaseQuickAdapter<SearchBook, BaseViewHolder>(R.layout.item_main_test) {
        override fun convert(holder: BaseViewHolder, item: SearchBook) {
            holder.getView<TextView>(R.id.title).text = item.title.replace(" ", "").trim()
            holder.getView<TextView>(R.id.des).text = item.descriptor.replace(" ", "").trim()
            holder.getView<TextView>(R.id.author).text = item.author.replace(" ", "").trim()
            val view = holder.getView<View>(R.id.divider)

            val itemPosition = getItemPosition(item)
            if (itemPosition == data.size - 1) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
            }

            val options = RequestOptions()
                    .transform(MultiTransformation(CenterCrop(), RoundedCorners(16)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            val cover = holder.getView<ImageView>(R.id.cover)
            Glide.with(cover).load(item.cover).apply(options).into(cover)
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
        Crawler.search("罗")
                .doOnError {
                    pageLoader.loadData(responseState = PageLoader.DATA_STATE_ERROR)
                }.subscribe {
                    pageLoader.loadData(it)
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
            refresh.dispatchTouchEvent(ev)
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
}