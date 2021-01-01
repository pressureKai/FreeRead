package com.kai.base

import com.kai.base.activity.BaseActivity

class MainActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_main
    }
}