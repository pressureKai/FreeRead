package com.kai.model.search

import com.kai.crawler.Crawler
import com.kai.crawler.entity.book.SearchBook
import com.kai.database.CustomDatabase
import com.kai.entity.SearchHistory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class RemoteSearchDataSource : SearchDataSource {
    override fun search(keyword: String): Observable<List<SearchBook>> {
        val searchHistory = SearchHistory()
        searchHistory.searchName = keyword
        searchHistory.searchTime = System.currentTimeMillis().toString()
        searchHistory.isRecommend = false
        val historyByName = CustomDatabase.get().searchHistoryDao().getHistoryByName(keyword, false)
        if (historyByName.isEmpty()) {
            CustomDatabase.get().searchHistoryDao().insertHistory(searchHistory)
        }

        return Crawler.search(keyword)
    }

    override fun recommend(): Observable<List<SearchHistory>> {
        return Observable.create<List<SearchHistory>> {
            val recommends: ArrayList<SearchHistory> = ArrayList()
            Crawler.getRecommend().subscribe { list ->
                val recommend = CustomDatabase.get().searchHistoryDao().getHistoryList(true)

                for(value in recommend){
                    CustomDatabase.get().searchHistoryDao().deleteHistory(value)
                }

                for (value in list) {
                    val searchHistory = SearchHistory()
                    searchHistory.searchName = value
                    searchHistory.searchTime = System.currentTimeMillis().toString()
                    searchHistory.isRecommend = true
                    recommends.add(searchHistory)
                    val historyByName = CustomDatabase.get().searchHistoryDao().getHistoryByName(value, true)
                    if (historyByName.isEmpty()) {
                        CustomDatabase.get().searchHistoryDao().insertHistory(searchHistory)
                    }
                }
                it.onNext(recommends)
            }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())


    }

    override fun history(): Observable<List<SearchHistory>> {
        return Observable.create<List<SearchHistory>> {
            val historyList = CustomDatabase.get().searchHistoryDao().getHistoryList(false)
            it.onNext(historyList)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }
}