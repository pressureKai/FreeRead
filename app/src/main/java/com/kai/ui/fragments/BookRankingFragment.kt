package com.kai.ui.fragments

import android.os.Bundle
import android.view.View
import com.kai.base.R
import com.kai.base.fragment.BaseMvpFragment
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import kotlinx.android.synthetic.main.fragment_book_ranking.*

class BookRankingFragment:BaseMvpFragment<BasePresenter<IView>>(){
    companion object{
        fun newInstance(): BookRankingFragment {
            val bookRackFragment =
                BookRankingFragment()
            val bundle = Bundle()
            bookRackFragment.arguments = bundle
            return bookRackFragment
        }
    }
    override fun createPresenter(): BasePresenter<IView>? {
       return null
    }

    override fun setLayoutId(): Int {
        return R.layout.fragment_book_ranking
    }

    override fun lazyInit(view: View, savedInstanceState: Bundle?) {
    }

    override fun initImmersionBar() {

    }
}