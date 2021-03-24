package com.kai.ui.main

import android.util.Log
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    主页面 - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class MainPresenter: BasePresenter<MainContract.View>() {

    fun loadBookRecommend(){
        Log.e("MainPresenter","loadBookRecommend")
        getView().onLoadBookRecommend()
    }


}