package com.kai.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.kai.base.activity.BaseActivity
import com.kai.base.eventBusEntity.EventBusEntity
import com.kai.base.extension.initImmersionBar
import com.kai.base.mvp.base.BasePresenter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<BasePresenter<AppCompatActivity>>() {
    companion object{
        const val STRING_CODE = 0
    }
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        text_view.setOnClickListener {
            startActivity(Intent(this,TestActivity::class.java))
            postStickyEvent(2324,code = TestActivity.INT_CODE,message = TestActivity::class.java.name)
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