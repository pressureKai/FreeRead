package com.kai.base.bookpage.page

import android.content.Context
import android.graphics.Paint
import com.kai.base.bookpage.model.BookRecordBean
import com.kai.base.bookpage.model.CoolBookBean
import com.kai.base.bookpage.model.TextChapter
import com.kai.base.bookpage.model.TextPage
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


    interface OnPageChangeListener{
        fun onChapterChange(pos :Int)
        fun requestChapters(requestChapters :List<TextChapter>)
        fun onCategoryFinish(chapter: List<TextChapter>)
        fun onPageCountChange(count :Int)
        fun onPageChange(pos :Int)
    }
}