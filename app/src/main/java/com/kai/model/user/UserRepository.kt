package com.kai.model.user

import com.kai.entity.User
import io.reactivex.rxjava3.core.Observable

class UserRepository private  constructor(private val localUserDataSource: LocalUserDataSource): UserDataSource{
   companion object{
       private var instance: UserRepository ?= null
       get() {
           if(field == null){
               field = UserRepository(LocalUserDataSource())
           }
           return field
       }


       @Synchronized
       fun get(): UserRepository{
           return instance!!
       }
   }

    override fun getCurrentUser(): Observable<User> {
        return localUserDataSource.getCurrentUser()
    }

    override fun updateUser(user: User) {
        localUserDataSource.updateUser(user)
    }

    override fun deleteUser(user: User) {
         localUserDataSource.deleteUser(user)
    }

    override fun insertUser(user: User) {
         localUserDataSource.insertUser(user)
    }

}