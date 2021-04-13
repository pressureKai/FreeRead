package com.kai.ui.main

import com.kai.base.mvp.base.IView
import com.kai.entity.User
import io.reactivex.rxjava3.core.Observable

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-约束MVP中的接口
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class MainContract {
    interface View : IView {
        fun onLoadBookRecommend(list: List<String>)

        /**
         * #  获取当前登录用户回调
         * @param [user] 可观测的Observable<User>
         * @date 2021/4/13
         */
        fun onGetLoginUser(user: Observable<User>)
    }

    interface Presenter {
        fun loadBookRecommend()

        /**
         * #  获取当前登录用户
         * @date 2021/4/13
         */
        fun getLoginCurrentUser()
    }
}