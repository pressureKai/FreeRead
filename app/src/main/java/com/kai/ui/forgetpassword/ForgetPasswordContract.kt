package com.kai.ui.forgetpassword

import com.kai.base.mvp.base.IView
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.entity.User

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-注册
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class ForgetPasswordContract {
    interface View : IView {
        /**
         * #  根据账号获取用户信息回调
         * @param [entity] 回调基类 data 为 User
         * @date 2021/4/13
         */
        fun onGetUserByAccount(entity: BaseEntity<User>)

        /**
         * #  重置密码回调
         * @param [entity] 回调基类 data 为 User
         * @date 2021/4/13
         */
        fun onUpdatePassword(entity: BaseEntity<User>)
    }

    interface Presenter {
        /**
         * #  根据账号获取用户信息
         * @param [account] 账号
         * @date 2021/4/13
         */
        fun getUserByAccount(account: String)


        /**
         * #  重置密码
         * @param [account]  账号
         * @param [password] 新的密码
         * @date 2021/4/13
         */
        fun updatePassword(account: String, password: String)
    }
}