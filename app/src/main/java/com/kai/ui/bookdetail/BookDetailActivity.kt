package com.kai.ui.bookdetail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.LAYER_TYPE_SOFTWARE
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.database.BookDatabase
import com.kai.bookpage.page.PageLoader
import com.kai.bookpage.page.PageView
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.getScreenWidth
import com.kai.common.utils.ScreenUtils
import com.kai.crawler.entity.book.SearchBook
import com.kai.ui.pageLoader.CrawlerPageLoader
import kotlinx.android.synthetic.main.activity_book_detail.*
import kotlinx.android.synthetic.main.activity_book_detail.draw_content
import kotlinx.android.synthetic.main.activity_book_detail.draw_layout
import kotlinx.android.synthetic.main.activity_book_detail.shadow_view
import skin.support.SkinCompatManager
import java.lang.Exception


@Route(path = "/app/book")
class BookDetailActivity : BaseMvpActivity<BookDetailContract.View, BookDetailPresenter>(),
    BookDetailContract.View {
    private var mPageLoader: PageLoader? = null
    private var isDrawOpen = false
    private var isCollected = false
    private var drawLayoutIsOpen = false
    private var mCoolBookBean: CoolBookBean = CoolBookBean()


    companion object {
        const val BOOK_DETAIL = 0
    }

    override fun initView() {
        initBaseView()
        initPageView()
        initBroadCastReceiver()
    }

    private fun initBaseView() {
        initImmersionBar(fitSystem = false, color = R.color.app_background)
        draw_content.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0)
        draw_content.layoutParams.width = ((getScreenWidth() / 6f) * 5).toInt()
        draw_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (SkinCompatManager.getInstance().curSkinName != "night") {
                    val d = (0.5 * slideOffset).toFloat()
                    shadow_view.alpha = d
                } else {
                    val d = (0.2 * slideOffset).toFloat()
                    shadow_view.alpha = d
                }

            }

            override fun onDrawerOpened(drawerView: View) {
                if (SkinCompatManager.getInstance().curSkinName != "night") {
                    shadow_view.alpha = 0.5f
                } else {
                    shadow_view.alpha = 0.2f
                }
                drawLayoutIsOpen = true

            }

            override fun onDrawerClosed(drawerView: View) {
                drawLayoutIsOpen = false
                shadow_view.alpha = 0f
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
        initMenuList()
    }

    private fun initMenuList() {
        draw_list.layoutManager = LinearLayoutManager(this)
        draw_list.adapter = BookMenuAdapter()
        (draw_list.adapter as BookMenuAdapter).setOnItemClickListener { adapter, _, position ->
            mPresenter?.loadBookContentByChapter((adapter.data[position] as BookChapterBean))
        }
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_book_detail
    }

    override fun onLoadBookDetail(list: List<String>) {

    }

    override fun onLoadBookChapter(chapters: ArrayList<BookChapterBean>) {
        if (chapters.size > 0) {
            mCoolBookBean.chapterCount = chapters.size
            mCoolBookBean.bookId = chapters.first().bookId
            (draw_list.adapter as BookMenuAdapter).setNewInstance(chapters)
            try {
                BookDatabase.get().bookDao().insertCoolBook(mCoolBookBean)
            } catch (e: Exception) {
                BookDatabase.get().bookDao().updateCoolBook(mCoolBookBean)
            }

            for (value in chapters) {
                try {
                    BookDatabase.get().bookDao().insertBookChapter(value)
                } catch (e: Exception) {
                    BookDatabase.get().bookDao().updateBookChapter(value)
                }
            }
            mPageLoader?.refreshChapterList()
        }
    }

    override fun onLoadBookContentByChapter(bookChapterBean: BookChapterBean) {
        if(bookChapterBean.content.isNotEmpty()){
            mPageLoader?.openChapter()
        }
    }


    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        if (baseEntity.code == BOOK_DETAIL) {
            val searchBook = baseEntity.data as SearchBook

            val source = searchBook.sources
            if (source.size > 0) {
                mPresenter?.loadBookChapter(
                    source = source.first(),
                    (source.first().link + searchBook.title).hashCode()
                )
                mCoolBookBean.author = searchBook.author
                mCoolBookBean.isUpdate = true
                mCoolBookBean.shortIntro = searchBook.descriptor
                mCoolBookBean.title = searchBook.title
                mCoolBookBean.isLocal = true
                mCoolBookBean.updated = System.currentTimeMillis().toString()
                mCoolBookBean.lastRead = System.currentTimeMillis().toString()
            }
        }
    }


    /**
     * #  初始化pageView 相关设置
     * @date 2021/4/14
     */
    private fun initPageView() {
        // 如果 API < 18 取消硬件加速
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        ) {
            pageView.setLayerType(LAYER_TYPE_SOFTWARE, null)
        }


        mPageLoader =
            pageView.getPageLoader(mCoolBookBean, CrawlerPageLoader(pageView, mCoolBookBean))
        mPageLoader?.setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
            override fun onChapterChange(pos: Int) {
                 Log.e("BookDetailActivity","onChapter change position is $pos")
            }

            override fun requestChapters(requestChapters: List<BookChapterBean>) {

            }

            override fun onCategoryFinish(chapter: List<BookChapterBean>) {

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
    private fun initBroadCastReceiver() {
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


    override fun onBackPressed() {
        if (!drawLayoutIsOpen) {
            draw_layout.openDrawer(Gravity.LEFT)
        } else {
            finish()
        }
    }


    class BookMenuAdapter : BaseQuickAdapter<BookChapterBean, BaseViewHolder>(R.layout.item_menu) {
        override fun convert(holder: BaseViewHolder, item: BookChapterBean) {
            val chapterName = holder.getView<TextView>(R.id.chapter_name)
            chapterName.text = item.title
        }

    }

    override fun createPresenter(): BookDetailPresenter? {
        return BookDetailPresenter()
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!drawLayoutIsOpen) {
            pageView.dispatchTouchEvent(ev)
            false
        } else {
            ev?.let {
                return if (ev.action == MotionEvent.ACTION_DOWN) {
                    if (it.rawX > ((getScreenWidth() / 6f) * 5).toInt()) {
                        draw_layout.closeDrawer(Gravity.LEFT)
                        false
                    } else {
                        super.dispatchTouchEvent(ev)
                    }
                } else {
                    super.dispatchTouchEvent(ev)
                }
            } ?: run {
                return super.dispatchTouchEvent(ev)
            }

        }
    }
}