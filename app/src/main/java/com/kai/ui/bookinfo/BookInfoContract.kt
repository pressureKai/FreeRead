package com.kai.ui.bookinfo

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend

class BookInfoContract {
    interface View : IView {
        fun onBookDetail(recommend: BookRecommend)
        fun onRecommendList(list: ArrayList<BookRecommend>)
        fun onLikeOption(code:Int)
        fun onShelfOption(code:Int)
        fun onGetBookLike(like:Boolean)
        fun onGetBookShelf(shelf:Boolean)
    }

    interface Presenter {
        fun getBookDetail(url:String)
        fun recommendList(url: String)
        fun likeOption(bookUrl:String,like:Boolean)
        fun shelfOption(bookUrl: String,shelf:Boolean)
        fun getBookLike(bookUrl:String)
        fun getBookShelf(bookUrl: String)
    }
}