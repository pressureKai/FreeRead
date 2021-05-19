package com.kai.ui.bookinfo

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend

class BookInfoContract {
    interface View : IView {
        fun onBookDetail(recommend: BookRecommend)
    }

    interface Presenter {
        fun getBookDetail(url:String)
    }
}