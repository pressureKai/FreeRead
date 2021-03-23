package com.kai.ui.main

import androidx.appcompat.app.AppCompatActivity
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.common.eventBusEntity.EventBusEntity
import com.kai.common.extension.initImmersionBar

class MainActivity :BaseMvpActivity<BasePresenter<AppCompatActivity>>() {
    companion object{
        const val INT_CODE = 0
    }
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        initImmersionBar(fitSystem = true)
    }

    override fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>) {
        super.onMessageReceiver(eventBusEntity)
    }
}