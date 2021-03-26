package com.kai.model.book

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 *
 * @ProjectName:    app
 * @Description:    加载网络数据
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/26 11:50
 */
class RemoteBookDataSource :BookDataSource {
    override fun getBookRecommend(): Observable<List<String>>? {
        return  Observable.create<List<String>> {
            val arrayList = ArrayList<String>()
            Thread.sleep(3000)
            arrayList.add("from remote")
            it.onNext(arrayList)
            it.onComplete()
        }.subscribeOn(Schedulers.newThread()).subscribeOn(AndroidSchedulers.mainThread())
    }
}