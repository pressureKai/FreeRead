package com.kai.ui.main

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.widget.load.PageLoader
import com.kai.common.extension.initImmersionBar
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
      //  showErrorView()
        mPresenter?.loadBookRecommend()
        initImmersionBar(fitSystem = true)
        checkNetworkState()
        val pageLoader = PageLoader(recycler, mSmartRefreshLayout = refresh,mAdapter = TestBaseQuickAdapter())
        val source = ArrayList<String>()
        for(index in 0.until(120)){
            source.add(index.toString())
        }
        pageLoader.loadNewData(source)
//        Thread{
//            var isRun = false
//            Crawler.search("罗").subscribe {
//                for(SL in it.first().sources){
//                    if(!isRun){
//                        Crawler.catalog(SL).subscribe { chapters ->
//                            if(!isRun){
//                                isRun = true
//                                Crawler.content(SL,chapters.first().link)
//                            }
//                        }
//                    }
//                }
//            }
//        }.start()
    }
    override fun createPresenter(): MainPresenter? {
        return MainPresenter()
    }

    override fun onLoadBookRecommend(list: List<String>) {
        showContent()
        book_recommend.text = list.last()
    }



    inner class TestBaseQuickAdapter: BaseQuickAdapter<String,BaseViewHolder>(R.layout.item_main_test){
        override fun convert(holder: BaseViewHolder, item: String) {
            holder.getView<TextView>(R.id.test).text = item
        }
    }

}