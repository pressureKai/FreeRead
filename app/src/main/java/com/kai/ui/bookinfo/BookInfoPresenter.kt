package com.kai.ui.bookinfo

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.model.book.BookRepository
import com.kai.ui.fragments.recommend.RecommendPresenter
import com.kai.ui.rankingdetail.RankingDetailContract
import java.lang.Exception

class BookInfoPresenter: BasePresenter<BookInfoContract.View>(), BookInfoContract.Presenter  {
    private val bookRepository: BookRepository = BookRepository.get()
    override fun getBookDetail(url: String) {
        bookRepository.getBookDetail(url,true).subscribe {
            getView()?.onBookDetail(it)
        }
    }

    override fun recommendList(url: String) {
        Crawler.getBookDetailRecommendList(url).subscribe {
            getView()?.onRecommendList(it as ArrayList<BookRecommend>)
        }
    }

    fun localBookDetail(bookUrl:String):BookRecommend{
        return  BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
    }

    fun bookDetail(bookRecommend: BookRecommend,onBookDetail: OnBookDetail){
        if(bookRecommend.bookUrl.isEmpty()){
            return
        }
        bookRepository.getBookDetail(bookRecommend.bookUrl,false).subscribe {
            try {
                val bookRecommendByBookUrl =
                    BookDatabase.get().bookDao().getBookRecommendByBookUrl(it.bookUrl)
                if(bookRecommendByBookUrl == null || (bookRecommendByBookUrl!= null && bookRecommendByBookUrl.checkIsEmpty())){
                    BookDatabase.get().bookDao().insertBookRecommend(it)
                }
            }catch (e: Exception){
                LogUtils.e("BookRecommendPresenter","get book detail save in sql error is $e")
            }
            onBookDetail.onBookDetail(it)
        }
    }

    public interface OnBookDetail{
        fun onBookDetail(bookRecommend: BookRecommend)
    }
}