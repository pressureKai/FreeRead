package com.kai.ui

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.listener.CustomAnimatorListener
import com.kai.common.utils.LogUtils
import com.kai.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*
import java.lang.Exception

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