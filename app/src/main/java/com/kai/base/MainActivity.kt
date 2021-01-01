package com.kai.base

import com.kai.base.activity.BaseActivity
import com.kai.base.eventBusEntity.EventBusEntity

class MainActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        postStickyEvent("")
    }


    override fun <String> onMessageReceiver(eventBusEntity: EventBusEntity<String>) {
        super.onMessageReceiver(eventBusEntity)
    }
}