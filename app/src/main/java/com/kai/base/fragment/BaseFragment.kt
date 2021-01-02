package com.kai.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView

abstract class BaseFragment<P:BasePresenter<Fragment>>:Fragment(), IView {
    private var mPresenter :P ?= null
    lateinit var mRootView :View
    private var isCreate = false
    private var hasLoad = false
    private var isVisibleToUser = false
    override fun onCreate(savedInstanceState: Bundle?) {
        bindView()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getView(inflater,container,savedInstanceState)
    }


    private fun getView(inflater: LayoutInflater,
                        container: ViewGroup?,
                        savedInstanceState: Bundle?):View{
       mRootView = inflater.inflate(setLayoutId(),container,false)
       isCreate = true
       lazyLoad(mRootView,savedInstanceState)
       return mRootView
    }
    override fun bindView() {
        mPresenter = createPresenter()
        mPresenter?.register(this)
    }

    override fun unBindView() {
        mPresenter?.unRegister()
    }


    override fun onDestroy() {
        super.onDestroy()
        unBindView()
    }



    private fun lazyLoad(view:View,savedInstanceState: Bundle?){
        if(!isCreate || hasLoad || !isVisibleToUser){
           return
        }
        lazyInit(view,savedInstanceState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(!::mRootView.isInitialized){
            return
        }else{
            this.isVisibleToUser = isVisibleToUser
            lazyLoad(mRootView,null)
        }
    }

    abstract fun createPresenter():P?
    abstract fun setLayoutId():Int
    abstract fun lazyInit(view:View,savedInstanceState: Bundle?)
}