package com.kai.base

import android.content.Intent
import com.kai.base.activity.BaseActivity
import com.kai.base.eventBusEntity.EventBusEntity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    companion object{
        const val STRING_CODE = 0
    }
    override fun setContentView(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        text_view.setOnClickListener {
            startActivity(Intent(this,TestActivity::class.java))
            postStickyEvent(0,code = TestActivity.INT_CODE,message = TestActivity::class.java.name)
        }
    }


    override fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>) {
        super.onMessageReceiver(eventBusEntity)
        runOnUiThread {
            if(eventBusEntity.code == STRING_CODE){
                text_view.text = eventBusEntity.data.toString()
            }
        }
    }
}