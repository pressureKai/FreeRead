package com.kai.bookpage.page

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.kai.bookpage.model.BookRecordBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.TextChapter
import com.kai.bookpage.model.TextPage
import com.kai.common.utils.ScreenUtils
import io.reactivex.rxjava3.disposables.Disposable
import java.io.BufferedReader

abstract class PageLoader {
    private val TAG = "PageLoader"

    var STATUS_LOADING = 1
    var STATUS_FINISH = 2
    var STATUS_ERROR = 3
    var STATUS_EMPTY = 4
    var STATUS_PARING = 5
    var STATUS_PARSE_ERROR = 6
    var STATUS_CATEGORY_EMPTY = 7

    private var DEFAULT_MARGIN_HEIGHT = 28
    private var DEFAULT_MARGIN_WIDTH = 15
    private var DEFAULT_TIP_SIZE = 12
    private var EXTRA_TITLE_SIZE = 4


    private var mChapterList: ArrayList<TextChapter> = ArrayList()
    private var mCoolBook: CoolBookBean? = null
    private var mPageChangeListener: OnPageChangeListener? = null
    private var mContext: Context? = null
    private var mCurPage: TextPage? = null
    private var mPrePageList: ArrayList<TextPage> = ArrayList()
    private var mCurPageList: ArrayList<TextPage> = ArrayList()
    private var mNextPageList: ArrayList<TextPage> = ArrayList()


    private var mPageView: PageView? = null


    private var mBatteryPaint: Paint? = null
    private var mTipPaint: Paint? = null
    private var mTitlePaint: Paint? = null
    private var mBgPaint: Paint? = null
    private var mTextPaint: Paint? = null


    private var mSettingManager: ReadSettingManager? = null

    private var mCancelPage: TextPage? = null
    private var mBookRecord: BookRecordBean? = null

    private var mPreLoadDisposable: Disposable? = null

    open var mStatus = STATUS_LOADING
    open var isChapterListPrepare = false


    private var isChapterOpen = false
    private var isFirstOpen = true

    private var isClose = false
    private var mPageMode: PageMode? = null
    private var mPageStyle: PageStyle? = null

    private var isNightMode = false
    private var mVisibleWidth = 0
    private var mVisibleHeight = 0


    private var mMarginWidth = 0
    private var mMarginHeight = 0

    private var mDisplayWidth = 0
    private var mDisplayHeight = 0


    private var mTextColor = 0
    private var mTitleSize = 0


    private var mTextSize = 0
    private var mTextInterval = 0

    private var mTitleInterval = 0

    //段落距离（基于行间距的额外距离）
    private var mTextPara = 0
    private var mTitlePara = 0

    private var mBatteryLevel = 0
    private var mBgColor = 0

    private var mCurrentChapterPosition = 0
    private var mLastChapterPosition = 0


    constructor(pageView: PageView, coolBookBean: CoolBookBean) {
        mPageView = pageView
        mContext = pageView.context
        mCoolBook = coolBookBean
        mChapterList = ArrayList<TextChapter>(1)



        initData()
        initPaint()
        initPageView()
        prepareBook()
    }


    //初始化数据
    private fun initData() {
        mSettingManager = ReadSettingManager.getInstance()

        mPageMode = mSettingManager?.getPageMode()
        mPageStyle = mSettingManager?.getPageStyle()


        mMarginWidth = ScreenUtils.dpToPx(DEFAULT_MARGIN_WIDTH)
        mMarginHeight = ScreenUtils.dpToPx(DEFAULT_MARGIN_HEIGHT)

        var textSize = ScreenUtils.spToPx(28)
        mSettingManager?.let {
            textSize = it.getTextSize()
        }

        setUpTextParams(textSize)
    }

    //初始化画笔
    private fun initPaint() {
        //绘制提示的画笔
        mTipPaint = Paint()
        mTipPaint?.color = mTextColor
        mTipPaint?.textAlign = Paint.Align.LEFT
        mTipPaint?.textSize = ScreenUtils.spToPx(DEFAULT_TIP_SIZE).toFloat()
        //抗锯齿
        mTipPaint?.isAntiAlias = true
        //是否开启次像素级的抗锯齿（更好的抗锯齿效果）
        mTipPaint?.isSubpixelText = true


        //绘制页面内容的画笔
        mTextPaint = TextPaint()
        mTextPaint?.color = mTextColor
        mTextPaint?.textSize = mTextSize.toFloat()
        mTextPaint?.isAntiAlias = true


        mTitlePaint = TextPaint()
        mTitlePaint?.color = mTextColor
        mTitlePaint?.textSize = mTitleSize.toFloat()
        // Fill 填充内部  Stroke 描边
        mTitlePaint?.style = Paint.Style.FILL_AND_STROKE
        //加粗
        mTitlePaint?.typeface = Typeface.DEFAULT_BOLD
        mTitlePaint?.isAntiAlias = true


        // 绘制背景的画笔
        mBgPaint = Paint()
        mBgPaint?.color = mBgColor

        //绘制电池的画笔
        mBatteryPaint = Paint()
        mBatteryPaint?.isAntiAlias = true
        //设置防抖动
        mBatteryPaint?.isDither = true


        //初始化页面样式(day or night)
        var isNightMode = false
        mSettingManager?.let {
            isNightMode = it.isNightMode()
        }
        setNightMode(isNightMode)
    }


    /**
     * 设置页面模式(白天 - 黑夜)
     * @param nightMode 夜晚模式
     */
    private fun setNightMode(nightMode: Boolean) {
        mSettingManager?.let {
            it.setNightMode(nightMode)
            isNightMode = nightMode

            if (isNightMode) {
                mBatteryPaint?.color = Color.WHITE

            } else {
                mBatteryPaint?.color = Color.BLACK

            }
        }
    }


    /**
     * 设置阅读页面的底色(正面- 反面)
     * @param pageStyle 阅读页面的背景色
     */
    private fun setPageStyle(pageStyle: PageStyle) {
        if (pageStyle != PageStyle.BG_NIGHT) {
            mPageStyle = pageStyle
            mSettingManager?.setPageStyle(pageStyle)
        }

        if (isNightMode && pageStyle != PageStyle.BG_NIGHT) {
            return
        }
        //设置当前颜色样式
        mContext?.let {
            mTextColor = ContextCompat.getColor(it, pageStyle.fontColor)
            mBgColor = ContextCompat.getColor(it, pageStyle.bgColor)
        }
        mTipPaint?.color = mTextColor
        mTitlePaint?.color = mTextColor
        mTextPaint?.color = mTextColor


        mBgPaint?.color = mBgColor
        mPageView?.drawCurrentPage(false)
    }


    /**
     * 翻页动画
     *
     * @param pageMode 翻页模式
     */
    fun setPageMode(pageMode: PageMode) {
        mPageMode = pageMode
        mPageView?.setPageMode(mPageMode)
        mPageMode?.let {
            mSettingManager?.setPageMode(it)
        }
        mPageView?.drawCurrentPage(false)
    }

    /**
     * 设置内容与屏幕的间距
     *
     * @param marginWidth px
     * @param marginHeight px
     */
    fun setMargin(marginWidth: Int, marginHeight: Int) {
        mMarginWidth = marginWidth
        mMarginWidth = marginHeight

        // 如果是滑动动画，则需要重新创建
        if (mPageMode == PageMode.SCROLL) {
            mPageView?.setPageMode(PageMode.SCROLL)
        }

        mPageView?.drawCurrentPage(false)
    }

    /**
     * 初始化PageView
     */
    private fun initPageView() {
        mPageView?.setPageMode(mPageMode)
        mPageView?.setBgColor(mBgColor)
    }


    /**
     * 初始化书籍(将书籍数据储存入本地数据库)
     */
    private fun prepareBook() {
        // 对mBookRecord进行赋值(从数据库根据Id的形式)
        //unFinish
        // 原来使用了GreenDao现使用Room
        if (mBookRecord == null) {
            mBookRecord = BookRecordBean()
        }
        var currentChapterPosition = 0
        mBookRecord?.let {
            currentChapterPosition = it.chapter
        }
        mCurrentChapterPosition = currentChapterPosition
        mLastChapterPosition = mCurrentChapterPosition
    }


    /**
     * 跳转到上一章
     */
    fun skipPreChapter(): Boolean {
        if (!hasPreChapter()) {
            return false
        }
        //载入上一章
        mCurPage = if (parsePreChapter()) {
            getCurrentPage(0)
        } else {
            TextPage()
        }
        mPageView?.drawCurrentPage(false)
        return false
    }


    /**
     * 跳转到下一章
     */
    fun skipNextChapter(): Boolean {
        if (!hasNextChapter()) {
            return false
        }

        //判断是否到章节的终止点
        mCurPage = if (parseNextChapter()) {
            getCurrentPage(0)
        } else {
            TextPage()
        }

        mPageView?.drawCurrentPage(false)

        return true
    }


    /**
     * 跳转到指定章节
     * @param position :章节位置
     */
    fun skipToChapter(position: Int) {
        //设置参数
        mCurrentChapterPosition = position
        //将上一章的缓存设置为null
        mPrePageList.clear()

        //如果当前下一章缓存正在执行，则取消
        if (mPreLoadDisposable != null) {
            mPreLoadDisposable?.dispose()
        }

        //将下一章缓存设置为null
        mNextPageList.clear()

        //打开指定章节
        openChapter()
    }


    /**
     * 跳转到指定的页
     *
     * @param position
     */
    fun skipToPage(position: Int): Boolean {
        if (!isChapterListPrepare) {
            return false
        }

        mCurPage = getCurrentPage(position)
        mPageView?.drawCurrentPage(false)
        return true
    }

    /**
     * 翻到上一页
     */
    fun skipToPrePage(): Boolean? {
        return mPageView?.autoPrePage()
    }

    /**
     * 翻到下一页
     */
    fun skipToNextPage(): Boolean? {
        return mPageView?.autoNextPage()
    }

    /**
     * 更新时间
     */
    fun updateTime() {
        mPageView?.let {
            if (!it.isRunning()) {
                mPageView?.drawCurrentPage(true)
            }
        }
    }

    /**
     * 更新电量
     *
     * @param level : 电量
     */
    fun updateBattery(level: Int) {
        mBatteryLevel = level
        mPageView?.let {
            if (!it.isRunning()) {
                it.drawCurrentPage(true)
            }
        }
    }


    /**
     * 设置提示的文字大小
     *
     * @param textSize : px
     */
    fun setTipTextSize(textSize: Int) {
        mTipPaint?.textSize = textSize.toFloat()
        mPageView?.drawCurrentPage(false)
    }


    /**
     * 设置文字相关参数
     *
     * @param textSize : px
     */
    fun setTextSize(textSize: Int) {
        //设置文字相关参数
        setUpTextParams(textSize)

        //设置画笔的字体大小
        mTextPaint?.textSize = mTextSize.toFloat()
        //设置标题的字体大小
        mTitlePaint?.textSize = mTitleSize.toFloat()
        // 存储文字大小
        mSettingManager?.setTextSize(mTextSize)
        //取消缓存
        mPrePageList.clear()
        mNextPageList.clear()

        //如果当前已经显示数据
        if (isChapterListPrepare && mStatus == STATUS_FINISH) {
            //重新计算当前页面
            dealLoadPageList(mCurrentChapterPosition)

            //防止在最后一页,通过修改字体导致页面总数减少导致奔溃的问题
            mCurPage?.let {
                if (it.position >= mCurPageList.size) {
                    it.position = mCurPageList.size - 1
                }

                // 重新获取指定页面
                mCurPage = mCurPageList[it.position]
            }
        }

        mPageView?.drawCurrentPage(true)
    }


    /**
     * 判断上一章节是否为空
     */
    private fun hasPreChapter(): Boolean {
        if (mCurrentChapterPosition - 1 < 0) {
            return false
        }
        return true
    }


    /**
     * 设置与文字相关的参数
     */
    private fun setUpTextParams(textSize: Int) {
        mTextSize = textSize
        mTitleSize = textSize + ScreenUtils.spToPx(EXTRA_TITLE_SIZE)
        mTextInterval = mTextSize / 2
        mTitleInterval = mTextSize / 2
        mTextPara = mTextSize
        mTitlePara = mTitleSize
    }

    /**
     * 设置页面切换监听
     *@param listener
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageChangeListener = listener

        if (isChapterListPrepare) {
            mPageChangeListener?.onCategoryFinish(mChapterList)
        }
    }

    /**
     * 获取当前页的状态
     */
    fun getPageStatus(): Int {
        return mStatus
    }

    /**
     * 获取书籍信息
     */
    fun getCoolBook(): CoolBookBean? {
        return mCoolBook
    }

    /**
     * 获取章节目录
     */
    fun getChapterCategory(): List<TextChapter> {
        return mChapterList
    }

    /**
     * 获取当前页的页码
     */
    fun getPagePosition(): Int? {
        return mCurPage?.position
    }

    /**
     * 获取当前书籍的章节位置
     */
    fun getChapterPosition(): Int {
        return mCurrentChapterPosition
    }

    /**
     * 获取显示内容距离屏幕的高度
     */
    fun getMarginHeight(): Int {
        return mMarginHeight
    }


    /**
     * 保存阅读记录
     */
    fun saveRecord() {
        if (mChapterList.isEmpty()) {
            return
        }

        mCoolBook?.let {
            mBookRecord?.bookId = it.id
            mBookRecord?.chapter = mCurrentChapterPosition
        }

        if (mCurPage != null) {
            mBookRecord?.let {
                it.pagePos = mCurPage!!.position
            }
        } else {
            mBookRecord?.pagePos = 0
        }

        //数据库存储操作(BookRecord)
        //unFinish
    }

    /**
     * 打开指定章节
     */
    fun openChapter() {
        isFirstOpen = false
        mPageView?.let {
            if (it.isPrepare()) {
                return
            }
        }

        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING
            mPageView?.drawCurrentPage(false)
            return
        }


        if (parseCurrentChapter()) {
            if (!isChapterOpen) {
                //如果章节从未打开
                mBookRecord?.let {
                    var position = it.pagePos

                    //防止记录的页号,大于当前的最大页号
                    if (position >= mCurPageList.size) {
                        position = mCurPageList.size - 1
                    }
                    mCurPage = getCurrentPage(position)
                    mCancelPage = mCurPage
                    //切换状态
                    isChapterOpen = true
                }
            } else {
                mCurPage = getCurrentPage(0)
            }
        } else {
            mCurPage = TextPage()
        }

        mPageView?.drawCurrentPage(true)
    }

    /**
     * 加载错误页面
     */
    fun chapterError() {
        mStatus = STATUS_ERROR
        mPageView?.drawCurrentPage(false)
    }


    /**
     * 关闭书本
     */
    fun closeBook() {
        isChapterListPrepare = false
        isClose = true
        mPreLoadDisposable?.dispose()

        mChapterList.clear()
        mCurPageList.clear()
        mNextPageList.clear()

        mPageView = null
        mCurPage = null
    }


    /**
     * 获取当前显示页
     */
    private fun getCurrentPage(position: Int): TextPage {
        if (mPageChangeListener != null) {
            mPageChangeListener!!.onPageCountChange(position)
        }
        return mCurPageList[position]
    }


    /**
     * 解析当前书籍的目录
     */
    private fun parseCurrentChapter(): Boolean {
        //解析数据
        dealLoadPageList(mCurrentChapterPosition)
        //预加载下一页面
        preLoadNextChapter()
        return mCurPageList != null
    }

    /**
     * 预加载下一章
     */
    private fun preLoadNextChapter() {
        val nextChapter = mCurrentChapterPosition + 1

        //如果不存在下一章，且下一章没有数据，则不进行加载
        if (!hasNextChapter() ||
                !hasChapterData(mChapterList[nextChapter])) {
            return
        }

        //如果之前正在加载则取消
        if (mPreLoadDisposable != null) {
            mPreLoadDisposable?.dispose()
        }


        //解析书籍信息
        //unFinish
    }


    /**
     * 取消翻页
     */
    fun pageCancel() {
        if (mCurPage?.position == 0
                && mCurrentChapterPosition > mLastChapterPosition) {
            //加载到下一章取消
            if (mPrePageList != null) {
                cancelNextChapter()
            } else {
                mCurPage = if (parsePreChapter()) {
                    getPreLastPage()
                } else {
                    TextPage()
                }
            }
        } else if (mCurPageList == null
                || (mCurPage?.position == mCurPageList.size
                        && mCurrentChapterPosition < mLastChapterPosition)) {
            if (mNextPageList != null) {
                cancelPreChapter()
            } else {
                mCurPage = if (parseNextChapter()) {
                    mCurPageList[0]
                } else {
                    TextPage()
                }
            }
        } else {
            // 假如加载到下一页，又取消了。那么需要重新装载
            mCurPage = mCancelPage
        }
    }


    private fun parseNextChapter(): Boolean {
        return false
    }

    private fun cancelPreChapter() {
        // 重置位置位
        val temp = mLastChapterPosition
        mLastChapterPosition = mCurrentChapterPosition
        mCurrentChapterPosition = temp

        //重置页面列表(交换引用 unConfirm)
        mPrePageList = mCurPageList
        mCurPageList = mNextPageList
        mNextPageList.clear()

        chapterChangeCallback()
        mCurPage = getCurrentPage(0)
        mCancelPage = null
    }

    /**
     * 获取上一个章节的最后一页
     */
    private fun getPreLastPage(): TextPage {
        val position = mCurPageList.size - 1
        mPageChangeListener?.onPageChange(position)
        return mCurPageList[position]
    }


    /**
     * 取消下一页
     */
    private fun cancelNextChapter() {
        val temp = mLastChapterPosition
        mLastChapterPosition = mCurrentChapterPosition
        mCurrentChapterPosition = temp

        mNextPageList = mCurPageList
        mCurPageList = mPrePageList
        mPrePageList.clear()

        chapterChangeCallback()

        mCurPage = getPreLastPage()
        mCancelPage = null
    }


    /**
     * 设置页面翻页回调
     */
    private fun chapterChangeCallback() {
        mPageChangeListener?.onChapterChange(mCurrentChapterPosition)
        mPageChangeListener?.onPageCountChange(mCurPageList.size)
    }

    /**
     * 解析上一章的数据
     */
    private fun parsePreChapter(): Boolean {
        //加载上一章数据
        val preChapterPosition = mCurrentChapterPosition - 1
        mLastChapterPosition = mCurrentChapterPosition
        mCurrentChapterPosition = preChapterPosition

        //当前章节缓存为下一章
        mNextPageList = mCurPageList
        //判断是否具有上一章缓存
        //unConfirm
        if (mPrePageList.isNotEmpty()) {
            mCurPageList = mPrePageList
            mPrePageList.clear()
            chapterChangeCallback()
        } else {
            dealLoadPageList(preChapterPosition)
        }
        return mCurPageList.size > 0
    }


    /**
     * 判断是否还有下一章节
     */
    private fun hasNextChapter(): Boolean {
        if (mCurrentChapterPosition + 1 >= mCurPageList.size) {
            return false
        }
        return true
    }

    /**
     * 重新计算当前页面
     */
    private fun dealLoadPageList(currentChapterPosition: Int) {
        try {
             loadPageList(currentChapterPosition)
        } catch (e: Exception) {

        }
    }

    /**
     * 加载页面列表
     *@param chapterPosition : 章节序号
     */
    @Throws(java.lang.Exception::class)
    private fun loadPageList(chapterPosition: Int) :ArrayList<TextPage>?{
        //获取章节
        val chapter = mChapterList[chapterPosition]
        //判断章节是否存在
        if(!hasChapterData(chapter)){
            return null
        }
        // 获取章节的文本流
        val chapterReader = getChapterReader(chapter)
        return loadPages(chapter,chapterReader)
    }


    private fun loadPages(chapter: TextChapter,chapterReader: BufferedReader) :ArrayList<TextPage>{
        //生成的页面
        val pages :ArrayList<TextPage> = ArrayList()
        //使用流的方式加载
        val lines :ArrayList<String> = ArrayList()
        val rHeight = mVisibleHeight
        var titleLinesCount = 0
        //是否展示标题
        var showTitle = true
        //默认展示标题
        var paragraph = chapter.title
        try {
            while (showTitle){

            }
        }catch (e:java.lang.Exception){

        }

        return pages
    }


    /**
     * 根据当前状态决定是否能够翻页
     */
    private fun canTurnPage(): Boolean {
        var canTurn = true
        if (!isChapterListPrepare) {
            canTurn = false
        }
        //是否为错误状态 或 准备状态
        if (mStatus == STATUS_PARSE_ERROR || mStatus == STATUS_PARING) {
            canTurn = false
        } else if (mStatus == STATUS_ERROR) {
            //unConfirm
            mStatus = STATUS_LOADING
        }

        return canTurn
    }

    /**
     * 获取上个页面
     */
    private fun getPrePage(): TextPage? {
        var position = -1
        mCurPage?.let {
            position = it.position - 1
        }
        if (position < 0) {
            return null
        }
        mPageChangeListener?.onPageChange(position)
        return mCurPageList[position]
    }

    /**
     * 获取下个页面
     */
    private fun getNextPage(): TextPage? {
        var position = 0
        mCurPage?.let {
            position = it.position + 1
        }
        if (position >= mCurPageList.size) {
            return null
        }
        mPageChangeListener?.onPageChange(position)
        return mCurPageList[position]
    }

    /**
     * 页面是否处于关闭状态
     */
    fun isClose(): Boolean {
        return isClose
    }

    /**
     * 章节是否打开
     */
    fun isChapterOpen(): Boolean {
        return isChapterOpen
    }

    /**
     * 判断章节数据是否存在
     */
    protected abstract fun hasChapterData(chapter: TextChapter): Boolean
    @Throws(Exception::class)
    protected abstract fun getChapterReader(chapter :TextChapter) :BufferedReader
    interface OnPageChangeListener {
        fun onChapterChange(pos: Int)
        fun requestChapters(requestChapters: List<TextChapter>)
        fun onCategoryFinish(chapter: List<TextChapter>)
        fun onPageCountChange(count: Int)
        fun onPageChange(pos: Int)
    }
}