package com.kai.model.book

import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.crawler.xpath.model.JXDocument
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * @ProjectName:    app
 * @Description:    加载本地缓存数据  subscribeOn 数据生成线程,observeOn 数据订阅线程
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/26 11:51
 */

class LocalBookDataSource : BookDataSource {
    override fun getBookIndexRecommend(jxDocument: JXDocument?): Observable<List<BookRecommend>> {
        return Observable.create<List<BookRecommend>> {
            val bookRecommendByType = BookDatabase.get().bookDao()
                .getBookRecommendByType(BookRecommend.INDEX_RECOMMEND)
            it.onNext(bookRecommendByType)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

    }

    override fun getBookRecommendByType(
        type: Int,
        jxDocument: JXDocument?
    ): Observable<List<BookRecommend>> {
        return Observable.create<List<BookRecommend>> {
            val bookRecommendByType = BookDatabase.get().bookDao()
                .getBookRecommendByType(type)
            it.onNext(bookRecommendByType)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getBookDetail(bookUrl: String, update: Boolean): Observable<BookRecommend> {
        return Observable.create<BookRecommend> {
            val bookRecommendByType = BookDatabase.get().bookDao()
                .getBookRecommendByBookUrl(bookUrl)
            it.onNext(bookRecommendByType)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRanking(): Observable<HashMap<Int, String>> {
        return Observable.create<HashMap<Int, String>> {
            val types = BookRecommend.types
            val hashMap = HashMap<Int,String>()
            for(value in types){
                try {
                    val bookRecommend = BookDatabase.get().bookDao()
                        .getRankingBookRecommendByType(value, true)
                    if(bookRecommend.isNotEmpty()){
                        hashMap[value] = bookRecommend.first().bookUrl
                    }

                }catch (e:Exception){

                }

            }

            it.onNext(hashMap)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRankingFirst(type: Int, url: String): Observable<BookRecommend> {
        return Observable.create<BookRecommend> {
            val bookRecommendByType = BookDatabase.get().bookDao()
                .getRankingBookRecommendByType(type,true)
            if(bookRecommendByType.isNotEmpty()){
                it.onNext(bookRecommendByType.first())
            }

        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRankingList(type: Int, url: String): Observable<List<BookRecommend>> {
        return Observable.create<List<BookRecommend>> {
            val bookRecommendByType = BookDatabase.get().bookDao()
                .getRankingBookRecommendByType(type,true)
            it.onNext(bookRecommendByType)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }
}