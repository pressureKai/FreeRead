package com.kai.bookpage.model

import android.util.SparseBooleanArray
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils
import com.kai.crawler.entity.book.SearchBook
import com.kai.crawler.entity.source.SourceManager
import org.jsoup.select.Evaluator
import java.lang.Exception

@Entity(tableName = "bookRecommend")
class BookRecommend {
    companion object {
        const val INDEX_RECOMMEND = 0

        //玄幻
        const val FANTASY_RECOMMEND = 1

        //修真
        const val COMPREHENSION_RECOMMEND = 2

        //都市
        const val CITY_RECOMMEND = 3

        //历史
        const val HISTORY_RECOMMEND = 4

        //网游
        const val GAME_RECOMMEND = 5

        //科幻
        const val SCIENCE_RECOMMEND = 6

        //言情
        const val ROMANS_RECOMMEND = 7

        //全本
        const val ALLBOOK_RECOMMEND = 8

        //其他
        const val ORTHER_RECOMMEND = 9

        val types = arrayListOf(
            FANTASY_RECOMMEND,
            COMPREHENSION_RECOMMEND,
            CITY_RECOMMEND,
            HISTORY_RECOMMEND,
            GAME_RECOMMEND,
            SCIENCE_RECOMMEND,
            ROMANS_RECOMMEND,
            ALLBOOK_RECOMMEND,
            ORTHER_RECOMMEND
        )


        fun typeToName(type: Int): String {
            var typeName = "首页"
            when (type) {
                FANTASY_RECOMMEND -> {
                    typeName = "玄幻"
                }
                COMPREHENSION_RECOMMEND -> {
                    typeName = "修真"
                }
                CITY_RECOMMEND -> {
                    typeName = "都市"
                }
                HISTORY_RECOMMEND -> {
                    typeName = "历史"
                }
                GAME_RECOMMEND -> {
                    typeName = "网游"
                }
                SCIENCE_RECOMMEND -> {
                    typeName = "科幻"
                }
                ROMANS_RECOMMEND -> {
                    typeName = "言情"
                }
                ALLBOOK_RECOMMEND -> {
                    typeName = "全本"
                }
                ORTHER_RECOMMEND -> {
                    typeName = "其他"
                }
            }
            return typeName
        }
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recommendId", typeAffinity = ColumnInfo.INTEGER)
    var recommendId = 0
    var bookName = ""
    var bookUrl = ""
    var bookCoverUrl = ""
    var bookDescriptor = ""
    var bookType = 0
    var authorName = ""
    var newChapterUrl = ""
    var newChapterName = ""
    var updateTime = ""
    var isShelf = false
    var isRead = false
    var isLike = false
    var isRanking = false
    var userId = 0L
    var rankingPosition = 0


    fun checkIsEmpty(): Boolean {
        return bookName.isEmpty()
                || bookCoverUrl.isEmpty()
                || bookDescriptor.isEmpty()
                || authorName.isEmpty()
                || newChapterUrl.isEmpty()
                || newChapterName.isEmpty()
    }


    fun save() {
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        if (bookRecommendByBookUrl == null || (bookRecommendByBookUrl != null
                    && !bookRecommendByBookUrl.checkIsEmpty()
                    && checkIsEmpty())
        ) {
            BookDatabase.get().bookDao().insertBookRecommend(this)
        }
    }


    fun toSearchBook(): SearchBook {
        LogUtils.e("BookRecommend", "start change")
        val searchBook = SearchBook()
        try {
            searchBook.author = authorName
            searchBook.cover = bookCoverUrl
            searchBook.title = bookName
            searchBook.descriptor = bookDescriptor
            val checkedMap: SparseBooleanArray = SourceManager.getSourceEnableSparseArray()
            for (i in 0.until(checkedMap.size())) {
                val id = SourceManager.CONFIGS.keyAt(i)
                val source = SourceManager.SOURCES.get(id)
                searchBook.sources = ArrayList()
                searchBook.sources.add(SearchBook.SL(bookUrl, source))
            }
        } catch (e: Exception) {
            LogUtils.e("BookRecommend", "change error is $e")
        }


        return searchBook
    }


    fun updateReadState() {
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        if (bookRecommendByBookUrl != null) {
            if (!bookRecommendByBookUrl.isRead) {
                bookRecommendByBookUrl.isRead = true
                BookDatabase.get().bookDao().updateBookRecommend(bookRecommendByBookUrl)
            }
        }
    }



    fun updateLikeState():Boolean{
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        return if (bookRecommendByBookUrl != null) {
            bookRecommendByBookUrl.isLike = !bookRecommendByBookUrl.isLike
            BookDatabase.get().bookDao().updateBookRecommend(bookRecommendByBookUrl)
            true
        } else {
            false
        }
    }

    fun getCurrentLikeState():Boolean{
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        return if (bookRecommendByBookUrl != null) {
            bookRecommendByBookUrl.isLike
        } else {
            false
        }
    }


    fun updateShelfState():Boolean{
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        return if (bookRecommendByBookUrl != null) {
            bookRecommendByBookUrl.isShelf = !bookRecommendByBookUrl.isShelf
            BookDatabase.get().bookDao().updateBookRecommend(bookRecommendByBookUrl)
            true
        } else {
            false
        }
    }

    fun getCurrentShelfState():Boolean{
        val bookRecommendByBookUrl =
            BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
        return if (bookRecommendByBookUrl != null) {
            bookRecommendByBookUrl.isShelf
        } else {
            false
        }
    }

}