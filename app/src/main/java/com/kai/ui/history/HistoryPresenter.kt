package com.kai.ui.history

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase

class HistoryPresenter:  BasePresenter<HistoryContract.View>(), HistoryContract.Presenter  {
    override fun getHistoryByType(type: Int) {
        if(type == HistoryActivity.LIKE){
            val bookRecommendByBookLike =
                BookDatabase.get().bookDao().getBookRecommendByBookLike(true)
            getView()?.onHistory(bookRecommendByBookLike as ArrayList<BookRecommend>)
        } else {
            val bookRecommendByBookLike =
                BookDatabase.get().bookDao().getBookRecommendByBookRead(true)
            getView()?.onHistory(bookRecommendByBookLike as ArrayList<BookRecommend>)
        }
    }

}