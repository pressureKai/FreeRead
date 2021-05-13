package com.kai.bookpage.page

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.kai.bookpage.model.*
import com.kai.bookpage.utils.StringUtils
import com.kai.common.constant.Constant
import com.kai.common.utils.IOUtils
import com.kai.common.utils.LogUtils
import com.kai.common.utils.ScreenUtils
import io.reactivex.rxjava3.disposables.Disposable
import org.w3c.dom.Text
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


    private var mChapterList: ArrayList<BookChapterBean> = ArrayList()
    var mCoolBook: CoolBookBean? = null
    private var mPageChangeListener: OnPageChangeListener? = null
    private var mContext: Context? = null
    private var mCurPage: TextPage? = null
    private var mPrePageList: ArrayList<TextPage> = ArrayList()
    private var mCurPageList: ArrayList<TextPage> = ArrayList()
    private var mNextPageList: ArrayList<TextPage> = ArrayList()


    private var mPageView: PageView? = null


    private var mBatteryPaint: Paint? = null
    private var mTipPaint: Paint? = null
    private var mBgPaint: Paint? = null
    private var mTextPaint: Paint? = null


    private var mSettingManager: ReadSettingManager? = null

    private var mCancelPage: TextPage? = null
    private var mBookRecord: BookRecordBean? = null


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

    var mCurrentChapterPosition = 0
    private var mLastChapterPosition = 0


    private var mTipTitleMargin = 0
    private var preLoadChapterSize = 3


    constructor(pageView: PageView, coolBookBean: CoolBookBean) {
        mPageView = pageView
        mContext = pageView.context
        mCoolBook = coolBookBean
        mChapterList = ArrayList<BookChapterBean>()



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

        var textSize = ScreenUtils.spToPx(ReadSettingManager.defaultTextSize)
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


    fun reInitTextPaint(textPaint: TextPaint){
        mTextPaint = textPaint
        LogUtils.e("Paint","reInit finish")
        mTextPaint?.color = mTextColor
        mTextPaint?.textSize = mTextSize.toFloat()
        mTextPaint?.isAntiAlias = true
    }

    fun reInitTipPaint(tipPaint: TextPaint){
        mTipPaint = tipPaint
        mTipPaint?.color = mTextColor
        mTipPaint?.textAlign = Paint.Align.LEFT
        mTipPaint?.textSize = ScreenUtils.spToPx(DEFAULT_TIP_SIZE).toFloat()
        //抗锯齿
        mTipPaint?.isAntiAlias = true
        //是否开启次像素级的抗锯齿（更好的抗锯齿效果）
        mTipPaint?.isSubpixelText = true
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
    fun setPageStyle(pageStyle: PageStyle) {
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
    }
    /****************parse fun********************/
    /**
     * 从资源获取字符流，初始化页面列表
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
        return mCurPageList.isNotEmpty()
    }

    /****************draw fun********************/
    /**
     *绘制背景
     */
    private fun drawBackground(bitmap: Bitmap, isUpdate: Boolean) {
        val canvas = Canvas(bitmap)
        val tipMarginHeight = ScreenUtils.dpToPx(3)

        val topTipMarginHeight = ScreenUtils.getStatusBarHeight()
        if (!isUpdate) {
            //绘制背景
            canvas.drawColor(mBgColor)
            if (mChapterList.isNotEmpty()) {
                //初始化标题的参数
                //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部位置
                var tipTop = 0f
                mTipPaint?.let {
                    tipTop = topTipMarginHeight - it.fontMetrics.top
                    mTipTitleMargin = tipTop.toInt() + it.textSize.toInt()
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
        LogUtils.e("PageLoader","update time $time")
        mTipPaint?.let {
            val y = mDisplayHeight - it.fontMetrics.bottom - tipMarginHeight
            val x = outFrameLeft - it.measureText(time)
            canvas.drawText(time, x, y, it)
        }
    }

    /**
     * 获取当前章节的最后一页
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
     * 重新计算指定章节页面
     */
    private fun dealLoadPageList(currentChapterPosition: Int) {
        try {
            mCurPageList.clear()
            loadPageList(currentChapterPosition)?.let {
                //加载指定位置章节
                if(it.size > 0){
                    mCurPageList.addAll(it)
                }
            }

            if (mCurPageList.isEmpty()) {
                mStatus = STATUS_EMPTY
                //添加一个空数据
                val page = TextPage()
                page.lines = ArrayList(1)
                mCurPageList.add(page)
            } else {
                //数据不为空时，状态为加载完成
                mStatus = STATUS_FINISH
            }

        } catch (e: Exception) {
            e.printStackTrace()
            mCurPageList.clear()
            //发生错误加载数据状态为错误状态
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
     * 回调处理方法，通知章节变化以及总的页数变化
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
        if (mCurrentChapterPosition + 1 >= mChapterList.size) {
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
     * # 设置书籍数据变化监听器
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
    fun getChapterCategory(): List<BookChapterBean> {
        return mChapterList
    }



    fun setChapterCategory(chapterList: ArrayList<BookChapterBean>){
        mChapterList.clear()
        mChapterList.addAll(chapterList)
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
        mVisibleWidth = mDisplayWidth - mMarginWidth
        mVisibleHeight = mDisplayHeight - mMarginHeight
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
            //it TextPage 中的position为页码
            position = it.position - 1
        }
        if (position < 0 || position > mCurPageList.size - 1) {
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
        //mStatus != FINISH 代表正在加载下一章节
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

        //mStatus != STATUS_FINISH  数据为空或加载错误
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
        if(mStatus == STATUS_EMPTY && mCurPage!!.lines.size > 0){
            mStatus = STATUS_FINISH
        } else if(mCurPage!!.lines.size == 0){
            mStatus = STATUS_EMPTY
        }
        mPageView?.drawNextPage()
        return true
    }



    /**
     * 打开当前位置章节
     */
    fun openChapter() {
        mPrePageList.clear()
        mNextPageList.clear()
        isFirstOpen = false
        mPageView?.let {
            if (!it.isPrepare()) {
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
            mCurPage = getCurrentPage(0)
//            if (!isChapterOpen) {
//
                  //如果章节从未打开
//                mBookRecord?.let {
//                    var position = it.pagePos
//
//                    //防止记录的页号,大于当前的最大页号
//                    if (position >= mCurPageList.size) {
//                        position = mCurPageList.size - 1
//                    }
//                    mCurPage = getCurrentPage(position)
//                    mCancelPage = mCurPage
//                    //切换状态
//                    isChapterOpen = true
//                }
//
//            }
        } else {
            mCurPage = TextPage()
        }
        preLoadChapterData()
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

        mChapterList.clear()
        mCurPageList.clear()
        mNextPageList.clear()

        mPageView = null
        mCurPage = null
    }

    /****************abstract fun********************/
    interface OnPageChangeListener {
        fun onChapterChange(pos: Int)
        fun requestChapters(requestChapters: List<BookChapterBean>)
        fun onCategoryFinish(chapter: List<BookChapterBean>)
        fun onPageCountChange(count: Int)
        fun onPageChange(pos: Int)
        fun onPreLoadChapter(mCurrentChapterPosition: Int,loadSize: Int)
    }

    /**
     * 判断章节数据是否存在
     */
    protected abstract fun hasChapterData(chapter: BookChapterBean): Boolean

    /**
     * 获取章节的文件流
     */
    @Throws(Exception::class)
    protected abstract fun getChapterReader(chapter: BookChapterBean): BufferedReader


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
            mBookRecord?.bookId = it.bookId
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
     */
    private fun preLoadNextChapter() {
        mNextPageList.clear()
        val nextChapter = mCurrentChapterPosition + 1
        //如果不存在下一章，且下一章没有数据，则不进行加载
        if (!hasNextChapter() ||
            !hasChapterData(mChapterList[nextChapter])) {
            return
        } else {
            loadPageList(nextChapter)?.let {
                //加载指定位置章节
                if(it.size > 0){
                    mNextPageList.addAll(it)
                }
            }
        }
        preLoadChapterData()
    }



    private fun preLoadChapterData(){
        mPageChangeListener?.onPreLoadChapter(mCurrentChapterPosition,preLoadChapterSize)
    }


    /****************unConfirm fun********************/



    private fun drawContent(bitmap: Bitmap){
        val canvas = Canvas(bitmap)
        if (mPageMode == PageMode.SCROLL) {
            canvas.drawColor(mBgColor)
        }

        LogUtils.e("PageLoader","mCurPage is null ${mCurPage == null} mStatus is $mStatus")

        //mStatus 当加载到下一章节为空时，返回上一章节 status 状态未改变
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
                    top = -it.fontMetrics.top * 2
                }
            } else {
                mTextPaint?.let {
                    top = mMarginHeight - it.fontMetrics.top * 2
                }
            }


            //设置总距离
            var interval = 0
            var para = 0
            var str = ""

            mTextPaint?.let {
                interval = mTextInterval + it.textSize.toInt()
                para = mTextPara + it.textSize.toInt()
            }

            LogUtils.e("PageLoader","mCurPage is null ${mCurPage == null}")
            mCurPage?.let {
                for (value in it.lines){
                    LogUtils.e("PageLoader","lines is $value")
                }
                for (index in 0.until(it.lines.size)) {
                    str = it.lines[index]
                    if(str.replace("\n","").isNotEmpty()){
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
        mPrePageList.clear()
        mPrePageList.addAll(mCurPageList)

        //是否下一章数据已经预加载
        if (mNextPageList.isNotEmpty() && mNextPageList.first().lines.size>0) {
            mCurPageList.clear()
            mCurPageList.addAll(mNextPageList)
            mNextPageList.clear()
            //回调
            chapterChangeCallback()
        } else {
            //处理页面解析
            LogUtils.e("PageLoader","parse next chapter chapter position is $nextChapterPosition")
            dealLoadPageList(nextChapterPosition)
        }
        //预加载下一页面
        preLoadNextChapter()
        if (mCurPageList.isNotEmpty() && mCurPageList.first().lines.size > 0) {
            mStatus = STATUS_FINISH
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

        mPrePageList.clear()
        mPrePageList.addAll(mCurPageList)
        mCurPageList.clear()
        mCurPageList.addAll(mNextPageList)
        mNextPageList.clear()

        chapterChangeCallback()
        mCurPage = getCurrentPage(0)
        mCancelPage = null
    }


    /**
     * 解析上一章的数据
     */
    private fun parsePreChapter(): Boolean {
        //记录章节位置
        val preChapterPosition = mCurrentChapterPosition - 1
        mLastChapterPosition = mCurrentChapterPosition
        mCurrentChapterPosition = preChapterPosition


        LogUtils.e("PageLoader","preChapterPosition $preChapterPosition mLastChapterPosition $mLastChapterPosition mCurrentChapterPosition $mCurrentChapterPosition" )
        //当前章节缓存为下一章
        mNextPageList.clear()
        mNextPageList.addAll(mCurPageList)

        LogUtils.e("PageLoader","mPrePageList size is ${mPrePageList.size}")
        if (mPrePageList.isNotEmpty()) {
            mCurPageList.clear()
            mCurPageList.addAll(mPrePageList)
            mPrePageList.clear()
            chapterChangeCallback()
        } else {
            dealLoadPageList(preChapterPosition)
        }
        LogUtils.e("PageLoader","mCurPageList size is ${mCurPageList.size}")
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
        }

        return canTurn
    }


    /**
     * 取消下一页
     */
    private fun cancelNextChapter() {
        val temp = mLastChapterPosition
        mLastChapterPosition = mCurrentChapterPosition
        mCurrentChapterPosition = temp

        mNextPageList.clear()
        mNextPageList.addAll(mCurPageList)
        mCurPageList.clear()
        mCurPageList.addAll(mPrePageList)
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
    private fun loadPages(chapter: BookChapterBean, chapterReader: BufferedReader): ArrayList<TextPage> {
        //生成的页面
        val pages: ArrayList<TextPage> = ArrayList()
        //使用流的方式加载
        val lines: ArrayList<String> = ArrayList()
        //视图可用高度
        var mRealHeight = mVisibleHeight
        //默认展示标题
        var paragraph = ""
        var canReadLine = true
        try {
            while(canReadLine){
                val readLine = chapterReader.readLine()
                canReadLine = readLine != null
                if(canReadLine){
                    paragraph = readLine
                } else {
                    break
                }
                try {
                    if (paragraph.isNotEmpty()) {
                        mContext?.let {
                            //繁简转换
                            paragraph = StringUtils.convertCC(paragraph, it)
                        }

                        var wordCount = 0
                        var subStr = ""
                        while (paragraph.isNotEmpty()) {
                            //当前空间，是否容得下一行文字
                            mTextPaint?.let {
                                mRealHeight -= it.textSize.toInt()
                            }
                            //一页已经填充满了，创建TextPage
                            if (mRealHeight <= 0) {
                                //创建Page
                                val page = TextPage()
                                page.position = pages.size
                                mContext?.let {
                                    page.title = StringUtils.convertCC(chapter.title, it)
                                }
                                page.lines.addAll(lines)
                                pages.add(page)
                                //重置Lines
                                lines.clear()
                                mRealHeight = mVisibleHeight
                                continue
                            }
                            //测量一行占用的字节数
                            mTextPaint?.let {
                                wordCount = StringUtils.getWordCount(
                                        paragraph,
                                        it,
                                        mVisibleWidth.toFloat()
                                )
                            }

                            subStr = paragraph.substring(0, wordCount)
                            if (subStr != "\n") {
                                //将一行字节，存储到lines中
                                lines.add(subStr)
                                //设置段落间距
                                mRealHeight -= mTextInterval
                            }

                            //裁剪
                            paragraph = paragraph.substring(wordCount)
                        }

                        //增加段落的间距
                        if (lines.size != 0) {
                            mRealHeight = mRealHeight - mTextSize + mTextInterval
                        }

                    } else {
                        break
                    }
                }catch (e: java.lang.Exception){
                    LogUtils.e("PageLoader", "load pageList error is $e")
                    break
                }
            }

            if (lines.size != 0) {
                //创建Page
                val page = TextPage()
                page.position = pages.size
                mContext?.let {
                    page.title = StringUtils.convertCC(chapter.title, it)
                }
                page.lines.addAll(lines)
                pages.add(page)
                //重置Lines
                lines.clear()
                mRealHeight = mVisibleHeight
            }
        } catch (e: java.lang.Exception) {
            LogUtils.e("PageLoader", "loadPages error is $e")
        } finally {
            IOUtils.close(chapterReader)
        }
        LogUtils.e("PageLoader", "pages is ${pages.size}")
        return pages
    }

     fun getTextSize(): Int{
        return mTextSize
    }


    fun getSettingManager(): ReadSettingManager?{
        return  mSettingManager
    }
}