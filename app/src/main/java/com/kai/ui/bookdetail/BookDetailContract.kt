package com.kai.ui.bookdetail

import com.kai.base.mvp.base.IView

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

    }

    interface Presenter{
        fun loadBookDetail()
    }
}