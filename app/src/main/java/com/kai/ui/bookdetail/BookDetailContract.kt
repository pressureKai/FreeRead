package com.kai.ui.bookdetail

import com.kai.base.mvp.base.IView
import com.kai.bookpage.model.BookChapterBean
import com.kai.crawler.entity.book.SearchBook

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-约束MVP中的接口
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class BookDetailContract {
    interface View :IView{
        fun onLoadBookDetail(list: List<String>)
        fun onLoadBookChapter(chapters: ArrayList<BookChapterBean>)
        fun onLoadBookContentByChapter(bookChapterBean: BookChapterBean,isOpen: Boolean)
    }

    interface Presenter{
        fun loadBookDetail()

        /**
         * # 获取书籍列表
         * @param [source] 书源用于Crawler查找章节列表
         * @param [id] 用于生成书籍展示实体类 - BookChapterBean 指定唯一Id
         */
        fun loadBookChapter(source: SearchBook.SL,id: Int)


        fun loadBookContentByChapter(bookChapterBean: BookChapterBean,isOpen: Boolean)
    }
}