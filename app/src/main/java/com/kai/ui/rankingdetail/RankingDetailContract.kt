package com.kai.ui.rankingdetail

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend

class RankingDetailContract {
    interface View : IView {
        fun onRanking(recommends:ArrayList<BookRecommend>)
    }

    interface Presenter {
        fun ranking(type: Int,url:String)
    }
}