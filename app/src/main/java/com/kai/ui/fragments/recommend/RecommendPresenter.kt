package com.kai.ui.fragments.recommend

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.crawler.xpath.model.JXDocument
import com.kai.model.book.BookRepository
import java.lang.Exception

class RecommendPresenter : BasePresenter<RecommendContract.View>(), RecommendContract.Presenter {
    private val bookRepository: BookRepository = BookRepository.get()
    private var jxDocument: JXDocument? = null
    private val types = arrayListOf(
        BookRecommend.FANTASY_RECOMMEND,
        BookRecommend.HISTORY_RECOMMEND,
        BookRecommend.SCIENCE_RECOMMEND,
        BookRecommend.GAME_RECOMMEND,
        BookRecommend.COMPREHENSION_RECOMMEND,
        BookRecommend.CITY_RECOMMEND
    )

    fun getHomePage() {
        Crawler.getHomeJxDocument().subscribe {
            jxDocument = it
        }
    }

    override fun banner() {
        if (jxDocument == null) {
            bookRepository.getHomePage()
                .doOnError {
                    getView()?.onBanner(ArrayList<BookRecommend>())
                }
                .subscribe {
                    bookRepository.getBookIndexRecommend(it)
                        ?.subscribe { list ->
                            if (list.isNotEmpty()) {
                                val bookRecommendByType = BookDatabase.get().bookDao()
                                    .getBookRecommendByType(BookRecommend.INDEX_RECOMMEND)
                                for (value in bookRecommendByType) {
                                    BookDatabase.get().bookDao().deleteBookRecommend(value)
                                }
                            }

                            for (value in list) {
                                try {
                                    BookDatabase.get().bookDao().insertBookRecommend(value)
                                } catch (e: Exception) {
                                    LogUtils.e("RecommendPresenter", e.toString())
                                }
                            }
                            getView()?.onBanner(list as ArrayList<BookRecommend>)
                        }
                }
        } else {
            bookRepository.getBookIndexRecommend(jxDocument)
                ?.subscribe { list ->
                    if (list.isNotEmpty()) {
                        val bookRecommendByType = BookDatabase.get().bookDao()
                            .getBookRecommendByType(BookRecommend.INDEX_RECOMMEND)
                        for (value in bookRecommendByType) {
                            BookDatabase.get().bookDao().deleteBookRecommend(value)
                        }
                    }

                    for (value in list) {
                        try {
                            BookDatabase.get().bookDao().insertBookRecommend(value)
                        } catch (e: Exception) {
                            LogUtils.e("RecommendPresenter", e.toString())
                        }
                    }
                    getView()?.onBanner(list as ArrayList<BookRecommend>)
                }
        }

    }

    override fun recommend() {
        if (jxDocument == null) {
            bookRepository.getHomePage()
                .doOnError {
                    getView()?.onRecommend(ArrayList<ArrayList<BookRecommend>>())
                }
                .subscribe {
                    val recommends = ArrayList<ArrayList<BookRecommend>>()
                    val map = HashMap<Int, ArrayList<BookRecommend>>()
                    for (value in types) {
                        bookRepository.getBookRecommendByType(value, jxDocument)
                            .doOnComplete {
                                //成功访问数据
                                for (type in types) {
                                    map[type]?.let { mapItem ->
                                        if (!checkIsRepeat(mapItem, recommends)) {
                                            recommends.add(mapItem)
                                        }
                                    }
                                }
                                getView()?.onRecommend(recommends)
                            }
                            .doOnError {
                                //访问数据遇到错误
                                for (type in types) {
                                    map[type]?.let { mapItem ->
                                        if (!checkIsRepeat(mapItem, recommends)) {
                                            recommends.add(mapItem)
                                        }
                                    }
                                }
                                getView()?.onRecommend(recommends)
                            }
                            .subscribe {
                                //从数据库返回
                                if (it.isNotEmpty()) {
                                    map[it.first().bookType] = it as ArrayList<BookRecommend>
                                }
                            }
                    }
                }
        } else {
            val recommends = ArrayList<ArrayList<BookRecommend>>()
            val map = HashMap<Int, ArrayList<BookRecommend>>()
            for (value in types) {
                bookRepository.getBookRecommendByType(value, jxDocument)
                    .doOnComplete {
                        //成功访问数据
                        recommends.clear()
                        for (type in types) {
                            map[type]?.let { mapItem ->
                                if (!checkIsRepeat(mapItem, recommends)) {
                                    recommends.add(mapItem)
                                }
                            }
                        }
                        getView()?.onRecommend(recommends)
                    }
                    .doOnError {
                        //访问数据遇到错误
                        recommends.clear()
                        for (type in types) {
                            map[type]?.let { mapItem ->
                                if (!checkIsRepeat(mapItem, recommends)) {
                                    recommends.add(mapItem)
                                }

                            }
                        }
                        getView()?.onRecommend(recommends)
                    }
                    .subscribe {
                        //从数据库返回或爬虫数据返回
                        if (it.isNotEmpty()) {
                            map[it.first().bookType] = it as ArrayList<BookRecommend>
                        }
                    }
            }

        }
    }


    fun bookDetail(bookRecommend: BookRecommend,onBookDetail: OnBookDetail){
        if(bookRecommend.bookUrl.isEmpty()){
            return
        }
        bookRepository.getBookDetail(bookRecommend.bookUrl).subscribe {
            try {
                val bookRecommendByBookUrl =
                    BookDatabase.get().bookDao().getBookRecommendByBookUrl(it.bookUrl)
                if(bookRecommendByBookUrl == null || (bookRecommendByBookUrl!= null && bookRecommendByBookUrl.checkIsEmpty())){
                    BookDatabase.get().bookDao().insertBookRecommend(it)
                }
            }catch (e:Exception){
                LogUtils.e("BookRecommendPresenter","get book detail save in sql error is $e")
            }
            onBookDetail.onBookDetail(it)
        }
    }


    fun localBookDetail(bookUrl:String):BookRecommend{
        return  BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
    }


    public interface OnBookDetail{
        fun onBookDetail(bookRecommend: BookRecommend)
    }


    private fun checkIsRepeat(
        target: ArrayList<BookRecommend>,
        list: ArrayList<ArrayList<BookRecommend>>
    ): Boolean {
        var isRepeat = false
        for (value in list) {
            if (value.isNotEmpty() && target.isNotEmpty()) {
                if (value.first().bookType == target.first().bookType) {
                    isRepeat = true
                    break
                }
            }
        }
        return isRepeat
    }
}