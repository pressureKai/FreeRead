package com.kai.ui.main

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.widget.load.ChargeLoadMoreListener
import com.kai.base.widget.load.PageLoader
import com.kai.base.widget.load.RefreshDataListener
import com.kai.common.extension.initImmersionBar
import com.kai.common.utils.LogUtils
import com.kai.common.utils.RxNetworkObserver
import kotlinx.android.synthetic.main.activity_main.*

/**
 * des 书籍主页面
 */
class MainActivity : BaseMvpActivity<MainContract.View, MainPresenter>(), MainContract.View, RefreshDataListener, ChargeLoadMoreListener {
    companion object {
        const val INT_CODE = 0
    }

    private lateinit var pageLoader: PageLoader<String>
    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        RxNetworkObserver.register(this)
        mPresenter?.loadBookRecommend()
        initImmersionBar(fitSystem = true)
        pageLoader = PageLoader(recycler,
                refreshDataDelegate = this,
                chargeLoadMoreListener = this,
                mSmartRefreshLayout = refresh,
                mMultipleStatusView = status,
                mAdapter = TestBaseQuickAdapter())
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
        book_recommend.text = list.last()
    }


    inner class TestBaseQuickAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_main_test) {
        override fun convert(holder: BaseViewHolder, item: String) {
            holder.getView<TextView>(R.id.test).text = item
        }
    }

    /**
     * @desc pageLoader加载更多数据接口
     */
    override fun onLoadMore(pageIndex: Int, pageSize: Int) {

    }

    /**
     * @desc pageLoader 刷新数据接口
     */
    override fun onRefresh() {
        val source = ArrayList<String>()
        for (index in 0.until(120)) {
            source.add(index.toString())
        }
        Handler(Looper.getMainLooper()).postDelayed({
            pageLoader.loadData()
        }, 2000)
    }


    override fun onDestroy() {
        super.onDestroy()
        RxNetworkObserver.unregister()
    }

    override fun couldLoadMore(pageIndex: Int, totalPage: Int): Boolean {
        return pageIndex < totalPage
    }

}