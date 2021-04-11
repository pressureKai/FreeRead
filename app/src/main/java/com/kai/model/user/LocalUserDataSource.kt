package com.kai.model.user

import com.kai.common.utils.LogUtils
import com.kai.database.CustomDatabase
import com.kai.entity.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

class LocalUserDataSource : UserDataSource {
    override fun getCurrentUser(): Observable<User> {
        return Observable.create<User> {
            var user: User ?= null
            try {
                val userList = CustomDatabase.get().userDao().getUserList()
                user = userList.first()
            }catch (e :Exception){
                LogUtils.e("RegisterPresenter","register account on model error is $e")
            }


            user?.let { user ->
                it.onNext(user)
            }?: kotlin.run {
                it.onNext(User())
            }
            it.onComplete()
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun updateUser(user: User) {
        CustomDatabase.get().userDao().updateUser(user)
    }

    override fun deleteUser(user: User) {
        CustomDatabase.get().userDao().deleteUser(user)
    }

    override fun insertUser(user: User) {
        CustomDatabase.get().userDao().insertUser(user)
    }
}