package com.kai.base.bookpage.page

import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import com.kai.base.bookpage.model.BookRecordBean
import com.kai.base.bookpage.model.CoolBookBean
import com.kai.base.bookpage.model.TextChapter
import com.kai.base.bookpage.model.TextPage
import com.kai.base.utils.ScreenUtils
import io.reactivex.rxjava3.disposables.Disposable

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



    private var mChapterList : List<TextChapter> = ArrayList()
    private var mCoolBook : CoolBookBean ?= null
    private var mPageChangeListener : OnPageChangeListener ?= null
    private var mContext : Context ?= null
    private var mCurPage : TextPage ?= null
    private var mPrePageList : List<TextPage> = ArrayList()
    private var mCurPageList : List<TextPage> = ArrayList()
    private var mNextPageList : List<TextPage> = ArrayList()


    private var mPageView :PageView ?= null


    private var mBatteryPaint : Paint ?= null
    private var mTipPaint :Paint ?= null
    private var mTitlePaint :Paint ?= null
    private var mBgPaint :Paint ?= null
    private var mTextPaint :Paint ?= null


    private var mSettingManager :ReadSettingManager ?= null

    private var mCancelPage :TextPage ?= null
    private var mBookRecord :BookRecordBean ?= null

    private var mPreLoadDisposable :Disposable ?= null

    open var mStatus  = STATUS_LOADING
    open var isChapterListPrepare = false


    private var isChapterOpen = false
    private var isFirstOpen = true

    private var isClose = false
    private var mPageMode : PageMode ?= null
    private var mPageStyle :PageStyle ?= null
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



    constructor(pageView: PageView,coolBookBean: CoolBookBean){
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
    private fun initData(){
        mSettingManager =  ReadSettingManager.getInstance()

        mPageMode =  mSettingManager?.getPageMode()
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
    private fun initPaint(){
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


    }

    //初始化PageView
    private fun initPageView(){

    }

    //初始化书籍
    private fun prepareBook(){

    }

    /**
     * 设置与文字相关的参数
     */
    private fun setUpTextParams(textSize :Int){
        mTextSize = textSize
        mTitleSize = textSize +ScreenUtils.spToPx(EXTRA_TITLE_SIZE)
        mTextInterval = mTextSize / 2
        mTitleInterval = mTextSize / 2
        mTextPara = mTextSize
        mTitlePara = mTitleSize
    }

    interface OnPageChangeListener{
        fun onChapterChange(pos :Int)
        fun requestChapters(requestChapters :List<TextChapter>)
        fun onCategoryFinish(chapter: List<TextChapter>)
        fun onPageCountChange(count :Int)
        fun onPageChange(pos :Int)
    }
}