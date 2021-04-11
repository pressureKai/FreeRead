package com.kai.model.user

import com.kai.entity.User
import io.reactivex.rxjava3.core.Observable

interface UserDataSource {

    fun getCurrentUser(): Observable<User>

    fun updateUser(user: User)

    fun deleteUser(user: User)

    fun insertUser(user: User)
}