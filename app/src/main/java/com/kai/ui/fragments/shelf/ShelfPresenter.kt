package com.kai.ui.fragments.shelf

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.database.CustomDatabase

class ShelfPresenter: BasePresenter<ShelfContract.View>(), ShelfContract.Presenter {
    override fun shelf() {
        val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
        if(!userByOnLine.isNullOrEmpty()){
            val arrayList = ArrayList<BookRecommend>()
            for(value in userByOnLine){
                val shelfs =
                    CustomDatabase.get().shelfDao().getUserShelfList(value.id)
                for(shelf in shelfs){
                    val bookRecommendByBookUrl =
                        BookDatabase.get().bookDao().getBookRecommendByBookUrl(shelf.bookUrl)
                    if(bookRecommendByBookUrl != null){
                        arrayList.add(bookRecommendByBookUrl)
                    }
                }
            }
            val bookRecommendByLocal =
                BookDatabase.get().bookDao().getBookRecommendByLocal(true)
            arrayList.addAll(bookRecommendByLocal)
            getView()?.onShelf(arrayList)

        } else {
            getView()?.onShelf(ArrayList<BookRecommend>())
        }

    }


    fun removeBookShelf(url:String,removeListener: RemoveListener){
        val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
        if(!url.contains("http")){
            val bookRecommendByBookUrl = BookDatabase.get().bookDao().getBookRecommendByBookUrl(url)
            bookRecommendByBookUrl.isLocal = false
            BookDatabase.get().bookDao().updateBookRecommend(bookRecommendByBookUrl)
        }
        if(!userByOnLine.isNullOrEmpty()){
            for(value in userByOnLine){
                val shelfs =
                    CustomDatabase.get().shelfDao().getUserShelfById(value.id,url)
                for(shelf in shelfs){
                    CustomDatabase.get().shelfDao().deleteUserShelf(shelf)
                }
            }
            removeListener.removeListener()
        }
    }

    public interface RemoveListener{
        fun removeListener()
    }
}