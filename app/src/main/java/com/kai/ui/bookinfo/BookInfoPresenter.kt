package com.kai.ui.bookinfo

import com.kai.base.mvp.base.BasePresenter
import com.kai.model.book.BookRepository
import com.kai.ui.rankingdetail.RankingDetailContract

class BookInfoPresenter: BasePresenter<BookInfoContract.View>(), BookInfoContract.Presenter  {
    private val bookRepository: BookRepository = BookRepository.get()
    override fun getBookDetail(url: String) {

    }
}