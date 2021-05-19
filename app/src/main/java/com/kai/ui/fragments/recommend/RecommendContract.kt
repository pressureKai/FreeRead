package com.kai.ui.fragments.recommend

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend

class RecommendContract {

    interface View : IView {
        fun onBanner(arrayList: ArrayList<BookRecommend>)
        fun onRecommend(arrayList: ArrayList<ArrayList<BookRecommend>>)
    }


    interface Presenter{
        fun banner()
        fun recommend()
    }
}