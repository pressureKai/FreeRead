package com.kai.ui.main

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.common.utils.LogUtils
import com.kai.entity.User
import com.kai.model.book.BookRepository
import com.kai.model.user.UserRepository

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    MainActivity - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class MainPresenter : BasePresenter<MainContract.View>(), MainContract.Presenter {
    var mUserRepository: UserRepository = UserRepository.get()

    /**
     * des 加载书籍推荐列表
     */
    override fun loadBookRecommend() {
    }

    override fun getLoginCurrentUser() {
        try {
            val baseUserEntity = BaseEntity<User>()
            mUserRepository.getCurrentUser()
                    .doOnError {
                        baseUserEntity.code = BaseEntity.ENTITY_FAIL_CODE
                        getView()?.onGetLoginUser(baseUserEntity)
                    }
                    .subscribe {
                        baseUserEntity.code = BaseEntity.ENTITY_SUCCESS_CODE
                        baseUserEntity.data = it
                        getView()?.onGetLoginUser(baseUserEntity)
                    }

        } catch (e: Exception) {
            LogUtils.e("MainPresenter", e.toString())
        }
    }


}