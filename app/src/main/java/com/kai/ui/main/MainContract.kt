package com.kai.ui.main

import com.kai.base.mvp.base.IView

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class MainContract {
    interface View :IView{
        fun onLoadBookRecommend()
    }
}