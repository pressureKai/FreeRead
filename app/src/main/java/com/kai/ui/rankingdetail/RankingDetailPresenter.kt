package com.kai.ui.rankingdetail

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.model.book.BookRepository

class RankingDetailPresenter : BasePresenter<RankingDetailContract.View>(),
    RankingDetailContract.Presenter {
    private val bookRepository: BookRepository = BookRepository.get()
    override fun ranking(type: Int, url: String) {
        bookRepository.getRankingList(type, url).doOnError {

        }.subscribe {
            getView()?.onRanking(it as ArrayList<BookRecommend>)
        }
    }


    fun getBookDetail(
        type: Int,
        url: String,
        rankingPosition: Int,
        getBookDetailListener: GetBookDetailListener
    ) {
        bookRepository.getBookDetail(url, true).subscribe {
            it.bookType = type
            it.isRanking = true
            it.rankingPosition = rankingPosition
            it.save()
            getBookDetailListener.onBookDetailListener(it)
        }
    }


    public interface GetBookDetailListener {
        fun onBookDetailListener(bookRecommend: BookRecommend)
    }
}