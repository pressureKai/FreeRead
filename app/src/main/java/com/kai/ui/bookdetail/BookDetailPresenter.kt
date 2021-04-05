package com.kai.ui.bookdetail

import com.kai.base.mvp.base.BasePresenter
import com.kai.crawler.entity.book.BookDetail
import com.kai.model.book.BookRepository

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    MainActivity - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class BookDetailPresenter : BasePresenter<BookDetailContract.View>(), BookDetailContract.Presenter {
    var mBookRepository: BookRepository = BookRepository.get()
    override fun loadBookDetail() {

    }


}