package com.kai.ui.history

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend

class HistoryContract {
    interface View : IView {
        fun onHistory(list:ArrayList<BookRecommend>)
    }

    interface Presenter {
        fun getHistoryByType(type: Int)
    }
}