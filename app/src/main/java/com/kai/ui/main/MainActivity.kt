package com.kai.ui.main

import android.os.Handler
import android.os.Looper
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
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.widget.load.ChargeLoadMoreListener
import com.kai.base.widget.load.PageLoader
import com.kai.base.widget.load.RefreshDataListener
import com.kai.common.extension.getScreenWidth
import com.kai.common.utils.RxNetworkObserver
import com.kai.common.utils.ScreenUtils
import com.kai.common.utils.SharedPreferenceUtils
import kotlinx.android.synthetic.main.activity_main.*
import skin.support.SkinCompatManager
import skin.support.widget.SkinCompatSupportable


/**
 * des 书籍主页面
 */
class MainActivity : BaseMvpActivity<MainContract.View, MainPresenter>(), MainContract.View,
    RefreshDataListener, ChargeLoadMoreListener ,SkinCompatSupportable{
    companion object {
        const val INT_CODE = 0
        const val IS_DAY = "is_day"
    }

    private lateinit var pageLoader: PageLoader<String>
    private var drawLayoutIsOpen = false
    private var isDay = true
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        RxNetworkObserver.register(this)
        mPresenter?.loadBookRecommend()
        initImmersionBar(fitSystem = false,color = R.color.app_background)
        pageLoader = PageLoader(
            recycler,
            refreshDataDelegate = this,
            chargeLoadMoreListener = this,
            mSmartRefreshLayout = refresh,
            mMultipleStatusView = status,
            mAdapter = TestBaseQuickAdapter()
        )


        val layoutParams = refresh.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = ScreenUtils.getStatusBarHeight()
        draw_content.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0)
        draw_content.layoutParams.width = ((getScreenWidth() / 6f) * 5).toInt()
        draw_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if(SkinCompatManager.getInstance().curSkinName != "night"){
                    val d = (0.5 * slideOffset).toFloat()
                    shadow_view.alpha = d
                } else {
                    val d = (0.2 * slideOffset).toFloat()
                    shadow_view.alpha = d
                }

            }

            override fun onDrawerOpened(drawerView: View) {
                if(SkinCompatManager.getInstance().curSkinName != "night"){
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


                  if(isDay){
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

        SkinCompatManager.getInstance().curSkinName



        val sourceIcon = source.findViewById<ImageView>(R.id.icon)
        val sourceName = source.findViewById<TextView>(R.id.name)
        sourceIcon.setImageResource(R.drawable.liabrary)
        sourceName.text = "书源"
        source.setOnClickListener {

        }



//        Thread{
//            var isRun = false
//            Crawler.search("罗").subscribe {
//                for(SL in it.first().sources){
//                    if(!isRun){
//                        Crawler.catalog(SL).subscribe { chapters ->
//                            if(!isRun){
//                                isRun = true
//                                Crawler.content(SL,chapters.first().link)
//                            }
//                        }
//                    }
//                }
//            }
//        }.start()
    }

    override fun createPresenter(): MainPresenter? {
        return MainPresenter()
    }

    override fun onLoadBookRecommend(list: List<String>) {

    }


    inner class TestBaseQuickAdapter :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_main_test) {
        override fun convert(holder: BaseViewHolder, item: String) {
            holder.getView<TextView>(R.id.test).text = item
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
        val source = ArrayList<String>()
        for (index in 0.until(120)) {
            source.add(index.toString())
        }
        Handler(Looper.getMainLooper()).postDelayed({
            pageLoader.loadData()
        }, 2000)
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
                return if(ev.action == MotionEvent.ACTION_DOWN){
                    if(it.rawX > ((getScreenWidth() / 6f) * 5).toInt()){
                        draw_layout.closeDrawer(Gravity.LEFT)
                        false
                    }else {
                        super.dispatchTouchEvent(ev)
                    }
                } else {
                    super.dispatchTouchEvent(ev)
                }
            }?:run {
               return  super.dispatchTouchEvent(ev)
            }

        }
    }


    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun applySkin() {
        initImmersionBar(fitSystem = false)
    }
}