package com.kai.ui.bookdetail

import com.alibaba.android.arouter.facade.annotation.Route
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity



@Route(path = "/app/book")
class BookDetailActivity: BaseMvpActivity<BookDetailContract.View,BookDetailPresenter>(),BookDetailContract.View {
    override fun initView() {

    }

    override fun setLayoutId(): Int {
        return R.layout.activity_book_detail
    }

    override fun onLoadBookDetail(list: List<String>) {

    }


}