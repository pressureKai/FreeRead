package com.kai.bookpage.page

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.kai.bookpage.R
import com.kai.bookpage.model.BookRecordBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.TextChapter
import com.kai.bookpage.model.TextPage
import com.kai.bookpage.utils.StringUtils
import com.kai.common.constant.Constant
import com.kai.common.utils.IOUtils
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import io.reactivex.rxjava3.disposables.Disposable
import java.io.BufferedReader

/**
 * # 页面数据提供者
 * 衍生两个类
 *   LocalPageLoader
 *   NetPageLoader
 */
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


    /**
     * 初始化数据
     */
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

    /**
     * 初始化画笔
     */
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
                setPageStyle(PageStyle.BG_NIGHT)
            } else {
                mBatteryPaint?.color = Color.BLACK
                mPageStyle?.let {
                    setPageStyle(it)
                }

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
     * 设置与文字相关的参数
     */
    private fun setUpTextParams(textSize: Int) {
        // 文字大小
        mTextSize = textSize
        mTitleSize = textSize + ScreenUtils.spToPx(EXTRA_TITLE_SIZE)
        // 行间距（大小为字体的一半）
        mTextInterval = mTextSize / 2
        mTitleInterval = mTextSize / 2
        // 段落间距（大小为字体的高度）
        mTextPara = mTextSize
        mTitlePara = mTitleSize
    }
    /****************parse fun********************/
    /**
     * 加载页面列表
     *@param chapterPosition : 章节序号
     */
    @Throws(java.lang.Exception::class)
    private fun loadPageList(chapterPosition: Int): ArrayList<TextPage>? {
        //获取章节
        val chapter = mChapterList[chapterPosition]
        //判断章节是否存在
        if (!hasChapterData(chapter)) {
            return null
        }
        // 获取章节的文本流
        val chapterReader = getChapterReader(chapter)
        return loadPages(chapter, chapterReader)
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

    /****************draw fun********************/
    /**
     *绘制背景
     */
    private fun drawBackground(bitmap: Bitmap, isUpdate: Boolean) {
        val canvas = Canvas(bitmap)
        val tipMarginHeight = ScreenUtils.dpToPx(3)
        if (!isUpdate) {
            //绘制背景
            canvas.drawColor(mBgColor)
            if (mChapterList.isNotEmpty()) {
                //初始化标题的参数
                //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部位置
                var tipTop = 0f
                mTipPaint?.let {
                    tipTop = tipMarginHeight - it.fontMetrics.top
                }
                //根据状态不一样，数据不一样
                if (mStatus != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        mTipPaint?.let {
                            canvas.drawText(
                                mChapterList[mCurrentChapterPosition].title,
                                mMarginWidth.toFloat(), tipTop, it
                            )
                        }
                    }
                } else {
                    mCurPage?.let {
                        mTipPaint?.let { paint ->
                            canvas.drawText(it.title, mMarginWidth.toFloat(), tipTop, paint)
                        }
                    }
                }


                //绘制页码
                //底部的字现实的位置Y
                var y = 0f
                mTipPaint?.let {
                    y = mDisplayHeight - it.fontMetrics.bottom - tipMarginHeight
                }
                //只有章节数据获取finish的时候采用页码
                if (mStatus == STATUS_FINISH) {
                    var percent = ""
                    mCurPage?.let {
                        percent = (it.position + 1).toString() + "/" + mCurPageList.size
                    }
                    mTipPaint?.let {
                        canvas.drawText(percent, mMarginWidth.toFloat(), y, it)
                    }
                }
            }
        } else {
            //擦除区域
            mBgPaint?.let {
                it.color = mBgColor
                canvas.drawRect(
                    (mDisplayWidth / 2).toFloat(),
                    (mDisplayHeight - mMarginHeight + ScreenUtils.dpToPx(2)).toFloat(),
                    mDisplayWidth.toFloat(),
                    mDisplayHeight.toFloat(),
                    it
                )
            }
        }


        //绘制电池
        val visibleRight = mDisplayWidth - mMarginWidth
        val visibleBottom = mDisplayHeight - tipMarginHeight
        var outFrameWidth = 0
        var outFrameHeight = 0
        mTipPaint?.let {
            outFrameWidth = it.measureText("xxx").toInt()
            outFrameHeight = it.textSize.toInt()
        }
        val polarHeight = ScreenUtils.dpToPx(6)
        val polarWidth = ScreenUtils.dpToPx(2)
        val border = 1
        val innerMargin = 1
        //电极的制作
        val polarLeft = visibleRight - polarWidth
        val polarTop = visibleBottom - (outFrameHeight + polarHeight)/2
        val polar = Rect(
            polarLeft,
            polarTop,
            visibleRight,
            polarTop + polarHeight - ScreenUtils.dpToPx(2)
        )
        mBatteryPaint?.style = Paint.Style.FILL
        mBatteryPaint?.let {
            canvas.drawRect(polar, it)
        }

        //外框的制作
        val outFrameLeft = polarLeft - outFrameWidth
        val outFrameTop = visibleBottom - outFrameHeight
        val outFrameBottom = visibleBottom - ScreenUtils.dpToPx(2)
        val outFrame = Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom)
        mBatteryPaint?.style = Paint.Style.STROKE
        mBatteryPaint?.strokeWidth = border.toFloat()
        mBatteryPaint?.let {
            canvas.drawRect(outFrame, it)
        }


        //内框制作
        val innerWidth = (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f)
        val innerFrame = RectF(
            (outFrameLeft + border + innerMargin).toFloat(),
            (outFrameTop + border + innerMargin).toFloat(),
            outFrameLeft + border + innerMargin + innerWidth,
            (outFrameBottom - border - innerMargin).toFloat()
        )


        mBatteryPaint?.style = Paint.Style.FILL
        mBatteryPaint?.let {
            canvas.drawRect(innerFrame, it)
        }


        //绘制当前时间
        //底部的字显示的位置Y
        val time = StringUtils.dateConvert(System.currentTimeMillis(), Constant.FORMAT_TIME)
        mTipPaint?.let {
            val y = mDisplayHeight - it.fontMetrics.bottom - tipMarginHeight
            val x = outFrameLeft - it.measureText(time)
            canvas.drawText(time, x, y, it)
        }
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
     * 中心文字绘制
     */
    private fun drawCenter(tip: String, canvas: Canvas){
        mTextPaint?.let {
            val fontMetrics = it.fontMetrics
            val textHeight = fontMetrics.top - fontMetrics.bottom
            val textWidth = it.measureText(tip)
            val pivotX = (mDisplayWidth - textWidth) / 2
            val pivotY = (mDisplayHeight - textHeight) / 2
            mTextPaint?.let { paint ->
                canvas.drawText(tip, pivotX, pivotY, paint)
            }
        }
    }


    /**
     * 绘制页面，暴露给外部的方法
     */
    fun drawPage(bitmap: Bitmap, isUpdate: Boolean) {
        mPageView?.let {
            it.getBgBitmap()?.let { bit ->
                drawBackground(bit, isUpdate)
            }
        }
        if(!isUpdate){
            drawContent(bitmap)
        }
        //更新绘制
        mPageView?.invalidate()
    }


    /**
     * 重新计算当前页面
     */
    private fun dealLoadPageList(currentChapterPosition: Int) {
        try {
            loadPageList(currentChapterPosition)?.let {
                mCurPageList = it
            }
            if (mCurPageList != null) {
                if (mCurPageList.isEmpty()) {
                    mStatus = STATUS_EMPTY
                    //添加一个空数据
                    val page = TextPage()
                    page.lines = ArrayList(1)
                    mCurPageList.add(page)
                } else {
                    mStatus = STATUS_FINISH
                }
            } else {
                mStatus = STATUS_LOADING
            }

        } catch (e: Exception) {
            e.printStackTrace()
            mCurPageList.clear()
            mStatus = STATUS_ERROR
        }

        //回调
        chapterChangeCallback()
    }


    /****************private fun********************/

    /**
     * 判断上一章节是否为空
     */
    private fun hasPreChapter(): Boolean {
        if (mCurrentChapterPosition - 1 < 0) {
            return false
        }
        return true
    }

    private fun scaleBitmap(origin: Bitmap?): Bitmap?{
        if(origin == null){
            return null
        }
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.preScale(0.5f, 0.5f)
        val createBitmap = Bitmap.createBitmap(
            origin, 0, 0,
            width, height,
            matrix, false
        )
        if(createBitmap == origin){
            return createBitmap
        }
        return createBitmap
    }

    /**
     * 获取当前显示页根据Position
     */
    private fun getCurrentPage(position: Int): TextPage {
        if (mPageChangeListener != null) {
            mPageChangeListener!!.onPageCountChange(position)
        }
        return mCurPageList[position]
    }

    /****************getState fun********************/

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
     * 设置页面翻页回调
     */
    private fun chapterChangeCallback() {
        mPageChangeListener?.onChapterChange(mCurrentChapterPosition)

        mPageChangeListener?.onPageCountChange(mCurPageList.size)
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
     * 获取当前页
     */
    fun getCurrentPage(): TextPage?{
        return mCurPage
    }

    /**
     * 设置当前页
     */
    fun setCurrentPage(currentPage: TextPage){
        this.mCurPage = currentPage
    }

    /**
     * 获取当前页列表
     */
    fun getCurrentPageList(): List<TextPage> {
        return mCurPageList
    }

    /**
     * 获取下一页列表
     */
    fun getNextPageList(): List<TextPage>{
        return mNextPageList
    }
    /**
     * 设置页面切换监听
     *@param listener
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageChangeListener = listener

        // 如果目录加载完之后才设置监听器，那么会默认回调
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


    /****************operation fun********************/
    /**
     * 翻页动画
     *
     * @param pageMode 翻页模式
     */
    fun setPageMode(pageMode: PageMode) {
        mPageMode = pageMode
        mPageMode?.let {
            mPageView?.setPageMode(it)
        }

        mPageMode?.let {
            mSettingManager?.setPageMode(it)
        }
        // 重新绘制的前页
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
        //设置参数
        mPageMode?.let {
            mPageView?.setPageMode(it)
        }
        mPageView?.setBgColor(mBgColor)
    }





    /**
     * 来吧，准备展示
     */
    fun prepareDisplay(w: Int, h: Int) {
        // 获取PageView的宽高
        mDisplayWidth = w
        mDisplayHeight = h
        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginWidth * 2
        mVisibleHeight = mDisplayHeight - mMarginHeight * 2
        //重置 PageMode
        mPageMode?.let {
            mPageView?.setPageMode(it)
        }

        if (!isChapterOpen) {
            //展示加载页面
            mPageView?.drawCurrentPage(false)
            //如果在 display 之前调用 openChapter 肯定是无法打开的
            //所以需要通过display再重新调用一次
            if (!isFirstOpen) {
                openChapter()
            }
        } else {
            //如果章节已显示，那么就重新计算页面
            if (mStatus == STATUS_FINISH) {
                dealLoadPageList(mCurrentChapterPosition)
                //重新设置文章指针的位置
                mCurPage?.let {
                    mCurPage = getCurrentPage(it.position)
                }
            }
            mPageView?.drawCurrentPage(false)
        }
    }


    /**
     * 跳转到上一章
     * @return 跳转是否成功
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
        return true
    }


    /**
     * 跳转到下一章
     * @return 跳转是否成功
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
     * @return 跳转是否成功
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
     * @return 操作是否成功
     */
    fun skipToPrePage(): Boolean? {
        return mPageView?.autoPrePage()
    }

    /**
     * 翻到下一页
     * @return 操作是否成功
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
        //如果屏幕大小加载完成
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

        mPageView?.drawCurrentPage(false)
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
     * 翻到下一页
     * @return 是否允许翻页
     */
    fun next(): Boolean {
        //以下情况禁止翻页
        if (!canTurnPage()) {
            return false
        }
        if (mStatus == STATUS_FINISH) {
            //先查看是否存在下一页
            val nextPage = getNextPage()
            if (nextPage != null) {
                mCancelPage = mCurPage
                mCurPage = nextPage
                mPageView?.drawNextPage()
                return true
            }
        }

        if (!hasNextChapter()) {
            return false
        }


        mCancelPage = mCurPage
        //解析下一章数据
        mCurPage = if (parseNextChapter()) {
            mCurPageList.first()
        } else {
            TextPage()
        }
        mPageView?.drawNextPage()
        return true
    }


    /**
     * 翻阅上一页
     *
     * @return 操作是否成功
     */
    fun pre(): Boolean {
        //以下情况禁止翻页
        if (!canTurnPage()) {
            return false
        }

        if (mStatus == STATUS_FINISH) {
            //先查看是否存在上一页
            val prePage = getPrePage()
            if (prePage != null) {
                mCancelPage = mCurPage
                mCurPage = prePage
                mPageView?.drawNextPage()
                return true
            }
        }

        if (!hasPreChapter()) {
            return false
        }

        mCancelPage = mCurPage
        mCurPage = if (parsePreChapter()) {
            getPreLastPage()
        } else {
            TextPage()
        }
        mPageView?.drawNextPage()
        return true
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

        //如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING
            mPageView?.drawCurrentPage(false)
            return
        }


        // 如果获取到的章节目录为空
        if(mChapterList.isEmpty()){
            mStatus = STATUS_CATEGORY_EMPTY
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

        mPageView?.drawCurrentPage(false)
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

    /****************abstract fun********************/
    interface OnPageChangeListener {
        fun onChapterChange(pos: Int)
        fun requestChapters(requestChapters: List<TextChapter>)
        fun onCategoryFinish(chapter: List<TextChapter>)
        fun onPageCountChange(count: Int)
        fun onPageChange(pos: Int)
    }

    /**
     * 判断章节数据是否存在
     */
    protected abstract fun hasChapterData(chapter: TextChapter): Boolean

    /**
     * 获取章节的文件流
     */
    @Throws(Exception::class)
    protected abstract fun getChapterReader(chapter: TextChapter): BufferedReader


    /**
     * 刷新章节列表
     */
    abstract fun refreshChapterList()





    /****************unFinish fun********************/



    /**
     * 初始化书籍(将书籍数据储存入本地数据库)
     * unFinish
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
     * 保存阅读记录
     * unFinish
     */
    open fun saveRecord() {
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
     * 预加载下一章
     * unFinish
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


    /****************unConfirm fun********************/


    /**
     * 绘制主体内容
     */
    private fun drawContent(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        if (mPageMode == PageMode.SCROLL) {
            canvas.drawColor(mBgColor)
        }

        if (mStatus != STATUS_FINISH) {
            //绘制字体
            var tip = ""
            when (mStatus) {
                STATUS_LOADING -> {
                    tip = "正在拼命加载中..."
                }
                STATUS_ERROR -> {
                    tip = "加载失败(点击边缘重试)"
                }
                STATUS_EMPTY -> {
                    tip = "文字内容为空"
                }
                STATUS_PARING -> {
                    tip = "正在排版请等待..."
                }
                STATUS_PARSE_ERROR -> {
                    tip = "文件解析错误"
                }
                STATUS_CATEGORY_EMPTY -> {
                    tip = "目录列表为空"
                }
            }

            //将提示语句放正中间
            mTextPaint?.let {
                val fontMetrics = it.fontMetrics
                val textHeight = fontMetrics.top - fontMetrics.bottom
                val textWidth = it.measureText(tip)

                val pivotX = (mDisplayWidth - textWidth) / 2
                val pivotY = (mDisplayHeight - textHeight) / 2
                canvas.drawText(tip, pivotX, pivotY, it)
            }
        } else {
            var top = 0f
            if (mPageMode == PageMode.SCROLL) {
                mTextPaint?.let {
                    top = -it.fontMetrics.top
                }
            } else {
                mTextPaint?.let {
                    top = mMarginHeight - it.fontMetrics.top
                }
            }


            //设置总距离
            var interval = 0
            var para = 0
            var titleInterval = 0
            var titlePara = 0
            var str = ""
            mTitlePaint?.let {
                titleInterval = mTitleInterval + it.textSize.toInt()
            }
            mTextPaint?.let {
                interval = mTextInterval + it.textSize.toInt()
                para = mTextPara + it.textSize.toInt()
                titlePara = mTitlePara + it.textSize.toInt()
            }

            //对标题进行绘制
            mCurPage?.let {
                val titleLines = it.titleLines
                //unConfirm
                for (i in 0.until(titleLines)) {
                    str = it.lines[i]

                    //设置顶部间距
                    if (i == 0) {
                        top += mTitlePara
                    }


                    //计算文字的显示起始点
                    mTitlePaint?.let { paint ->
                        val start = ((mDisplayWidth - paint.measureText(str)) / 2).toInt()
                        //进行绘制
                        canvas.drawText(str, start.toFloat(), top, paint)
                    }

                    //设置尾部间距
                    top += if (i == it.titleLines - 1) {
                        titlePara
                    } else {
                        //行间距
                        titleInterval
                    }
                    //对内容进行绘制
                    mCurPage?.let { textPage ->
                        for (index in textPage.titleLines.until(textPage.lines.size)) {
                            str = textPage.lines[index]
                            mTextPaint?.let { paint ->
                                canvas.drawText(str, mMarginWidth.toFloat(), top, paint)
                            }
                            top += if (str.endsWith("\n")) {
                                para
                            } else {
                                interval
                            }
                        }
                    }


                    val picture = getCurrentPage()?.picture
                    if(!picture.isNullOrEmpty()){
                        mContext?.let { context ->
                            Glide.with(context)
                                .asBitmap()
                                .load(picture)
                                .thumbnail(0.1f)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onLoadStarted(placeholder: Drawable?) {
                                        canvas.save()
                                        drawCenter(context.getString(R.string.pic_loading), canvas)
                                        canvas.restore()
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        var realBitmap = resource
                                        if (resource.width > mDisplayWidth) {
                                            val scaleBitmap = scaleBitmap(resource)
                                            if (scaleBitmap != null) {
                                                realBitmap = scaleBitmap
                                            }
                                        }

                                        val pivotX =
                                            ((mDisplayWidth - resource.width) shr 1).toFloat()
                                        val pivotY =
                                            ((mDisplayHeight - resource.height) shr 1).toFloat()

                                        val currentPicture = getCurrentPage()?.picture

                                        if(!currentPicture.isNullOrEmpty()){
                                            canvas.drawBitmap(realBitmap,pivotX,pivotY,mTextPaint)
                                            mPageView?.invalidate()
                                        }

                                    }
                                })
                        }

                    }
                }
            }
        }
    }




    /**
     * 解析下一章节数据
     * @return 操作是否成功
     */
    private fun parseNextChapter(): Boolean {
        val nextChapterPosition = mCurrentChapterPosition + 1

        mLastChapterPosition = mCurrentChapterPosition
        mCurrentChapterPosition = nextChapterPosition

        //将当前章节的页面列表，作为上一章缓存
        //unConfirm
        mPrePageList = mCurPageList

        //是否下一章数据已经预加载
        if (mNextPageList != null && mNextPageList.isNotEmpty()) {
            //unConfirm
            mCurPageList = mNextPageList
            mNextPageList.clear()
            //回调
            chapterChangeCallback()
        } else {
            //处理页面解析
            dealLoadPageList(nextChapterPosition)
        }
        //预加载下一页面
        preLoadNextChapter()
        if (mCurPageList != null && mCurPageList.size > 0) {
            return true
        }
        return false
    }


    /**
     * 清空上一章节
     * unConfirm
     */
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
     * 取消下一页
     * unConfirm
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
     * # 将章节数据，解析成页面列表
     *
     * @param chapter : 章节信息
     * @param chapterReader : 章节的文本流
     * @return pages : 页面列表
     */
    private fun loadPages(chapter: TextChapter, chapterReader: BufferedReader): ArrayList<TextPage> {
        //生成的页面
        val pages: ArrayList<TextPage> = ArrayList()
        //使用流的方式加载
        val lines: ArrayList<String> = ArrayList()
        var rHeight = mVisibleHeight
        var titleLinesCount = 0
        //是否展示标题
        var showTitle = true
        //默认展示标题
        var paragraph = chapter.title
        try {
            //java代码中的while循环其中涉及到流数据的读取赋值问题,在kotlin中使用以下写法会报错
            //(Assignments are not expressions, and only expressions are allowed in this context)
            //while(showTitle || (paragraph = chapterReader.readLine()) != null){
            //
            //}
            do {
                paragraph = chapterReader.readLine()
                if (showTitle || paragraph != null) {
                    mContext?.let {
                        paragraph = StringUtils.convertCC(paragraph, it)
                    }
                    //重置段落
                    if (!showTitle) {
                        //unConfirm
                        paragraph = paragraph.replace("\\s", "")
                        //如果只有换行符，那么就不执行
                        if (paragraph == "") {
                            continue
                        }
                        paragraph = StringUtils.halfToFull("  $paragraph\n")
                    } else {
                        //设置title的顶部间距
                        rHeight -= mTitlePara
                    }

                    var wordCount = 0
                    var subStr = ""
                    while (paragraph.isNotEmpty()) {
                        //当前空间，是否容得下一行文字
                        if (showTitle) {
                            mTitlePaint?.let {
                                rHeight -= it.textSize.toInt()
                            }
                        } else {
                            mTextPaint?.let {
                                rHeight -= it.textSize.toInt()
                            }
                        }
                        //一页已经填充满了，创建TextPage
                        if (rHeight <= 0) {
                            //创建Page
                            val page = TextPage()
                            page.position = pages.size
                            mContext?.let {
                                page.title = StringUtils.convertCC(chapter.title, it)
                            }
                            page.lines = ArrayList()
                            page.titleLines = titleLinesCount
                            pages.add(page)
                            //重置Lines
                            lines.clear()
                            rHeight = mVisibleHeight
                            titleLinesCount = 0
                            continue
                        }
                        //测量一行占用的字节数
                        if (showTitle) {
                            mTitlePaint?.let {
                                wordCount = StringUtils.getWordCount(
                                    paragraph,
                                    it,
                                    mVisibleWidth.toFloat()
                                )
                            }
                        } else {
                            mTextPaint?.let {
                                wordCount = StringUtils.getWordCount(
                                    paragraph,
                                    it,
                                    mVisibleWidth.toFloat()
                                )
                            }
                        }

                        subStr = paragraph.substring(0, wordCount)
                        if (subStr != "\n") {
                            //将一行字节，存储到lines中
                            lines.add(subStr)
                            //设置段落间距
                            if (showTitle) {
                                titleLinesCount += 1
                                rHeight -= mTitleInterval
                            } else {
                                rHeight -= mTextInterval
                            }
                        }

                        //裁剪
                        paragraph = paragraph.substring(wordCount)
                    }

                    //增加段落的间距
                    if (!showTitle && lines.size != 0) {
                        rHeight = rHeight - mTextSize + mTextInterval
                    }

                    if (showTitle) {
                        rHeight = rHeight - mTitlePara + mTitleInterval
                        showTitle = false
                    }

                } else {
                    break
                }
            } while (true)


            if (lines.size != 0) {
                //创建Page
                val page = TextPage()
                page.position = pages.size
                mContext?.let {
                    page.title = StringUtils.convertCC(chapter.title, it)
                }
                page.lines = ArrayList()
                page.titleLines = titleLinesCount
                pages.add(page)
                //重置Lines
                lines.clear()
            }
        } catch (e: java.lang.Exception) {
            LogUtils.e("PageLoader", "loadPages error is $e")
        } finally {
            IOUtils.close(chapterReader)
        }

        return pages
    }
}