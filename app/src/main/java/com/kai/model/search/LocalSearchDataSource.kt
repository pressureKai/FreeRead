package com.kai.model.search

import com.kai.crawler.entity.book.SearchBook
import com.kai.database.CustomDatabase
import com.kai.entity.SearchHistory
import io.reactivex.rxjava3.core.Observable

class LocalSearchDataSource : SearchDataSource {
    override fun search(keyword: String): Observable<List<SearchBook>>? {
        return null
    }

    override fun recommend(): Observable<List<SearchHistory>> {
        return Observable.create<List<SearchHistory>> {
            val historyList = CustomDatabase.get().searchHistoryDao().getHistoryList(true)
            it.onNext(historyList)
        }

    }

    override fun history(): Observable<List<SearchHistory>> {
        return Observable.create<List<SearchHistory>> {
            val historyList = CustomDatabase.get().searchHistoryDao().getHistoryList(false)
            it.onNext(historyList)
        }
    }
}