package com.kai.ui.register

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.utils.LogUtils
import com.kai.entity.User
import com.kai.model.user.UserRepository

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    注册- Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class RegisterPresenter : BasePresenter<RegisterContract.View>(), RegisterContract.Presenter {
    private var userRepository: UserRepository = UserRepository.get()
    override fun register(account: String, password: String, question: String, answer: String) {
        userRepository.getCurrentUser()
            .doOnComplete {
                LogUtils.e("RegisterPresenter","start register complete")
            }
            .subscribe { user ->
            if(user.account.isEmpty()){
                val user = User()
                user.account = account
                user.password = password
                user.question = question
                user.answer = answer
                userRepository?.insertUser(user)
                LogUtils.e("RegisterPresenter","register account is ${user.account}")
            } else {
                LogUtils.e("RegisterPresenter","current account is ${user.account}")
            }
            getView()?.onRegister(0)
        }
    }
}