package com.kai.base.widget.load

import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.classic.common.MultipleStatusView
import com.kai.base.R
import com.kai.common.extension.customToast
import com.kai.common.utils.LogUtils
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
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/31 14:53
 */
class PageLoader<T>(
    private val mRecyclerView: RecyclerView,
    private val refreshDataDelegate: RefreshDataListener? = null,
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
        const val STATE_VIEW_ERROR = 0
        const val STATE_VIEW_EMPTY = 1
        const val STATE_VIEW_LOADING = 2
        const val STATE_VIEW_CONTENT = 3
        const val STATE_VIEW_NO_NETWORK = 4
        const val SMART_LOAD_REFRESH = 5
        const val SMART_LOAD_MORE = 6
        const val SMART_LOAD_FINISH = 7
    }

    private val data: ArrayList<T> = ArrayList()
    private var viewState: Int = STATE_VIEW_CONTENT
    private var loadState = SMART_LOAD_FINISH

    private var pageIndex = 0
    private var totalPage = 2
    private var pageSize = 10

    private val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )

    init {
        initRecyclerView()
        initSmartRefreshLayout()
        autoRefresh()
    }


    /**
     * @desc 自动刷新
     */
    private fun autoRefresh(){
        if(autoRefreshEnable) run {
            if(canLoadData(true)){
                viewState = STATE_VIEW_LOADING
                showLoading()
                LogUtils.e("PageLoader","load autoRefresh")
                mSmartRefreshLayout?.autoRefresh(100)
            }

        }
    }

    /**
     * @desc 检查页面的显示状态
     */
    private fun showMultipleStatusView() {
        when (viewState) {
            STATE_VIEW_ERROR -> {
                showError()
            }
            STATE_VIEW_EMPTY -> {
                showEmpty()
            }
            STATE_VIEW_LOADING -> {
                showLoading()
            }
            STATE_VIEW_CONTENT -> {
                showContent()
            }
            STATE_VIEW_NO_NETWORK -> {
                showNoNetwork()
            }
        }
        setEnableLoad(viewState != STATE_VIEW_LOADING)
    }


    /**
     * @desc smartRefreshLayout 刷新数据操作是否能够进行
     * @param enable 是否启动数据刷新
     */
    private fun setEnableLoad(enable: Boolean){
        mSmartRefreshLayout?.setEnableRefresh(enable)
        mSmartRefreshLayout?.setEnableLoadMore(enable)
    }

    private fun initSmartRefreshLayout() {
        mSmartRefreshLayout?.let {
            it.setRefreshHeader(ClassicsHeader(mRecyclerView.context))
            it.setRefreshFooter(ClassicsFooter(mRecyclerView.context))
            it.setOnRefreshListener {
                LogUtils.e("PageLoader","check refresh")
                if (canLoadData(true)) {
                    LogUtils.e("PageLoader","load refresh")
                    onRefresh()
                }
            }
            it.setOnLoadMoreListener {
                if (canLoadData(false)) {
                    loadMore()
                }
            }
        }
    }

    private fun initRecyclerView() {
        mRecyclerView.layoutManager = getLayoutManager()
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
        }
    }


    /**
     * @desc 将原有数据清除并填充新的数据
     */
    fun loadNewData(source: List<T>) {
        LogUtils.e("PageLoader","load form activity")
        data.clear()
        data.addAll(source)
        refreshState()
        refreshRecyclerView()
    }

    /**
     * @desc 刷新数据
     */
    private fun refreshState() {
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
        checkViewState()
    }


    /**
     * @desc  数据更新后检查页面状态
     */
    private fun checkViewState(){
         if(viewState != STATE_VIEW_NO_NETWORK && viewState != STATE_VIEW_ERROR){
             viewState = if(loadState == SMART_LOAD_FINISH){
                 if(data.size > 0){
                     STATE_VIEW_CONTENT
                 } else {
                     STATE_VIEW_EMPTY
                 }
             } else {
                 STATE_VIEW_LOADING
             }
         }
        showMultipleStatusView()
    }

    //有可能由接口实现的方法
    //(由于是网络请求或本地数据库的获取,并不能直接判断下拉加载和上拉刷新的操作是否成功)
    //必定存在回调(换言之必定存在接口的定义)
    //将Presenter层获取的数据通过调用方法传递参数的形式赋值给pageLoader中的data


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
     * @desc 是否能够进行数据的加载
     */
    private fun canLoadData(expectIsRefresh: Boolean): Boolean {
        var canLoad = false
        when (loadState) {
            SMART_LOAD_FINISH -> {
                canLoad = if (expectIsRefresh) {
                    true
                } else {
                    pageIndex < totalPage
                }
            }
            SMART_LOAD_MORE -> {
                canLoad = false
            }
            SMART_LOAD_REFRESH -> {
                canLoad = false
            }
        }
        return canLoad
    }

    /**
     * @desc 显示内容布局
     */
    private fun showContent() {
        mMultipleStatusView?.showContent()
    }

    /**
     * @desc 显示错误页面
     */
    private fun showError() {
        mMultipleStatusView?.showError(mLayoutErrorResource!!,layoutParams)
    }

    /**
     * @desc 显示空页面
     */
    private fun showEmpty() {
        mMultipleStatusView?.showEmpty(mLayoutEmptyResource!!,layoutParams)
    }

    /**
     * @desc 显示加载页面
     */
    private fun showLoading() {
        mMultipleStatusView?.showLoading(mLayoutLoadingResource!!,layoutParams)
    }

    /**
     * @desc 显示无网络页面
     */
    private fun showNoNetwork(){
        mMultipleStatusView?.showNoNetwork(mLayoutNotNetResource!!,layoutParams)
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
    fun setErrorResource(errorResource: Int){
        mLayoutErrorResource = errorResource
    }

    /**
     * @desc 设置无网络资源布局
     */
    fun setNoNetworkResource(noNetworkResource: Int){
        mLayoutNotNetResource = noNetworkResource
    }

}