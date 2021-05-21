package com.kai.ui.fragments.shelf

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookRecommend

class ShelfContract {

    interface View : IView {
        fun onShelf(recommends:ArrayList<BookRecommend>)
    }


    interface Presenter{
        fun shelf()
    }
}