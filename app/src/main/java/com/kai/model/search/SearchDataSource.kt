package com.kai.model.search

import com.kai.crawler.entity.book.SearchBook
import com.kai.entity.SearchHistory
import io.reactivex.rxjava3.core.Observable

interface SearchDataSource {
    fun search(keyword: String): Observable<List<SearchBook>>?

    fun recommend(): Observable<List<SearchHistory>>

    fun history(): Observable<List<SearchHistory>>
}