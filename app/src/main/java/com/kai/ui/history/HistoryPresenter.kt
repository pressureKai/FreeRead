package com.kai.ui.history

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.database.CustomDatabase

class HistoryPresenter:  BasePresenter<HistoryContract.View>(), HistoryContract.Presenter  {
    override fun getHistoryByType(type: Int) {
        if(type == HistoryActivity.LIKE){
            val arrayList = ArrayList<BookRecommend>()
            val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
            if(!userByOnLine.isNullOrEmpty()){
                for(value in userByOnLine){
                    val shelfs =
                        CustomDatabase.get().likeDao().getUserLikeList(value.id)
                    for(shelf in shelfs){
                        val bookRecommendByBookUrl =
                            BookDatabase.get().bookDao().getBookRecommendByBookUrl(shelf.bookUrl)
                        if(bookRecommendByBookUrl != null){
                            arrayList.add(bookRecommendByBookUrl)
                        }
                    }
                }
                arrayList.reverse()
                getView()?.onHistory(arrayList)
            }
        } else {
            val bookRecommendByBookLike =
                BookDatabase.get().bookDao().getBookRecommendByBookRead(true)
            val arrayList = bookRecommendByBookLike as ArrayList<BookRecommend>
            arrayList.reverse()
            getView()?.onHistory(arrayList)
        }
    }

}