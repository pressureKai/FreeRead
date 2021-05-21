package com.kai.model.book

import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.crawler.xpath.model.JXDocument
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.NullPointerException

/**
 *
 * @ProjectName:    app-bookpage
 * @Description:    统筹调用本地数据与网络数据(Presenter层通过此对象获取数据) 单例模式节省资源
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/26 13:39
 */
class BookRepository private constructor(
    private val localBookDataSource: LocalBookDataSource,
    private val remoteBookDataSource: RemoteBookDataSource
) : BookDataSource {
    companion object {
        private var instance: BookRepository? = null
            get() {
                if (field == null) {
                    field = BookRepository(LocalBookDataSource(), RemoteBookDataSource())
                }
                return field
            }

        @Synchronized
        fun get(): BookRepository {
            return instance!!
        }
    }

    override fun getBookIndexRecommend(jxDocument: JXDocument?): Observable<List<BookRecommend>>? {
        return Observable.create<List<BookRecommend>> { emitter ->
            localBookDataSource.getBookIndexRecommend(jxDocument).subscribe {
                emitter.onNext(it)
            }
            if (jxDocument == null) {
                Crawler.getHomeJxDocument()
                    .doOnError {
                        emitter.onError(it)
                    }.subscribe {
                        remoteBookDataSource.getBookIndexRecommend(it)?.doOnError { error ->
                            emitter.onError(error)
                        }?.subscribe { list ->
                            emitter.onNext(list)
                        } ?: kotlin.run {
                            emitter.onError(NullPointerException())
                        }
                    }
            } else {
                remoteBookDataSource.getBookIndexRecommend(jxDocument)?.doOnError { error ->
                    emitter.onError(error)
                }?.subscribe { list ->
                    emitter.onNext(list)
                    emitter.onComplete()
                } ?: kotlin.run {
                    emitter.onError(NullPointerException())
                }
            }

        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

    }

    override fun getBookRecommendByType(
        type: Int,
        jxDocument: JXDocument?
    ): Observable<List<BookRecommend>> {
        return Observable.create<List<BookRecommend>> { emitter ->
            localBookDataSource.getBookRecommendByType(type, jxDocument).subscribe {
                emitter.onNext(it)
            }
            if (jxDocument == null) {
                Crawler.getHomeJxDocument()
                    .doOnError {
                        emitter.onError(it)
                    }.subscribe {
                        remoteBookDataSource.getBookRecommendByType(type, it).doOnError { error ->
                            emitter.onError(error)
                        }?.subscribe { list ->
                            emitter.onNext(list)
                            emitter.onComplete()
                        }
                    }
            } else {
                remoteBookDataSource.getBookRecommendByType(type, jxDocument)?.doOnError { error ->
                    emitter.onError(error)
                }?.subscribe { list ->
                    emitter.onNext(list)
                    emitter.onComplete()
                }
            }

        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getBookDetail(bookUrl: String, update: Boolean): Observable<BookRecommend> {
        return Observable.create<BookRecommend> { emitter ->
            localBookDataSource.getBookDetail(bookUrl,false)
                .doOnError {
                remoteBookDataSource
                    .getBookDetail(bookUrl,update)
                    .subscribe { recommend ->
                        emitter.onNext(recommend)
                    }
            }.subscribe {
                if(!update){
                    if (it.checkIsEmpty()) {
                        remoteBookDataSource
                            .getBookDetail(bookUrl,update)
                            .subscribe { recommend ->
                                emitter.onNext(recommend)
                            }
                    } else {
                        emitter.onNext(it)
                    }
                } else {
                    remoteBookDataSource
                        .getBookDetail(bookUrl,update)
                        .subscribe { recommend ->
                            emitter.onNext(recommend)
                            val bookRecommendByBookUrl = BookDatabase.get().bookDao()
                                .getBookRecommendByBookUrl(recommend.bookUrl)
                            bookRecommendByBookUrl?.let { bookRecommend ->
                                bookRecommend.bookDescriptor = recommend.bookDescriptor
                                bookRecommend.updateTime = recommend.updateTime
                                bookRecommend.newChapterName = recommend.newChapterName
                                bookRecommend.newChapterUrl = recommend.newChapterUrl
                                BookDatabase.get().bookDao().updateBookRecommend(bookRecommend)
                            }
                            if(bookRecommendByBookUrl == null){
                                recommend.save()
                            }
                        }
                }

            }

        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRanking(): Observable<HashMap<Int, String>> {
        return Observable.create<HashMap<Int, String>> { emitter ->

            localBookDataSource.getRanking().subscribe {
                emitter.onNext(it)
            }

            remoteBookDataSource.getRanking().subscribe {
                emitter.onNext(it)
            }


        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRankingFirst(type: Int, url: String): Observable<BookRecommend> {
        return Observable.create<BookRecommend> { emitter ->
            localBookDataSource.getRankingFirst(type, url).subscribe {
                emitter.onNext(it)
            }

            remoteBookDataSource.getRankingFirst(type, url).subscribe {
                emitter.onNext(it)
            }

        }.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread())

    }

    override fun getRankingList(type: Int, url: String): Observable<List<BookRecommend>> {
        return Observable.create<List<BookRecommend>> { emitter ->
            val arrayList = ArrayList<BookRecommend>()
            localBookDataSource.getRankingList(type, url).subscribe {
                arrayList.addAll(it)
            }

            remoteBookDataSource.getRankingList(type, url).doOnError {
                if(arrayList.size > 0){
                    emitter.onNext(arrayList)
                } else {
                    emitter.onError(it)
                }
            }.subscribe {
                emitter.onNext(it)
            }

        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
    }


    fun getHomePage(): Observable<JXDocument> {
        return Crawler.getHomeJxDocument()
    }

}