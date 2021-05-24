package com.kai.ui.bookdetail

import android.os.Handler
import android.os.Looper
import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.extension.isContainChinese
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

    override fun loadBookChapter(source: SL, id: String) {

        if(id.contains("http")){
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
        } else {
            //加载本地文件
            var name = ""
            try {
               name =  id.substring(id.lastIndexOf("/") + 1,id.lastIndexOf("."))
            }catch (e:Exception){

            }
            val arrayList = ArrayList<BookChapterBean>()
            arrayList.clear()
            val bookChapterBean = BookChapterBean()
            bookChapterBean.bookId = id
            bookChapterBean.link = id
            bookChapterBean.id = id.hashCode()
            bookChapterBean.unReadable = false
            bookChapterBean.title = name
            bookChapterBean.sourceName = name
            bookChapterBean.sourceID = source.source.id
            bookChapterBean.sourceName = source.source.name
            bookChapterBean.searchUrl = id
            bookChapterBean.mineKeyWord = source.source.minKeyWord
            bookChapterBean.position = 0
            arrayList.add(bookChapterBean)
            Handler(Looper.getMainLooper()).postDelayed({
                getView()?.onLoadBookChapter(arrayList)
            },200)

        }

    }

    override fun loadBookContentByChapter(bookChapterBean: BookChapterBean,isOpen: Boolean) {
        val beforeChapter = BookDatabase.get().bookDao().getBookChapterById(bookChapterBean.id)
        if(beforeChapter.content.isEmpty()){
            val source =
                Source(bookChapterBean.sourceID, bookChapterBean.sourceName, bookChapterBean.searchUrl,bookChapterBean.searchUrl)
            val sl = SL(bookChapterBean.link, source)

            if(bookChapterBean.link.contains("http")){
                Crawler.content(sl, bookChapterBean.link)
                    .doOnError {
                        LogUtils.e("BookDetailPresenter", "parse content error is $it")
                    }.subscribe {
                        try {
                            val bookChapterById = BookDatabase.get().bookDao().getBookChapterById(bookChapterBean.id)
                            bookChapterById.content = it
                            BookDatabase.get().bookDao().updateBookChapter(bookChapterById)
                            getView()?.onLoadBookContentByChapter(bookChapterById,isOpen)
                        } catch (e: Exception) {
                            LogUtils.e("BookDetailPresenter","save content error is $e")
                        }
                    }
            } else {
                val bookChapterById = BookDatabase.get().bookDao().getBookChapterById(bookChapterBean.id)
                getView()?.onLoadBookContentByChapter(bookChapterById,isOpen)
            }

        } else {
            getView()?.onLoadBookContentByChapter(beforeChapter,isOpen)
        }

    }
}