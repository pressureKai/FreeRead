package com.kai.ui.fragments

import android.os.Bundle
import android.view.View
import com.kai.base.R
import com.kai.base.fragment.BaseMvpFragment
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import kotlinx.android.synthetic.main.fragment_book_shelf.*

class BookShelfFragment:BaseMvpFragment<BasePresenter<IView>>(){
    companion object{
        fun newInstance(): BookShelfFragment {
            val bookRackFragment =
                BookShelfFragment()
            val bundle = Bundle()
            bookRackFragment.arguments = bundle
            return bookRackFragment
        }
    }
    override fun createPresenter(): BasePresenter<IView>? {
       return null
    }

    override fun setLayoutId(): Int {
        return R.layout.fragment_book_shelf
    }

    override fun lazyInit(view: View, savedInstanceState: Bundle?) {
    }

    override fun initImmersionBar() {

    }

}