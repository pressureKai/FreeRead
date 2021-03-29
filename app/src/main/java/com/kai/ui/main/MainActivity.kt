package com.kai.ui.main

import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.common.extension.initImmersionBar
import com.kai.crawler.Crawler
import kotlinx.android.synthetic.main.activity_main.*

/**
 * des 书籍主页面
 */
class MainActivity :BaseMvpActivity<MainContract.View,MainPresenter>(), MainContract.View{
    companion object{
        const val INT_CODE = 0
    }
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        showErrorView()
        mPresenter?.loadBookRecommend()
        initImmersionBar(fitSystem = true)
        checkNetworkState()
        Thread{
            Crawler.search("罗")
        }.start()
    }
    override fun createPresenter(): MainPresenter? {
        return MainPresenter()
    }

    override fun onLoadBookRecommend(list: List<String>) {
        showContent()
        book_recommend.text = list.last()
    }
}