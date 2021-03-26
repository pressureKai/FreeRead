package com.kai.model.book

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

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
            var count = 0
            while (count < 60){
                count++
                Thread.sleep(1000)
                val randomInt = Random().nextInt(5000)
                arrayList.add(randomInt.toString())
                it.onNext(arrayList)
            }
            it.onComplete()
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }
}