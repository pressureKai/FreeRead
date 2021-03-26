package com.kai.ui

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.eventBusEntity.EventBusEntity
import com.kai.common.extension.initImmersionBar
import com.kai.common.listener.CustomAnimatorListener
import com.kai.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseMvpActivity<IView,BasePresenter<IView>>() {
    override fun setLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        initImmersionBar(fitSystem = true)
        splash_animation.addAnimatorListener(object  :CustomAnimatorListener(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                super.onAnimationEnd(animation)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }
}