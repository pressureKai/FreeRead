package com.kai.ui.fragments.shelf

import com.kai.base.mvp.base.IView

class ShelfContract {

    interface View : IView {
        fun onBanner()
    }


    interface Presenter{
        fun banner()
    }
}