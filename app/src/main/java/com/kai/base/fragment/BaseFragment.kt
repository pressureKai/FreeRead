package com.kai.base.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.gyf.immersionbar.components.ImmersionOwner
import com.gyf.immersionbar.components.ImmersionProxy
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView

abstract class BaseFragment<P : BasePresenter<Fragment>>:Fragment(), IView,ImmersionOwner {
    private var mPresenter :P ?= null
    lateinit var mRootView :View
    private var isCreate = false
    private var hasLoad = false
    private var isVisibleToUser = false
    private val mImmersionProxy :ImmersionProxy = ImmersionProxy(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        bindView()
        super.onCreate(savedInstanceState)
        mImmersionProxy.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mImmersionProxy.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getView(inflater, container, savedInstanceState)
    }


    private fun getView(inflater: LayoutInflater,
                        container: ViewGroup?,
                        savedInstanceState: Bundle?):View{
       mRootView = inflater.inflate(setLayoutId(), container, false)
       isCreate = true
       lazyLoad(mRootView, savedInstanceState)
       return mRootView
    }
    override fun bindView() {
        mPresenter = createPresenter()
        mPresenter?.register(this)
    }

    override fun unBindView() {
        mPresenter?.unRegister()
    }


    override fun onResume() {
        super.onResume()
        mImmersionProxy.onResume()
    }


    override fun onPause() {
        super.onPause()
        mImmersionProxy.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        unBindView()
        mImmersionProxy.onDestroy()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mImmersionProxy.onHiddenChanged(hidden)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mImmersionProxy.onConfigurationChanged(newConfig)
    }



    private fun lazyLoad(view: View, savedInstanceState: Bundle?){
        if(!isCreate || hasLoad || !isVisibleToUser){
           return
        }
        lazyInit(view, savedInstanceState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(!::mRootView.isInitialized){
            return
        }else{
            this.isVisibleToUser = isVisibleToUser
            lazyLoad(mRootView, null)
        }
        mImmersionProxy.isUserVisibleHint = isVisibleToUser
    }

    abstract fun createPresenter():P?
    abstract fun setLayoutId():Int
    abstract fun lazyInit(view: View, savedInstanceState: Bundle?)


    /**
     * 懒加载，在view初始化完成之前执行
     * On lazy after view.
     */
    override fun onLazyBeforeView() {}

    /**
     * 懒加载，在view初始化完成之后执行
     * On lazy before view.
     */
    override fun onLazyAfterView() {}

    /**
     * Fragment用户可见时候调用
     * On visible.
     */
    override fun onVisible() {}

    /**
     * Fragment用户不可见时候调用
     * On invisible.
     */
    override fun onInvisible() {}

    /**
     * 是否可以实现沉浸式，当为true的时候才可以执行initImmersionBar方法
     * Immersion bar enabled boolean.
     *
     * @return the boolean
     */
    override fun immersionBarEnabled(): Boolean {
        return true
    }


}