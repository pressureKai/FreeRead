package com.kai.ui.bookdetail

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.crawler.entity.book.SearchBook.SL
import com.kai.crawler.entity.source.Source
import com.kai.model.book.BookDataSource
import com.kai.model.book.BookRepository
import java.lang.Exception

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    MainActivity - Presenter
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:09
 */
class BookDetailPresenter : BasePresenter<BookDetailContract.View>(), BookDetailContract.Presenter {
    var mBookRepository: BookRepository = BookRepository.get()
    override fun loadBookDetail() {


    }

    override fun loadBookChapter(source: SL, id: Int) {
        Crawler.catalog(source).subscribe { chapters ->
            val arrayList = ArrayList<BookChapterBean>()
            arrayList.clear()
            for ((index, value) in chapters.withIndex()) {
                val bookChapterBean = BookChapterBean()
                bookChapterBean.bookId = id
                bookChapterBean.link = value.link
                bookChapterBean.id = value.link.hashCode()
                bookChapterBean.unReadable = false
                bookChapterBean.title = value.title
                bookChapterBean.sourceName = value.title
                bookChapterBean.sourceID = source.source.id
                bookChapterBean.sourceName = source.source.name
                bookChapterBean.searchUrl = source.source.searchUrl
                bookChapterBean.mineKeyWord = source.source.minKeyWord
                bookChapterBean.position = index
                arrayList.add(bookChapterBean)
            }
            getView()?.onLoadBookChapter(arrayList)
        }
    }

    override fun loadBookContentByChapter(bookChapterBean: BookChapterBean) {
        val source =
                Source(bookChapterBean.sourceID, bookChapterBean.sourceName, bookChapterBean.searchUrl)
        val sl = SL(bookChapterBean.link, source)

        Crawler.content(sl, bookChapterBean.link)
                .doOnError {
                    LogUtils.e("BookDetailPresenter", "parse content error is $it")
                }.subscribe {
                    try {
                        val bookChapterById = BookDatabase.get().bookDao().getBookChapterById(bookChapterBean.id)
                        bookChapterById.content = it
                        BookDatabase.get().bookDao().updateBookChapter(bookChapterById)
                        getView()?.onLoadBookContentByChapter(bookChapterById)
                        LogUtils.e("BookDetailPresenter", "parse content $it")
                    } catch (e: Exception) {
                        LogUtils.e("BookDetailPresenter","save content error is $e")
                    }
                }
    }
}