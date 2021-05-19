package com.kai.ui.fragments.shelf

import android.os.Bundle
import android.view.View
import com.kai.base.R
import com.kai.base.fragment.BaseMvpFragment

class BookShelfFragment:BaseMvpFragment<ShelfContract.View,ShelfPresenter>(){
    companion object{
        fun newInstance(): BookShelfFragment {
            val bookRackFragment =
                BookShelfFragment()
            val bundle = Bundle()
            bookRackFragment.arguments = bundle
            return bookRackFragment
        }
    }
    override fun createPresenter(): ShelfPresenter? {
       return ShelfPresenter()
    }

    override fun setLayoutId(): Int {
        return R.layout.fragment_book_shelf
    }

    override fun lazyInit(view: View, savedInstanceState: Bundle?) {
    }

    override fun initImmersionBar() {

    }

}