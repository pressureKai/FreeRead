package com.kai.model.book

import com.kai.bookpage.model.BookRecommend
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.crawler.xpath.model.JXDocument
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *
 * @ProjectName:    app
 * @Description:    加载网络数据
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/26 11:50
 */
class RemoteBookDataSource : BookDataSource {
    override fun getBookIndexRecommend(jxDocument: JXDocument?): Observable<List<BookRecommend>>? {
        return Observable.create<List<BookRecommend>> { emitter ->
            Crawler.getIndexRecommend(jxDocument!!)
                .doOnError {
                    emitter.onError(NullPointerException())
                }.subscribe { list ->
                    emitter.onNext(list)
                }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getBookRecommendByType(
        type: Int,
        jxDocument: JXDocument?
    ): Observable<List<BookRecommend>> {
        return Observable.create<List<BookRecommend>> { emitter ->
            Crawler.getRecommendByType(type, jxDocument!!)
                .doOnError {
                    emitter.onError(NullPointerException())
                }.subscribe { list ->
                    val arrayList = ArrayList<BookRecommend>()
                    for (value in list) {
                        val bookRecommend = BookRecommend()
                        bookRecommend.bookUrl = value
                        bookRecommend.bookType = type
                        arrayList.add(bookRecommend)
                    }
                    emitter.onNext(arrayList)
                }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getBookDetail(bookUrl: String): Observable<BookRecommend> {
        return Observable.create<BookRecommend> { emitter ->
            Crawler.getBookDetail(bookUrl)
                .doOnError {
                    emitter.onError(NullPointerException())
                }.subscribe { it ->
                    emitter.onNext(it)
                }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRanking(): Observable<HashMap<Int,String>> {
        return Observable.create<HashMap<Int,String>> { emitter ->
            Crawler.getHomeJxDocument()
                .doOnError {
                    emitter.onError(NullPointerException())
                }
                .subscribe {
                    Crawler.getTypeUrls(it)
                        .doOnError {
                            emitter.onError(NullPointerException())
                        }.subscribe { it ->
                            emitter.onNext(it)
                        }
                }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRankingFirst(type: Int, url: String): Observable<BookRecommend> {
        return Observable.create<BookRecommend> { emitter ->
            Crawler.getConcurrentTypeRankingList(type,url)
                .doOnError {
                    emitter.onError(NullPointerException())
                }.subscribe { it ->
                    if(it.isNotEmpty()){
                        emitter.onNext(it.first())
                    }
                }
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }
}