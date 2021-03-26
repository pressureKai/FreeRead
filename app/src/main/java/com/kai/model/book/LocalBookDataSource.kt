package com.kai.model.book

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 *
 * @ProjectName:    app
 * @Description:    加载本地缓存数据  subscribeOn 数据生成线程,observeOn 数据订阅线程
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/26 11:51
 */
class LocalBookDataSource :BookDataSource{
    override fun getBookRecommend(): Observable<List<String>>? {
        return Observable.create<List<String>> {
            val arrayList = ArrayList<String>()
            Thread.sleep(3000)
            arrayList.add("from local")
            it.onNext(arrayList)
            it.onComplete()
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }
}