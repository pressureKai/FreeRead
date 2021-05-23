package com.kai.ui.wifi

import android.text.TextUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.appbar.AppBarLayout
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.extension.getScreenWidth
import com.kai.util.NetworkUtils
import com.kai.wifitransfer.Defaults
import com.kai.wifitransfer.ServerRunner
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.android.synthetic.main.activity_wifi.appBar
import kotlinx.android.synthetic.main.activity_wifi.back_layout
import kotlinx.android.synthetic.main.activity_wifi.toolbar
import kotlin.math.abs

@Route(path = "/app/wifi")
class WifiActivity : BaseMvpActivity<IView, BasePresenter<IView>>() {
    override fun initView() {
        initImmersionBar(fitSystem = false)
        val wifiIp = NetworkUtils.getConnectWifiIp(this@WifiActivity)
        if (wifiIp!=null && !TextUtils.isEmpty(wifiIp)) {
            address.text = "http://" + NetworkUtils.getConnectWifiIp(this@WifiActivity)
                .toString() + ":" + Defaults.getPort()
            // 启动wifi传书服务器
            ServerRunner.startServer()
        }


        wifi_animation.layoutParams.height = getScreenWidth()
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->

            val alpha = if (verticalOffset != 0) {
                abs(verticalOffset).toFloat() / (appBar.height - toolbar.height).toFloat()
            } else {
                0f
            }
            back_layout.alpha = alpha
            toolbar.alpha = alpha
        })
        toolbar_title.text = resources.getString(R.string.wifi_transmission)
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_wifi
    }


    override fun onDestroy() {
        super.onDestroy()
        ServerRunner.stopServer()
    }
}