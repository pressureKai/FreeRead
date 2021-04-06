package com.kai.ui.fonts

import com.kai.base.mvp.base.BasePresenter
import com.kai.model.book.BookRepository

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    MainActivity - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class FontsPresenter : BasePresenter<FontsContract.View>(), FontsContract.Presenter {
    var mBookRepository: BookRepository = BookRepository.get()
    override fun loadFonts() {

    }
}