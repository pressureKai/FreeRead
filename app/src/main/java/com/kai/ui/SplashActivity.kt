package com.kai.ui

import android.animation.Animator
import android.content.Intent
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.listener.CustomAnimatorListener
import com.kai.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseMvpActivity<IView,BasePresenter<IView>>() {
    private var isClickSkip = false
    override fun setLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        initImmersionBar(fitSystem = true)
        splash_animation.addAnimatorListener(object  :CustomAnimatorListener(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                super.onAnimationEnd(animation)
                if(!isClickSkip){
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        })
        skip.setOnClickListener {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            isClickSkip = true
            finish()
        }
    }
}