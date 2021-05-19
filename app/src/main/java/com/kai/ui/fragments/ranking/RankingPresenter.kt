package com.kai.ui.fragments.ranking

import com.kai.base.mvp.base.BasePresenter
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.model.book.BookRepository

class RankingPresenter : BasePresenter<RankingContract.View>(), RankingContract.Presenter {
    private val bookRepository: BookRepository = BookRepository.get()
    override fun ranking() {
        bookRepository.getRanking().subscribe {
            getView()?.onRanking(it)
        }
    }


    fun getCover(type: Int, url: String, getCoverListener: GetCoverListener) {
        bookRepository.getRankingFirst(type, url).subscribe {
            if (it.bookUrl.isNotEmpty()) {
                Crawler.getBookDetail(it.bookUrl).subscribe { recommend ->
                    if (recommend.bookCoverUrl.isNotEmpty()) {
                        recommend.isRanking = true
                        recommend.save()
                        getCoverListener.onCover(recommend.bookCoverUrl)
                    }
                }
            }
        }
    }

    public interface GetCoverListener {
        fun onCover(path: String)
    }

}