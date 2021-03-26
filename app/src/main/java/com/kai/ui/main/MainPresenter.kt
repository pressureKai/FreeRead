package com.kai.ui.main

import com.kai.base.mvp.base.BasePresenter
import com.kai.model.book.BookRepository

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    主页面 - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class MainPresenter : BasePresenter<MainContract.View>(), MainContract.Presenter {
    var mBookRepository: BookRepository = BookRepository.get()

    /**
     * des 加载书籍推荐列表
     */
    override fun loadBookRecommend() {
        mBookRepository.getBookRecommend()?.let { observable ->
            observable.doOnError {

            }.doOnComplete {

            }.subscribe { list ->
                getView().onLoadBookRecommend(list)
            }
        }
    }
}