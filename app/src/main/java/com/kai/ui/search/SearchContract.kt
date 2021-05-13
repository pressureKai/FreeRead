package com.kai.ui.search

import com.kai.base.mvp.base.IView
import com.kai.crawler.entity.book.SearchBook
import com.kai.entity.SearchHistory
import com.kai.entity.User
import io.reactivex.rxjava3.core.Observable

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-注册
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class SearchContract {
    interface View :IView{
        fun onSearch(searchBooks: ArrayList<SearchBook>)
        fun onRecommend(recommends: ArrayList<SearchHistory>)
        fun onHistory(historys: ArrayList<SearchHistory>)
        fun onDelete()
    }

    interface Presenter{
        fun search(keyword: String)
        fun recommend()
        fun history()
        fun deleteAll()
        fun deleteByName(name: String)
    }
}