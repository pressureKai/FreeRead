package com.kai.ui.fragments.ranking

import com.kai.base.mvp.base.IView

class RankingContract {

    interface View : IView {
        fun onRanking(hashMap: HashMap<Int,String>)
    }


    interface Presenter{
        fun ranking()
    }
}