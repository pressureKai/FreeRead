package com.kai.ui.fragments.shelf

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase

class ShelfPresenter: BasePresenter<ShelfContract.View>(), ShelfContract.Presenter {
    override fun shelf() {
        val bookRecommendByBookShelf =
            BookDatabase.get().bookDao().getBookRecommendByBookShelf(true)
        getView()?.onShelf(bookRecommendByBookShelf as ArrayList<BookRecommend>)
    }

}