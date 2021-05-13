package com.kai.base.widget.load

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.classic.common.MultipleStatusView
import com.kai.base.R
import com.kai.common.extension.closeDefaultAnimation
import com.kai.common.extension.customToast
import com.kai.common.utils.LogUtils
import com.kai.common.utils.RxNetworkObserver
import com.kai.crawler.entity.book.SearchBook
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import java.lang.Exception

/**
 *
 * @ProjectName:    app
 * @Description:    recyclerView - 数据加载封类(委托模式)
 *                  刷新事件 - 传递方向
 *                  1. View (MotionEvent) -> PageLoader
 *                  2. PageLoader监听 -> SmartRefreshLayout -> 通知View需要加载更多数据
 *                  3. 外部强制刷新数据
 *                  数据刷新与显示操作的封装类，
 *                  在Mvp模式中只有Activity与Fragment才能持有此类的实例化对象，
 *                  presenter 与 model 不应持有此类的实例化对象
 *
 *                  页数与页码如何做数据同步(通过外部赋值耦合度高)
 *
 *                  SmartRefreshLayout在做刷新和加载更多操作时，
 *                  如果MultipleStatusView做更新界面操作,
 *                  SmartRefreshLayout会被隐藏
 *                  so 不要在 SmartRefreshLayout 正在加载或正在加载更多时，去做MultipleStatusView的页面操作
 *                  换言之 MultipleStatusView 的操作必须在SmartRefreshLayout做数据操作之前
 *                  MultipleStatusView 可做操作的条件是
 *                  1. SmartRefreshLayout 不在做数据加载操作
 *                  2. 页面数据为空
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/31 14:53
 */
class ListPageLoader<T>(
        private val mRecyclerView: RecyclerView,
        private val refreshDataDelegate: RefreshDataListener? = null,
        private val chargeLoadMoreListener: ChargeLoadMoreListener? = null,
        private val mMultipleStatusView: MultipleStatusView? = null,
        private val mSmartRefreshLayout: SmartRefreshLayout? = null,
        private val mAdapter: BaseQuickAdapter<T, BaseViewHolder>? = null,
        private var mLayoutErrorResource: Int? = R.layout.layout_error,
        private var mLayoutEmptyResource: Int? = R.layout.layout_empty,
        private var mLayoutLoadingResource: Int? = R.layout.layout_loading,
        private var mLayoutNotNetResource: Int? = R.layout.layout_no_net,
        private var autoRefreshEnable: Boolean = true
) {
    companion object {
        const val SMART_LOAD_REFRESH = 5
        const val SMART_LOAD_MORE = 6
        const val SMART_LOAD_FINISH = 7

        const val DATA_STATE_SUCCESS = 0
        const val DATA_STATE_ERROR = 1
        const val DATA_STATE_NO_NETWORK = 2
    }

    private val data: ArrayList<T> = ArrayList()
    private var loadState = SMART_LOAD_FINISH

    private var pageIndex = 0
    private var totalPage = 0
    private var pageSize = 10

    private var mErrorView: View? = null
    private var mEmptyView: View? = null
    private var mLoadingView: View? = null
    private var mNoNetworkView: View? = null

    private var lastNetWorkState: Int = -2
    private var netWorkState: Int = -2

    private var mInflate = LayoutInflater.from(mRecyclerView.context)
    private val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
    )

    init {
        initRecyclerView()
        initSmartRefreshLayout()
        initNetWorkState()
    }


    /**
     * @desc 初始化网络监听状态
     */
    private fun initNetWorkState(){
        //在MainActivity中注册一个网络状态监听
        RxNetworkObserver.subscribe {
            if(lastNetWorkState == -2){
                //第一次接收到网络状态并做同步操作自动刷新数据
                lastNetWorkState = it
                netWorkState = it
                if(it != RxNetworkObserver.NET_STATE_DISCONNECT){
                    autoRefresh()
                } else {
                    //第一次检测为无网络状态
                    updateStatusView(MultipleStatusView.STATUS_NO_NETWORK)
                }
            } else {
                lastNetWorkState = netWorkState
                netWorkState = it
                if (lastNetWorkState != netWorkState
                    && netWorkState != RxNetworkObserver.NET_STATE_DISCONNECT
                    && lastNetWorkState == RxNetworkObserver.NET_STATE_DISCONNECT
                ) {
                    //网络状态改变由无网络变成有网络链接
                    autoRefresh()
                }
            }
        }
    }

    /**
     * @desc 自动刷新
     */
    private fun autoRefresh() {
        if (autoRefreshEnable) run {
            updateStatusView(MultipleStatusView.STATUS_LOADING)
            mSmartRefreshLayout?.autoRefresh(100)
        }
    }

    /**
     * @desc 更新页面显示状态
     */
    private fun updateStatusView(expectStatus: Int) {
        when (expectStatus) {
            MultipleStatusView.STATUS_ERROR -> {
                showError()
            }
            MultipleStatusView.STATUS_EMPTY -> {
                showEmpty()
            }
            MultipleStatusView.STATUS_LOADING -> {
                showLoading()
            }
            MultipleStatusView.STATUS_CONTENT -> {
                showContent()
            }
            MultipleStatusView.STATUS_NO_NETWORK -> {
                showNoNetwork()
            }
        }
        if (!autoRefreshEnable) {
            setEnableLoad(mMultipleStatusView?.viewStatus != MultipleStatusView.STATUS_LOADING)
        } else if(mMultipleStatusView?.viewStatus == MultipleStatusView.STATUS_LOADING) {
            autoRefreshEnable = false
        }
    }


    /**
     * @desc smartRefreshLayout 刷新数据操作是否能够进行
     * @param enable 是否启动数据刷新
     */
    private fun setEnableLoad(enable: Boolean) {
        mSmartRefreshLayout?.setEnableRefresh(enable)
        mSmartRefreshLayout?.setEnableLoadMore(enable)
    }


    /**
     * @desc 检查加载更多是否可用
     * @return 是否可用
     */
    private fun loadMoreEnable(): Boolean {
        var enable = false
        chargeLoadMoreListener?.let {
            enable = chargeLoadMoreListener.couldLoadMore(pageIndex, totalPage)
        } ?: run {
            enable == pageIndex < totalPage
        }
        if (!enable) {
            mSmartRefreshLayout?.finishLoadMore()
        }
        return enable
    }

    /**
     * @desc 显示网络状态
     * @param callBack 网络正常时回调刷新数据
     */
    private fun checkNetWorkState(callBack: () -> Unit) {
        if(netWorkState == -2){
            refreshState()
            mRecyclerView.context.customToast("正在同步网络状态,请稍后再试")
        } else {
            if (netWorkState == RxNetworkObserver.NET_STATE_DISCONNECT) {
                loadData(responseState = DATA_STATE_NO_NETWORK)
            } else {
                callBack.invoke()
            }
        }
    }

    private fun initSmartRefreshLayout() {
        mSmartRefreshLayout?.let {
            it.setRefreshHeader(ClassicsHeader(mRecyclerView.context))
            it.setRefreshFooter(ClassicsFooter(mRecyclerView.context))
            it.setOnRefreshListener {
                checkNetWorkState {
                    onRefresh()
                }
            }
            it.setOnLoadMoreListener {
                checkNetWorkState {
                    if (loadMoreEnable()) {
                        loadMore()
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        mRecyclerView.layoutManager = getLayoutManager()
        mRecyclerView.closeDefaultAnimation()
        mAdapter?.let {
            mRecyclerView.adapter = it
            it.setNewInstance(data)
        }
    }

    /**
     * @desc 获取RecyclerView 的布局管理器
     */
    private fun getLayoutManager(): RecyclerView.LayoutManager {
        val linearLayoutManager = LinearLayoutManager(mRecyclerView.context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        return linearLayoutManager
    }


    /**
     * @desc 刷新列表数据
     */
    private fun refreshRecyclerView() {
        mAdapter?.let {
            mAdapter.setNewInstance(data)
            mAdapter.notifyDataSetChanged()
        }
    }


    /**
     * @desc 装载数据
     * @param source 即将刷新的数据源
     * @param responseState 回调状态  0: success,1: fail, 2 :no_net
     */
    fun loadData(source: List<T> = ArrayList(), responseState: Int = DATA_STATE_SUCCESS) {
        if (responseState == 0) {
            //回调状态为0时,即代表数据请求成功,处理数据。
            if (loadState == SMART_LOAD_REFRESH) {
                data.clear()
                data.addAll(source)
            } else {
                data.addAll(source)
            }
        }
        refreshState()
        refreshMultipleStatusView(responseState)
        refreshRecyclerView()
    }

    /**
     * @desc 刷新MultipleStatusView显示状态
     * @param responseState 回调状态
     */
    private fun refreshMultipleStatusView(responseState: Int) {
        if (loadState == SMART_LOAD_FINISH) {
            if (data.size > 0) {
                updateStatusView(MultipleStatusView.STATUS_CONTENT)
            } else {
                when (responseState) {
                    0 -> {
                        updateStatusView(MultipleStatusView.STATUS_EMPTY)
                    }
                    1 -> {
                        //回调状态为1且数据为空
                        updateStatusView(MultipleStatusView.STATUS_ERROR)
                    }
                    else -> {
                        updateStatusView(MultipleStatusView.STATUS_NO_NETWORK)
                    }
                }
            }
        }
    }

    /**
     * @desc 刷新SmartRefreshLayout的显示状态
     */
    fun refreshState() {
        when (loadState) {
            SMART_LOAD_FINISH -> {
                mSmartRefreshLayout?.finishLoadMore()
                mSmartRefreshLayout?.finishRefresh()
            }
            SMART_LOAD_MORE -> {
                mSmartRefreshLayout?.finishLoadMore()
                loadState = SMART_LOAD_FINISH
            }
            SMART_LOAD_REFRESH -> {
                mSmartRefreshLayout?.finishRefresh()
                loadState = SMART_LOAD_FINISH
            }
        }
    }

    /**
     * @desc 加载更多
     */
    private fun loadMore() {
        loadState = SMART_LOAD_MORE
        refreshDataDelegate?.onLoadMore(pageIndex, pageSize) ?: run {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshState()
            }, 200)
        }
    }


    /**
     * @desc 刷新数据
     */
    private fun onRefresh() {
        loadState = SMART_LOAD_REFRESH
        refreshDataDelegate?.onRefresh() ?: run {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshState()
            }, 200)
        }
    }


    /**
     * @desc 显示内容布局
     */
    private fun showContent() {
        mMultipleStatusView?.showContent()
        clearAllView()
    }

    /**
     * @desc 显示错误页面
     */
    private fun showError() {
        mErrorView ?: run {
            mErrorView = mInflate.inflate(mLayoutErrorResource!!, null)
        }
        mMultipleStatusView?.showError(mErrorView, layoutParams)
        clearAllView()
    }

    /**
     * @desc 显示空页面
     */
    private fun showEmpty() {
        mEmptyView ?: run {
            mEmptyView = mInflate.inflate(mLayoutEmptyResource!!, null)
        }
        mMultipleStatusView?.showEmpty(mEmptyView, layoutParams)
        clearAllView()
    }

    /**
     * @desc 显示加载页面
     */
    private fun showLoading() {
        mLoadingView ?: run {
            mLoadingView = mInflate.inflate(mLayoutLoadingResource!!, null)
        }
        mMultipleStatusView?.showLoading(mLoadingView, layoutParams)
        clearAllView()
    }

    /**
     * @desc 显示无网络页面
     */
    private fun showNoNetwork() {
        mNoNetworkView ?: run {
            mNoNetworkView = mInflate.inflate(mLayoutNotNetResource!!, null)
        }
        mMultipleStatusView?.showNoNetwork(mNoNetworkView, layoutParams)
        clearAllView()
    }

    /**
     * @desc 设置空布局资源
     */
    fun setEmptyResource(emptyResource: Int) {
        mLayoutEmptyResource = emptyResource
    }

    /**
     * @desc 设置加载布局资源
     */
    fun setLoadingResource(loadingResource: Int) {
        mLayoutLoadingResource = loadingResource
    }

    /**
     * @desc 设置错误布局资源
     */
    fun setErrorResource(errorResource: Int) {
        mLayoutErrorResource = errorResource
    }

    /**
     * @desc 设置无网络资源布局
     */
    fun setNoNetworkResource(noNetworkResource: Int) {
        mLayoutNotNetResource = noNetworkResource
    }


    /**
     * @desc 由于MultipleStatusView 会出现状态重叠的bug 使用此方法辅助修复
     */
    private fun clearAllView() {
        try {
            when (mMultipleStatusView?.viewStatus) {
                MultipleStatusView.STATUS_NO_NETWORK -> {
                    mEmptyView?.visibility = View.INVISIBLE
                    mErrorView?.visibility = View.INVISIBLE
                    mNoNetworkView?.visibility = View.VISIBLE
                    mLoadingView?.visibility = View.INVISIBLE
                }
                MultipleStatusView.STATUS_ERROR -> {
                    mEmptyView?.visibility = View.INVISIBLE
                    mErrorView?.visibility = View.VISIBLE
                    mNoNetworkView?.visibility = View.INVISIBLE
                    mLoadingView?.visibility = View.INVISIBLE
                }
                MultipleStatusView.STATUS_CONTENT -> {
                    mEmptyView?.visibility = View.INVISIBLE
                    mErrorView?.visibility = View.INVISIBLE
                    mNoNetworkView?.visibility = View.INVISIBLE
                    mLoadingView?.visibility = View.INVISIBLE
                }
                MultipleStatusView.STATUS_EMPTY -> {
                    mEmptyView?.visibility = View.VISIBLE
                    mErrorView?.visibility = View.INVISIBLE
                    mNoNetworkView?.visibility = View.INVISIBLE
                    mLoadingView?.visibility = View.INVISIBLE
                }
                MultipleStatusView.STATUS_LOADING -> {
                    mEmptyView?.visibility = View.INVISIBLE
                    mErrorView?.visibility = View.INVISIBLE
                    mNoNetworkView?.visibility = View.INVISIBLE
                    mLoadingView?.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            LogUtils.e("PageLoader","error is $e")
        }
    }


    fun setLoadState(loadState: Int){
        this.loadState = loadState
    }

    fun finishAll(){
        loadState = SMART_LOAD_FINISH
        refreshState()
    }
}