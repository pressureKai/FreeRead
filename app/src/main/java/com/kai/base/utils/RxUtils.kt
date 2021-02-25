package com.kai.base.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleSource
import io.reactivex.rxjava3.schedulers.Schedulers.io

class RxUtils {
    companion object{
        fun <T> toSimpleSingle(upStream :Single<T>):SingleSource<T>{
            return upStream.subscribeOn(io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        fun <T> toSimpleSingle(upStream :Observable<T>):ObservableSource<T>{
            return upStream.subscribeOn(io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}