package com.kai.model.book

import io.reactivex.rxjava3.core.Observable

/**
 *
 * @ProjectName:    app-bookpage
 * @Description:    统筹调用本地数据与网络数据(Presenter层通过此对象获取数据) 单例模式节省资源
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/26 13:39
 */
class BookRepository private constructor(
    private val localBookDataSource: LocalBookDataSource,
    private val remoteBookDataSource: RemoteBookDataSource
) : BookDataSource {

    companion object {
        private var instance: BookRepository? = null
            get() {
                if (field == null) {
                    field = BookRepository(LocalBookDataSource(), RemoteBookDataSource())
                }
                return field
            }

        @Synchronized
        fun get(): BookRepository {
            return instance!!
        }
    }

    override fun getBookRecommend(): Observable<List<String>>? {
        return Observable.create { emitter ->
            val arrayList = ArrayList<String>()
            val localRecommend = localBookDataSource.getBookRecommend()
            localRecommend?.let { local ->
                local
                    .doOnComplete {
                        if(arrayList.size > 0){
                            emitter.onNext(arrayList)
                        }
                        val remoteBookRecommend = remoteBookDataSource.getBookRecommend()
                        remoteBookRecommend?.let { remote ->
                            remote
                                .doOnError {
                                    emitter.onError(it)
                                }
                                .doOnComplete {
                                    emitter.onComplete()
                                }
                                .subscribe {
                                    arrayList.clear()
                                    arrayList.addAll(it)
                                    emitter.onNext(arrayList)
                                }
                        }
                    }.subscribe { list ->
                        arrayList.clear()
                        arrayList.addAll(list)
                    }
            }
        }

    }


}