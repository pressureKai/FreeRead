package com.kai.ui.bookinfo

import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.bookpage.model.BookRecommend


@Route(path = "/app/bookinfo")
class BookInfoActivity : BaseMvpActivity<BookInfoContract.View, BookInfoPresenter>(),
    BookInfoContract.View {
    override fun initView() {
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_book_info
    }

    override fun onBookDetail(recommend: BookRecommend) {

    }
}