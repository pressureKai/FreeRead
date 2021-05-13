package com.kai.ui.search

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.utils.LogUtils
import com.kai.crawler.entity.book.SearchBook
import com.kai.database.CustomDatabase
import com.kai.entity.SearchHistory
import com.kai.entity.User
import com.kai.model.search.SearchRepository
import com.kai.model.user.UserRepository

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    注册- Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class SearchPresenter : BasePresenter<SearchContract.View>(), SearchContract.Presenter {
    private var searchRepository: SearchRepository = SearchRepository.get()
    override fun search(keyword: String) {
        searchRepository.search(keyword)?.doOnError {
            getView()?.onSearch(ArrayList<SearchBook>())
        }?.subscribe {
            getView()?.onSearch(it as ArrayList<SearchBook>)
        }
    }

    override fun recommend() {
        searchRepository.recommend().subscribe {
            getView()?.onRecommend(it as ArrayList<SearchHistory>)
        }
    }

    override fun history() {
        searchRepository.history().subscribe {
            getView()?.onHistory(it as ArrayList<SearchHistory>)
        }
    }

    override fun deleteAll() {
        val historyList = CustomDatabase.get().searchHistoryDao().getHistoryList(false)
        for(value in historyList){
            CustomDatabase.get().searchHistoryDao().deleteHistory(value)
        }
        getView()?.onDelete()
    }

    override fun deleteByName(name:String) {
        val historyByName = CustomDatabase.get().searchHistoryDao().getHistoryByName(name, false)
        for(value in historyByName){
            CustomDatabase.get().searchHistoryDao().deleteHistory(value)
        }
        getView()?.onDelete()
    }


}