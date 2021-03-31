package com.kai.base.widget.load

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.classic.common.MultipleStatusView
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout


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
 *                  question is how can i know the data load finish
 *                  and when i operation that clear list
 *                  or  when i operation that add data to the list
 *
 *                  页数与页码如何做数据同步(通过外部赋值耦合程度高)
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/31 14:53
 */
class PageLoader<T>(
    private val mRecyclerView: RecyclerView,
    private val mMultipleStatusView: MultipleStatusView? = null,
    private val mSmartRefreshLayout: SmartRefreshLayout? = null,
    private val mAdapter: BaseQuickAdapter<T, BaseViewHolder>? = null
) {
    companion object {
        const val STATE_ERROR = 0
        const val STATE_EMPTY = 1
        const val STATE_LOADING = 2
        const val STATE_CONTENT = 3
        const val SMART_LOAD_REFRESH = 5
        const val SMART_LOAD_MORE = 6
        const val SMART_LOAD_FINISH = 7
    }

    private val data: ArrayList<T> = ArrayList()
    private var viewState: Int = STATE_LOADING

    private var pageIndex = 0
    private var totalPage = 2
    private var pageSize = 10
    private var loadState = SMART_LOAD_FINISH


    init {
        initRecyclerView()
        initSmartRefreshLayout()
        checkMultipleStatus()
    }

    /**
     * @desc 检查页面的显示状态
     */
    private fun checkMultipleStatus() {

    }

    private fun initSmartRefreshLayout() {
        mSmartRefreshLayout?.let {
            it.setRefreshHeader(ClassicsHeader(mRecyclerView.context))
            it.setRefreshFooter(ClassicsFooter(mRecyclerView.context))
            it.setOnRefreshListener {
                viewState = if (data.isEmpty()) {
                    STATE_LOADING
                } else {
                    STATE_CONTENT
                }
                if(canLoadData(true)){
                    onRefresh()
                }

            }
            it.setOnLoadMoreListener {
                if(canLoadData(false)){
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

    open fun getLayoutManager(): RecyclerView.LayoutManager {
        val linearLayoutManager = LinearLayoutManager(mRecyclerView.context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        return linearLayoutManager
    }


    private fun refreshRecyclerView() {
        mAdapter?.let {
            mAdapter.setNewInstance(data)
        }
    }


    /**
     * @desc 将原有数据清除并填充新的数据
     */
    fun loadNewData(source: List<T>) {
        data.clear()
        data.addAll(source)
        refreshRecyclerView()
    }

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
        Handler(Looper.getMainLooper()).postDelayed({
            refreshState()
        }, 200)
    }


    /**
     * @desc 刷新数据
     */
    private fun onRefresh() {
        loadState = SMART_LOAD_REFRESH
        Handler(Looper.getMainLooper()).postDelayed({
            refreshState()
        }, 200)
    }


    /**
     * @desc 是否能够进行数据的加载
     */
    open fun canLoadData(expectIsRefresh: Boolean): Boolean {
        var canLoad = false
        when (loadState) {
            SMART_LOAD_FINISH -> {
                canLoad = if(expectIsRefresh){
                    true
                }else{
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
}