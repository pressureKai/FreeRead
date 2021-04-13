package com.kai.ui.forgetpassword

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.entity.User
import com.kai.model.user.UserRepository
import java.lang.Exception

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    注册- Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class ForgetPasswordPresenter : BasePresenter<ForgetPasswordContract.View>(), ForgetPasswordContract.Presenter {
    private var userRepository: UserRepository = UserRepository.get()
    override fun getUserByAccount(account: String) {
        val baseEntity = BaseEntity<User>()
        userRepository.getUserByAccount(account)
                .doOnError {
                    baseEntity.code = BaseEntity.ENTITY_FAIL_CODE
                    getView()?.onGetUserByAccount(baseEntity)
                }
                .subscribe {
                    if(it.isEmpty()){
                        baseEntity.code = BaseEntity.ENTITY_FAIL_CODE
                        getView()?.onGetUserByAccount(baseEntity)
                    } else {
                        baseEntity.data = it.first()
                        baseEntity.code = BaseEntity.ENTITY_SUCCESS_CODE
                        getView()?.onGetUserByAccount(baseEntity)
                    }
                }
    }

    override fun updatePassword(account: String,password: String) {
        val baseEntity = BaseEntity<User>()
        userRepository.getUserByAccount(account)
                .doOnError {
                    baseEntity.code = BaseEntity.ENTITY_FAIL_CODE
                    getView()?.onUpdatePassword(baseEntity)
                }
                .subscribe {
                    if(it.isEmpty()){
                        baseEntity.code = BaseEntity.ENTITY_FAIL_CODE
                        getView()?.onUpdatePassword(baseEntity)
                    } else {
                        try {
                            for(value in it){
                                value.password = password
                                userRepository.updateUser(value)
                            }
                            baseEntity.data = it.first()
                            baseEntity.code = BaseEntity.ENTITY_SUCCESS_CODE
                            getView()?.onUpdatePassword(baseEntity)
                        }catch (e:Exception){
                            baseEntity.code = BaseEntity.ENTITY_FAIL_CODE
                            getView()?.onUpdatePassword(baseEntity)
                        }

                    }
                }
    }

}