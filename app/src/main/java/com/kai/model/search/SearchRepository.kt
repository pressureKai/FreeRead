package com.kai.model.search

import com.kai.crawler.entity.book.SearchBook
import com.kai.entity.SearchHistory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchRepository private constructor(
        private val localSearchDataSource: LocalSearchDataSource,
        private val remoteSearchDataSource: RemoteSearchDataSource
) : SearchDataSource {
    companion object{
        private var instance: SearchRepository?= null
            get() {
                if(field == null){
                    field = SearchRepository(LocalSearchDataSource(), RemoteSearchDataSource())
                }
                return field
            }


        @Synchronized
        fun get(): SearchRepository {
            return instance!!
        }
    }
    override fun search(keyword: String): Observable<List<SearchBook>>? {
        return remoteSearchDataSource.search(keyword)
    }

    override fun recommend(): Observable<List<SearchHistory>> {
        return Observable.create<List<SearchHistory>> { firstRecommend ->
            localSearchDataSource.recommend().subscribe {
                firstRecommend.onNext(it)
            }
            remoteSearchDataSource.recommend().subscribe {
                firstRecommend.onNext(it)
            }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun history(): Observable<List<SearchHistory>> {
        return localSearchDataSource.history()
    }
}