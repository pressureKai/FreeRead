package com.kai.ui.bookdetail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.LAYER_TYPE_SOFTWARE
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Query
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.application.BaseInit
import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.BookChapterListBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.database.BookDatabase
import com.kai.bookpage.page.PageLoader
import com.kai.bookpage.page.PageMode
import com.kai.bookpage.page.PageStyle
import com.kai.bookpage.page.PageView
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.extension.customToast
import com.kai.common.extension.getScreenWidth
import com.kai.common.extension.isContainChinese
import com.kai.common.utils.*
import com.kai.crawler.entity.book.SearchBook
import com.kai.ui.pageLoader.CrawlerPageLoader
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_book_detail.*
import kotlinx.android.synthetic.main.activity_book_detail.draw_content
import kotlinx.android.synthetic.main.activity_book_detail.draw_layout
import kotlinx.android.synthetic.main.activity_book_detail.shadow_view
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.merge_toolbar.*
import skin.support.SkinCompatManager

@Route(path = BaseInit.BOOK)
class BookDetailActivity : BaseMvpActivity<BookDetailContract.View, BookDetailPresenter>(),
    BookDetailContract.View {
    private var mPageLoader: PageLoader? = null
    private var drawLayoutIsOpen = false
    private var mCoolBookBean: CoolBookBean = CoolBookBean()
    private var isRegistered = false
    private val brightnessObserver: ContentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            try {
                this.onChange(selfChange, null)
            } catch (e: Exception) {
                LogUtils.e("BookDetailActivity", "changeBrightness is $e")
            }
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            var isFollowSystem = false
            SharedPreferenceUtils.getInstance()?.let {
                isFollowSystem = it.getBoolean(BRIGHTNESS_FOLLOW_SYSTEM, false)
            }

            if (selfChange || isFollowSystem) {
                return
            }

            // 如果系统亮度改变，则修改当前 Activity 亮度
            if (BRIGHTNESS_MODE_URI == uri) {
                //亮度模式发生改变

            } else if (BRIGHTNESS_URI == uri && !BrightnessUtils.isAutoBrightness(this@BookDetailActivity)) {
                //未开启自动调光
                BrightnessUtils.setBrightness(
                    this@BookDetailActivity,
                    BrightnessUtils.getScreenBrightness(this@BookDetailActivity)
                )

            } else if (BRIGHTNESS_ADJ_URI == uri && BrightnessUtils.isAutoBrightness(this@BookDetailActivity)) {
                //开启自动调光
                BrightnessUtils.setDefaultBrightness(this@BookDetailActivity)
            }
        }
    }

    companion object {
        const val BOOK_DETAIL = 0
        const val BRIGHTNESS_FOLLOW_SYSTEM = "brightness_follow_system"
        private val BRIGHTNESS_MODE_URI =
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE)
        private val BRIGHTNESS_URI = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
        private val BRIGHTNESS_ADJ_URI = Settings.System.getUriFor("screen_auto_brightness_adj")
        const val BRIGHTNESS = "brightness"
    }

    override fun initView() {
        initBaseView()
        initPageView()
        initBroadCastReceiver()
    }

    private fun initBaseView() {
        initImmersionBar(fitSystem = false, color = R.color.app_background, dark = true)
        toolbar.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0)
        back.setOnClickListener {
            finish()
        }
        initMenuList()
        toolbar.visibility = View.GONE
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
                closeSetting()
            }

            override fun onDrawerClosed(drawerView: View) {
                drawLayoutIsOpen = false
                shadow_view.alpha = 0f
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
        translation_layout.setOnClickListener {
            closeSetting()
        }
        chapter_progress.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                seekParams?.let {
                    mPageLoader?.let { pageLoader ->
                        try {
                            val category = pageLoader.getChapterCategory()
                            val bookChapterBean = category[it.progress]
                            current_chapter.text = bookChapterBean.title
                            try {
                                if (order_des.text == resources.getString(R.string.order_up_chapter_list)) {
                                    draw_list.scrollToPosition(it.progress.toInt())
                                } else {
                                    draw_list.scrollToPosition((category.size - 1) - it.progress.toInt())
                                }
                            } catch (e: Exception) {

                            }


                        } catch (e: java.lang.Exception) {
                            LogUtils.e("BookDetailActivity", "seek change error is $e")
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                seekBar?.let {
                    mPageLoader?.let { pageLoader ->
                        try {
                            val category = pageLoader.getChapterCategory()
                            val bookChapterBean = category[it.progress]
                            current_chapter.text = bookChapterBean.title
                            mPresenter?.loadBookContentByChapter(bookChapterBean, true)
                        } catch (e: Exception) {
                            try {
                                val category = pageLoader.getChapterCategory()
                                val bookChapterBean = category[it.progress - 1]
                                current_chapter.text = bookChapterBean.title
                                mPresenter?.loadBookContentByChapter(bookChapterBean, true)
                                LogUtils.e("BookDetailActivity", "stop track error is $e")
                            } catch (e: java.lang.Exception) {
                                customToast(resources.getString(R.string.current_chapter_no_exist))
                            }

                        }
                    }
                }
            }
        }


        SharedPreferenceUtils.getInstance()?.let {
            if (it.getBoolean(BRIGHTNESS_FOLLOW_SYSTEM, false)) {
                BrightnessUtils.setDefaultBrightness(this)
                brightness_progress.isEnabled = false
                brightness_progress.setProgress(BrightnessUtils.getScreenBrightness(this).toFloat())
            } else {
                val defaultBrightness = it.getInt(BRIGHTNESS, 40)
                BrightnessUtils.setBrightness(
                    this@BookDetailActivity,
                    defaultBrightness
                )
                brightness_progress.isEnabled = true
                brightness_progress.setProgress(defaultBrightness.toFloat())
            }
        }

        brightness_progress.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                seekParams?.let {
                    BrightnessUtils.setBrightness(
                        this@BookDetailActivity,
                        it.progress
                    )
                }

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                var auto = true
                SharedPreferenceUtils.getInstance()?.let {
                    auto = !it.getBoolean(BRIGHTNESS_FOLLOW_SYSTEM, false)
                }
                if (!auto) {
                    customToast("亮度跟随系统")
                }
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                seekBar?.let { seek ->
                    SharedPreferenceUtils.getInstance()?.let {
                        it.putInt(BRIGHTNESS, seek.progress.toInt())
                    }
                    BrightnessUtils.setBrightness(
                        this@BookDetailActivity,
                        seek.progress
                    )
                }

            }
        }
        SharedPreferenceUtils.getInstance()?.let {
            val boolean = it.getBoolean(BRIGHTNESS_FOLLOW_SYSTEM, false)
            brightness_follow_system_checkbox.isChecked = boolean
        }


        brightness_follow_system_checkbox.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferenceUtils.getInstance()?.let {
                it.putBoolean(BRIGHTNESS_FOLLOW_SYSTEM, isChecked)
                brightness_follow_system_checkbox.isChecked = isChecked
                brightness_progress.isEnabled = !isChecked
                if (isChecked) {
                    BrightnessUtils.setDefaultBrightness(this)
                    val screenBrightness =
                        BrightnessUtils.getScreenBrightness(this@BookDetailActivity)
                    brightness_progress.setProgress(screenBrightness.toFloat())
                    SharedPreferenceUtils.getInstance()?.let { share ->
                        share.putInt(BRIGHTNESS, screenBrightness)
                    }
                }
            }
        }


        size_progress.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                seekParams?.let {
                    mPageLoader?.let { pageLoader ->
                        pageLoader.setTextSize(it.progress)
                        current_size.text =
                            resources.getString(R.string.book_current_size) + " " + it.progress
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                seekBar?.let {
                    mPageLoader?.let { pageLoader ->
                        pageLoader.getSettingManager()?.setTextSize(it.progress)
                    }
                }
            }

        }


        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        background_list.layoutManager = linearLayoutManager
        background_list.adapter = BackgroundListAdapter()
        val arrayList = ArrayList<PageStyle>()
        arrayList.add(PageStyle.BG_0)
        arrayList.add(PageStyle.BG_1)
        arrayList.add(PageStyle.BG_2)
        arrayList.add(PageStyle.BG_3)
        arrayList.add(PageStyle.BG_4)
        (background_list.adapter as BackgroundListAdapter).setNewInstance(arrayList)
        (background_list.adapter as BackgroundListAdapter).setOnItemClickListener { adapter, _, position ->
            var pageStyle = adapter.data[position] as PageStyle
            if (SkinCompatManager.getInstance().curSkinName == "night") {
                pageStyle = PageStyle.BG_NIGHT
            }
            mPageLoader?.setPageStyle(pageStyle)
            adapter.notifyDataSetChanged()
        }




        simulation.setOnClickListener {
            simulation.setBackgroundResource(R.drawable.read_page_mode_selected)
            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            mPageLoader?.setPageMode(PageMode.SIMULATION)
        }
        cover.setOnClickListener {
            cover.setBackgroundResource(R.drawable.read_page_mode_selected)
            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            mPageLoader?.setPageMode(PageMode.COVER)

        }
        scroll.setOnClickListener {
            scroll.setBackgroundResource(R.drawable.read_page_mode_selected)
            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            mPageLoader?.setPageMode(PageMode.SCROLL)
        }
        slide.setOnClickListener {
            slide.setBackgroundResource(R.drawable.read_page_mode_selected)
            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            mPageLoader?.setPageMode(PageMode.SLIDE)

        }
        none.setOnClickListener {
            none.setBackgroundResource(R.drawable.read_page_mode_selected)
            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
            mPageLoader?.setPageMode(PageMode.NONE)
        }


        order.setOnClickListener {
            val bookMenuAdapter = draw_list.adapter as BookMenuAdapter
            bookMenuAdapter.data.reverse()
            bookMenuAdapter.notifyDataSetChanged()
            val progress = chapter_progress.progress.toInt()
            val size = bookMenuAdapter.data.size - 1
            try {
                if (order_des.text == resources.getString(R.string.order_up_chapter_list)) {
                    order_des.text = resources.getString(R.string.order_down_chapter_list)
                    draw_list.scrollToPosition(size - progress)
                } else {
                    order_des.text = resources.getString(R.string.order_up_chapter_list)
                    draw_list.scrollToPosition(progress)
                }
            } catch (e: Exception) {

            }
        }

    }


    private fun settingAction() {
        if (toolbar.visibility == View.GONE) {
            toolbar.visibility = View.VISIBLE
        } else {
            toolbar.visibility = View.GONE
        }
        if (setting_layout.visibility == View.GONE) {
            setting_layout.visibility = View.VISIBLE
        } else {
            setting_layout.visibility = View.GONE
        }

        if (translation_layout.visibility == View.INVISIBLE) {
            translation_layout.visibility = View.VISIBLE
        } else {
            translation_layout.visibility = View.INVISIBLE
        }
    }

    private fun closeSetting() {
        toolbar.visibility = View.GONE
        setting_layout.visibility = View.GONE
        translation_layout.visibility = View.INVISIBLE
    }


    private fun initMenuList() {
        draw_list.layoutManager = LinearLayoutManager(this)
        draw_list.adapter = BookMenuAdapter()
        (draw_list.adapter as BookMenuAdapter).setOnItemClickListener { adapter, _, position ->
            mPresenter?.loadBookContentByChapter((adapter.data[position] as BookChapterBean), true)
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
                LogUtils.e("BookDetailActivity", "save coolBook is $e")
            }


            var needAddChapter = false
            if (chapters.isNotEmpty()) {
                try {
                    val chapterList =
                        BookDatabase.get().bookDao().getChapterList(chapters.first().bookId)
                    needAddChapter = chapterList.bookChapterList.size != chapters.size
                } catch (e: Exception) {
                    LogUtils.e("BookDetailActivity", "check is same big error is $e")
                }
            }
            if (needAddChapter) {
                for (value in chapters) {
                    try {
                        BookDatabase.get().bookDao().insertBookChapter(value)
                    } catch (e: Exception) {

                    }
                }
            }

            mPageLoader?.refreshChapterList()
            var position = 0
            mPageLoader?.let {
                position = it.mCurrentChapterPosition
            }
            LogUtils.e("BookDetailActivity", "current position is $position")

            mPresenter?.loadBookContentByChapter(
                chapters[position], true
            )
            chapter_progress.min = 0f
            chapter_progress.max = chapters.size.toFloat()
            chapter_progress.setProgress(position.toFloat())
        }
    }

    override fun onLoadBookContentByChapter(bookChapterBean: BookChapterBean, isOpen: Boolean) {
        if (isOpen) {
            if (bookChapterBean.content.isNotEmpty() || !bookChapterBean.link.contains("http")) {
                mPageLoader?.mCurrentChapterPosition = bookChapterBean.position
                mPageLoader?.openChapter()
                draw_layout.closeDrawer(Gravity.LEFT)
                current_chapter.text = bookChapterBean.title
            }
        }
        (draw_list.adapter as BookMenuAdapter).notifyDataSetChanged()
    }


    override fun <T> onMessageReceiver(baseEntity: BaseEntity<T>) {
        super.onMessageReceiver(baseEntity)
        if (baseEntity.code == BOOK_DETAIL) {
            val searchBook = baseEntity.data as SearchBook
            val source = searchBook.sources
            if (source.size > 0) {
                val link = source.first().link
                mPresenter?.loadBookChapter(
                    source = source.first(),
                    link
                )

                mCoolBookBean.bookId = link
                mCoolBookBean.author = searchBook.author
                mCoolBookBean.isUpdate = true
                mCoolBookBean.shortIntro = searchBook.descriptor
                mCoolBookBean.title = searchBook.title
                mCoolBookBean.isLocal = link.contains("http")
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

        paint.post {
            val crawlerPageLoader = CrawlerPageLoader(
                pageView,
                mCoolBookBean
            )
            crawlerPageLoader.reInitTextPaint(paint.paint)
            crawlerPageLoader.reInitTipPaint(tip_paint.paint)
            mPageLoader =
                pageView.getPageLoader(mCoolBookBean, crawlerPageLoader)


            mPageLoader?.let {
                val pageMode = it.getSettingManager()?.getPageMode()
                LogUtils.e("BookDetailActivity","pagemode is ${pageMode}")
                if(pageMode!= null){
                    when (pageMode) {
                        PageMode.SIMULATION -> {
                            simulation.setBackgroundResource(R.drawable.read_page_mode_selected)
                            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                        }
                        PageMode.COVER -> {
                            cover.setBackgroundResource(R.drawable.read_page_mode_selected)
                            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                        }
                        PageMode.SCROLL -> {
                            scroll.setBackgroundResource(R.drawable.read_page_mode_selected)
                            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                        }
                        PageMode.SLIDE -> {
                            slide.setBackgroundResource(R.drawable.read_page_mode_selected)
                            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            none.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                        }
                        PageMode.NONE -> {
                            none.setBackgroundResource(R.drawable.read_page_mode_selected)
                            simulation.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            cover.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            slide.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                            scroll.setBackgroundResource(R.drawable.read_page_mode_un_selected)
                        }
                    }
                }

            }
            setPageStyle()
            mPageLoader?.let {
                current_size.text =
                    resources.getString(R.string.book_current_size) + " " + it.getTextSize()
                size_progress.setProgress(it.getTextSize().toFloat())
            }




            mPageLoader?.setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
                override fun onChapterChange(pos: Int) {
                    mPageLoader?.let {
                        current_chapter.text = it.getChapterCategory()[pos].title
                        chapter_progress.setProgress(it.getChapterCategory()[pos].position.toFloat())
                    }
                }

                override fun requestChapters(requestChapters: List<BookChapterBean>) {

                }

                override fun onCategoryFinish(chapter: List<BookChapterBean>) {

                }

                override fun onPageCountChange(count: Int) {

                }

                override fun onPageChange(pos: Int) {

                }

                override fun onPreLoadChapter(mCurrentChapterPosition: Int, loadSize: Int) {
                    //预加载数据前后指定章节数量的数据
                    val chapterCategory = mPageLoader?.getChapterCategory()
                    chapterCategory?.let {
                        for (index in (mCurrentChapterPosition - loadSize).until(
                            mCurrentChapterPosition
                        )) {
                            if (index >= 0) {
                                val bookChapterBean = it[index]
                                mPresenter?.loadBookContentByChapter(bookChapterBean, false)
                            }
                        }


                        for (index in mCurrentChapterPosition.until(mCurrentChapterPosition + 3)) {
                            if (index < it.size) {
                                val bookChapterBean = it[index]
                                mPresenter?.loadBookContentByChapter(bookChapterBean, false)
                            }
                        }
                    }

                }
            })

        }



        pageView.setTouchListener(object : PageView.TouchListener {
            override fun onTouch(): Boolean {
                return true
            }

            override fun center() {
                Handler(Looper.getMainLooper()).postDelayed({
                    settingAction()
                }, 20)
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
            Handler(Looper.getMainLooper()).postDelayed({
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra("level", 0)
                    mPageLoader?.updateBattery(level)
                } else if (intent.action == Intent.ACTION_TIME_TICK) {
                    mPageLoader?.updateTime()
                }
            }, 10)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unRegisterBrightnessObserver()
        unregisterReceiver(mReceiver)
    }


    override fun onBackPressed() {
        if (!drawLayoutIsOpen) {
            draw_layout.openDrawer(Gravity.LEFT)
            customToast(resources.getString(R.string.quit_read_page))
        } else {
            finish()
        }
    }


    class BookMenuAdapter : BaseQuickAdapter<BookChapterBean, BaseViewHolder>(R.layout.item_menu) {
        override fun convert(holder: BaseViewHolder, item: BookChapterBean) {
            try {
                val chapterName = holder.getView<TextView>(R.id.chapter_name)
                val bookChapterById = BookDatabase.get().bookDao().getBookChapterById(item.id)
                if (bookChapterById.content.isEmpty()) {
                    if (SkinCompatManager.getInstance().curSkinName == "night") {
                        chapterName.setTextColor(context.resources.getColor(R.color.app_font_color_night))
                    } else {
                        chapterName.setTextColor(context.resources.getColor(R.color.app_book_color))
                    }

                } else {
                    if (SkinCompatManager.getInstance().curSkinName == "night") {
                        chapterName.setTextColor(context.resources.getColor(R.color.app_book_color_night))
                    } else {
                        chapterName.setTextColor(context.resources.getColor(R.color.app_font_color))
                    }

                }
                chapterName.text = item.title
            } catch (e: Exception) {

                LogUtils.e("BookMenuAdapter", "error is $e")
            }

        }

    }

    override fun createPresenter(): BookDetailPresenter? {
        return BookDetailPresenter()
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!drawLayoutIsOpen) {
            if (toolbar.visibility != View.GONE) {
                function_content.dispatchTouchEvent(ev)
            } else {
                pageView.dispatchTouchEvent(ev)
            }

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


    private fun registerBrightnessObserver() {
        try {
            if (brightnessObserver != null) {
                if (!isRegistered) {
                    contentResolver.unregisterContentObserver(brightnessObserver)
                    contentResolver.registerContentObserver(
                        BRIGHTNESS_MODE_URI,
                        false,
                        brightnessObserver
                    )
                    contentResolver.registerContentObserver(
                        BRIGHTNESS_ADJ_URI,
                        false,
                        brightnessObserver
                    )
                    contentResolver.registerContentObserver(
                        BRIGHTNESS_URI,
                        false,
                        brightnessObserver
                    )
                    isRegistered = true
                }
            }
        } catch (e: java.lang.Exception) {
            LogUtils.e("BookDetailActivity", "registerBrightnessObserver error is $e")
        }
    }


    private fun unRegisterBrightnessObserver() {
        try {
            if (brightnessObserver != null) {
                if (isRegistered) {
                    contentResolver.unregisterContentObserver(brightnessObserver)
                    isRegistered = false
                }
            }
        } catch (e: java.lang.Exception) {
            LogUtils.e("BookDetailActivity", "book detail error is $e")
        }
    }


    override fun onStart() {
        super.onStart()
        registerBrightnessObserver()
    }


    override fun onResume() {
        super.onResume()

        setPageStyle()
    }

    private fun setPageStyle() {
        mPageLoader?.let {
            var pageStyle = it.getSettingManager()?.getPageStyle()
            if (SkinCompatManager.getInstance().curSkinName == "night") {
                pageStyle = PageStyle.BG_NIGHT
            }

            pageStyle?.let { style ->
                Handler(Looper.getMainLooper()).postDelayed({
                    it.setPageStyle(style)
                }, 100)
            }
        }
    }

    inner class BackgroundListAdapter :
        BaseQuickAdapter<PageStyle, BaseViewHolder>(R.layout.item_book_background) {
        override fun convert(holder: BaseViewHolder, item: PageStyle) {
            val view = holder.getView<CardView>(R.id.back)
            val root = holder.getView<ConstraintLayout>(R.id.root)
            mPageLoader?.let {
                val pageStyle = it.getSettingManager()?.getPageStyle()
                pageStyle?.let { style ->
                    if (style.bgColor == item.bgColor) {
                        root.setBackgroundResource(R.drawable.read_setting_background_selected)
                    } else {
                        root.setBackgroundDrawable(null)
                    }
                }
            }
            view.setCardBackgroundColor(context.resources.getColor(item.bgColor))
        }
    }

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onPause() {
        super.onPause()
        mPageLoader?.saveRecord()
    }
}