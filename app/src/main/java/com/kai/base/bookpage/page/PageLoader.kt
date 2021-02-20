package com.kai.base.bookpage.page

import android.content.Context
import android.graphics.Paint
import com.kai.base.bookpage.model.CoolBookBean
import com.kai.base.bookpage.model.TextChapter
import com.kai.base.bookpage.model.TextPage

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

    interface OnPageChangeListener{
        fun onChapterChange(pos :Int)
        fun requestChapters(requestChapters :List<TextChapter>)
        fun onCategoryFinish(chapter: List<TextChapter>)
        fun onPageCountChange(count :Int)
        fun onPageChange(pos :Int)
    }
}