package com.kai.ui.bookinfo

import com.kai.base.mvp.base.BasePresenter
import com.kai.bookpage.model.BookRecommend
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils
import com.kai.crawler.Crawler
import com.kai.database.CustomDatabase
import com.kai.entity.UserLike
import com.kai.entity.UserShelf
import com.kai.model.book.BookRepository
import java.lang.Exception

class BookInfoPresenter : BasePresenter<BookInfoContract.View>(), BookInfoContract.Presenter {
    private val bookRepository: BookRepository = BookRepository.get()
    override fun getBookDetail(url: String) {
        bookRepository.getBookDetail(url, true).subscribe {
            it.save()
            getView()?.onBookDetail(it)
        }
    }

    override fun recommendList(url: String) {
        Crawler.getBookDetailRecommendList(url).subscribe {
            getView()?.onRecommendList(it as ArrayList<BookRecommend>)
        }
    }

    override fun likeOption(bookUrl: String, like: Boolean) {
        val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
        if (!userByOnLine.isNullOrEmpty()) {
            for (value in userByOnLine) {
                try {
                    if (like) {
                        val userLike = UserLike()
                        userLike.bookUrl = bookUrl
                        userLike.userId = value.id
                        CustomDatabase.get().likeDao().insertUserLike(userLike)

                    } else {
                        val userLikeById =
                            CustomDatabase.get().likeDao().getUserLikeById(value.id, bookUrl)
                        for (userlike in userLikeById) {
                            CustomDatabase.get().likeDao().deleteUserLike(userlike)
                        }
                    }
                } catch (e: Exception) {

                }
            }

            val code = if(like){
                1
            } else {
                2
            }
            getView()?.onLikeOption(code)
        } else {
            getView()?.onLikeOption(0)
        }

    }


    override fun shelfOption(bookUrl: String, shelf: Boolean) {
        val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
        if (!userByOnLine.isNullOrEmpty()) {
            for (value in userByOnLine) {
                try {
                    if (shelf) {
                        val userShelf = UserShelf()
                        userShelf.bookUrl = bookUrl
                        userShelf.userId = value.id
                        CustomDatabase.get().shelfDao().insertUserShelf(userShelf)

                    } else {
                        val userByShelf =
                            CustomDatabase.get().shelfDao().getUserShelfById(value.id, bookUrl)
                        for (userShelf in userByShelf) {
                            CustomDatabase.get().shelfDao().deleteUserShelf(userShelf)
                        }
                    }
                } catch (e: Exception) {

                }
            }

            val code = if(shelf){
                1
            } else {
                2
            }
            getView()?.onShelfOption(code)
        } else {
            getView()?.onShelfOption(0)
        }

    }

    override fun getBookLike(bookUrl: String) {
        val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
        if (!userByOnLine.isNullOrEmpty()) {
            for(value in userByOnLine){
                try {
                    val userByShelf =
                        CustomDatabase.get().likeDao().getUserLikeById(value.id, bookUrl)
                    if(!userByShelf.isNullOrEmpty()){
                        getView()?.onGetBookLike(true)
                    } else {
                        getView()?.onGetBookLike(false)
                    }
                }catch (e:Exception){
                    getView()?.onGetBookLike(false)
                }
            }

        } else {
            getView()?.onGetBookLike(false)
        }
    }

    override fun getBookShelf(bookUrl: String) {
        val userByOnLine = CustomDatabase.get().userDao().getUserByOnLine(true)
        if (!userByOnLine.isNullOrEmpty()) {
            for(value in userByOnLine){
                try {
                    val userByShelf =
                        CustomDatabase.get().shelfDao().getUserShelfById(value.id, bookUrl)

                    if(!userByShelf.isNullOrEmpty()){
                        getView()?.onGetBookShelf(true)
                    } else{
                        getView()?.onGetBookShelf(false)
                    }
                }catch (e:Exception){
                    getView()?.onGetBookShelf(false)
                }

            }

        } else {
            getView()?.onGetBookShelf(false)
        }
    }

    fun localBookDetail(bookUrl: String): BookRecommend {
        return BookDatabase.get().bookDao().getBookRecommendByBookUrl(bookUrl)
    }

    fun bookDetail(bookRecommend: BookRecommend, onBookDetail: OnBookDetail) {
        if (bookRecommend.bookUrl.isEmpty()) {
            return
        }
        bookRepository.getBookDetail(bookRecommend.bookUrl, false).subscribe {
            try {
                val bookRecommendByBookUrl =
                    BookDatabase.get().bookDao().getBookRecommendByBookUrl(it.bookUrl)
                if (bookRecommendByBookUrl == null || (bookRecommendByBookUrl != null && bookRecommendByBookUrl.checkIsEmpty())) {
                    BookDatabase.get().bookDao().insertBookRecommend(it)
                }
            } catch (e: Exception) {
                LogUtils.e("BookRecommendPresenter", "get book detail save in sql error is $e")
            }
            onBookDetail.onBookDetail(it)
        }
    }

    public interface OnBookDetail {
        fun onBookDetail(bookRecommend: BookRecommend)
    }
}