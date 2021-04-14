package com.kai.model.user

import com.kai.common.utils.LogUtils
import com.kai.database.CustomDatabase
import com.kai.entity.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception
import java.lang.NullPointerException

class LocalUserDataSource : UserDataSource {
    override fun getCurrentUser(): Observable<User> {
        return Observable.create<User> {
            var user: User? = null
            try {
                val userList = CustomDatabase.get().userDao().getUserByOnLine(true)
                LogUtils.e("LocalUserDataSource","userList size is ${userList.size}")
                if (userList.isNotEmpty()) {
                    user = userList.first()
                    it.onNext(user)
                } else {
                    it.onError(NullPointerException())
                }
            } catch (e: Exception) {
                it.onError(e)
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

    override fun getUserByAccount(account: String): Observable<List<User>> {
        return Observable.create<List<User>> {
            try {
                val userList = CustomDatabase.get().userDao().getUserByAccount(account)
                it.onNext(userList)
            } catch (e: Exception) {
                LogUtils.e("LocalUserDataSource", "$e")
            }
            it.onComplete()
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun login(user: User): Observable<User> {
        return Observable.create<User> {
            try {
                val userList = CustomDatabase.get().userDao().getUserList()
                for (value in userList) {
                    value.onLine = false
                    updateUser(value)
                }
                user.onLine = true
                updateUser(user)
                it.onNext(user)
            } catch (e: Exception) {
                it.onError(e)
                LogUtils.e("LocalUserDataSource", "$e")
            }
            it.onComplete()
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
    }
}