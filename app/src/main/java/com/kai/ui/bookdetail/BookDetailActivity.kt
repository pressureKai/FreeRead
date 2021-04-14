package com.kai.ui.bookdetail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View.LAYER_TYPE_SOFTWARE
import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.TextChapter
import com.kai.bookpage.page.PageLoader
import com.kai.bookpage.page.PageView
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.crawler.entity.book.SearchBook
import kotlinx.android.synthetic.main.activity_book_detail.*


@Route(path = "/app/book")
class BookDetailActivity : BaseMvpActivity<BookDetailContract.View, BookDetailPresenter>()
        , BookDetailContract.View {
    private var mPageLoader: PageLoader?= null
    companion object {
        const val BOOK_DETAIL = 0
    }

    override fun initView() {
        initImmersionBar(fitSystem = false)
        initPageView()
        initBroadCastReceiver()

    }

    override fun setLayoutId(): Int {
        return R.layout.activity_book_detail
    }

    override fun onLoadBookDetail(list: List<String>) {

    }


    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        if (baseEntity.code == BOOK_DETAIL) {
            val searchBook = baseEntity.data as SearchBook
            val coolBookBean = CoolBookBean()
            coolBookBean.id = "1"
            coolBookBean.author = "me"
            coolBookBean.chapterCount = 30
            coolBookBean.cover = "https://www.baidu.com"
            coolBookBean.isUpdate = true
            coolBookBean.isLocal = false
            coolBookBean.title = "百度"
            mPageLoader = pageView.getPageLoader(coolBookBean)

//            Crawler.catalog(searchBook.sources.first()).subscribe { chapters ->
//                Crawler.content(searchBook.sources.first(), chapters.first().link)
//            }
        }
    }


    /**
      * #  初始化pageView 相关设置
      * @date 2021/4/14
      */
    private fun initPageView(){
        // 如果 API < 18 取消硬件加速
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            pageView.setLayerType(LAYER_TYPE_SOFTWARE, null)
        }


        mPageLoader?.setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
            override fun onChapterChange(pos: Int) {

            }

            override fun requestChapters(requestChapters: List<TextChapter>) {

            }

            override fun onCategoryFinish(chapter: List<TextChapter>) {

            }

            override fun onPageCountChange(count: Int) {

            }

            override fun onPageChange(pos: Int) {

            }

        })


        pageView.setTouchListener(object : PageView.TouchListener {
            override fun onTouch(): Boolean {
                return true
            }

            override fun center() {

            }

            override fun prePage() {

            }

            override fun nextPage() {

            }

            override fun cancel() {

            }

        })
    }

    /**
      * #  初始化时间与电量的广播注册
      */
    private fun initBroadCastReceiver(){
        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        registerReceiver(mReceiver, intentFilter)
    }


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra("level", 0)
                mPageLoader?.updateBattery(level)
            } else if (intent.action == Intent.ACTION_TIME_TICK) {
                mPageLoader?.updateTime()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

}