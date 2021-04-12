package com.kai.ui.login

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.eventBusEntity.BaseEntity
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
class LoginPresenter : BasePresenter<LoginContract.View>(), LoginContract.Presenter {
    private var userRepository: UserRepository = UserRepository.get()
    override fun login(account: String, password: String) {
          userRepository.getUserByAccount(account).subscribe {
                 val baseEntity = BaseEntity<User>()
                 if(it.isNotEmpty()){
                     var loginSuccess = false
                     for(value in it){
                         if(value.password == password){
                             userRepository.login(user = value)
                             baseEntity.data = value
                             baseEntity.code = LoginActivity.LOGIN_SUCCESS
                             getView()?.onLogin(baseEntity)
                             loginSuccess = true
                             break
                         }
                     }
                     if(!loginSuccess){
                         baseEntity.code = LoginActivity.LOGIN_FAIL_ERROR_PASSWORD
                         getView()?.onLogin(baseEntity)
                     }
                 } else {
                     baseEntity.code = LoginActivity.LOGIN_FAIL_NO_ACCOUNT
                     getView()?.onLogin(baseEntity)
                 }
          }
    }

}