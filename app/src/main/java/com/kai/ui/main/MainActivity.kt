package com.kai.ui.main

import android.util.Log
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.eventBusEntity.EventBusEntity
import com.kai.common.extension.initImmersionBar

class MainActivity :BaseMvpActivity<MainContract.View,MainPresenter>(), MainContract.View{
    companion object{
        const val INT_CODE = 0
    }
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        mPresenter?.loadBookRecommend()
        initImmersionBar(fitSystem = true)
    }

    override fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>) {
        super.onMessageReceiver(eventBusEntity)
    }

    override fun createPresenter(): MainPresenter? {
        return MainPresenter()
    }

    override fun onLoadBookRecommend() {
        Log.e("MainPresenter","onLoadBookRecommend")
    }
}