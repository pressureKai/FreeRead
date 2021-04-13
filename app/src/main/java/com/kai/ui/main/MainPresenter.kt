package com.kai.ui.main

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.utils.LogUtils
import com.kai.entity.User
import com.kai.model.book.BookRepository
import com.kai.model.user.UserRepository
import io.reactivex.rxjava3.core.Observable

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    MainActivity - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class MainPresenter : BasePresenter<MainContract.View>(), MainContract.Presenter {
    var mBookRepository: BookRepository = BookRepository.get()
    var mUserRepository: UserRepository = UserRepository.get()

    /**
     * des 加载书籍推荐列表
     */
    override fun loadBookRecommend() {
        mBookRepository.getBookRecommend()?.let { observable ->
            observable.doOnError {

            }.doOnComplete {

            }.subscribe { list ->
                getView()?.onLoadBookRecommend(list)
            }
        }
    }

    override fun getLoginCurrentUser() {
        try {
            getView()?.onGetLoginUser(mUserRepository.getCurrentUser())
        }catch (e:Exception){
            LogUtils.e("MainPresenter",e.toString())
        }
    }
}