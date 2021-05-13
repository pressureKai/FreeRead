package com.kai.ui.pageLoader

import android.text.TextPaint
import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.database.BookDatabase
import com.kai.bookpage.page.PageLoader
import com.kai.bookpage.page.PageView
import com.kai.common.utils.LogUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import java.io.*
import kotlin.collections.ArrayList

class CrawlerPageLoader(pageView: PageView, coolBookBean: CoolBookBean):PageLoader(
    pageView,
    coolBookBean
) {

    override fun hasChapterData(chapter: BookChapterBean): Boolean {
        val bookChapterById = BookDatabase.get().bookDao().getBookChapterById(chapter.id)
        var contentIsEmpty = true
        try {
            contentIsEmpty =  bookChapterById.content.isNotEmpty()
        }catch (e:java.lang.Exception){
            LogUtils.e("CrawlerPageLoader","getChapter error is $e")
        }
        return  contentIsEmpty
    }

    override fun getChapterReader(chapter: BookChapterBean): BufferedReader {
        val bookChapterById = BookDatabase.get().bookDao().getBookChapterById(chapter.id)
        var contentIsEmpty = true
        try {
          contentIsEmpty =  bookChapterById.content.isNotEmpty()
        }catch (e:java.lang.Exception){
            LogUtils.e("CrawlerPageLoader","getChapter error is $e")
        }
        return if(contentIsEmpty){
            val content: ByteArray = bookChapterById.content.toByteArray()
            val input = ByteArrayInputStream(content)
            val br = BufferedReader(InputStreamReader(input))
            br
        } else {
            LogUtils.e("CrawlerPageLoader","getChapterReader content is empty")
            BufferedReader(null)
        }


    }

    override fun refreshChapterList() {
        try {
            getCoolBook()?.let {
                try {
                    BookDatabase.get().bookDao().insertCoolBook(it)
                }catch (e: Exception){
                    BookDatabase.get().bookDao().updateCoolBook(it)
                }

                val chapterList = BookDatabase.get().bookDao().getChapterList(it.bookId)
                if(chapterList.bookChapterList.size != getChapterCategory().size
                    && chapterList.bookChapterList.isNotEmpty()
                ){
                    Observable.fromIterable(chapterList.bookChapterList).toSortedList {
                        o1, o2 -> o1!!.position - o2!!.position }.subscribe(Consumer<List<BookChapterBean>> { orderList ->
                        setChapterCategory(orderList as ArrayList<BookChapterBean>)
                        isChapterListPrepare = true
                    })

                }

            }
        }catch (e: Exception){
            LogUtils.e("CrawlerPageLoader", e.toString())
        }
    }



    public interface OnTextPaintInitListener{
        fun onTextPaintInitListener(): TextPaint
    }
}