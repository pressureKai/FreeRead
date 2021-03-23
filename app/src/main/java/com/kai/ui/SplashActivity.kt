package com.kai.ui

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.common.eventBusEntity.EventBusEntity
import com.kai.common.extension.initImmersionBar
import com.kai.common.listener.CustomAnimatorListener
import kotlinx.android.synthetic.main.activity_main.*

class SplashActivity : BaseMvpActivity<BasePresenter<AppCompatActivity>>() {
    companion object{
        const val STRING_CODE = 0
    }
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        initImmersionBar(fitSystem = true)
        splash_animation.addAnimatorListener(object  :CustomAnimatorListener(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                super.onAnimationEnd(animation)
                val intent = Intent(this@SplashActivity, TestActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }


    override fun <T> onMessageReceiver(eventBusEntity: EventBusEntity<T>) {
        super.onMessageReceiver(eventBusEntity)
    }
}