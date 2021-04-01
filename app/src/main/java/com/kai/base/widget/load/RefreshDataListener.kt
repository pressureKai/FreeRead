package com.kai.base.widget.load

/**
 *
 * @ProjectName:    app-bookpage
 * @Description:    向外传递数据刷新操作
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/1 9:15
 */
interface RefreshDataListener {
    /**
     * @des pageLoader向外传递加载更多事件
     * @param pageIndex 页码
     * @param pageSize 总页数
     */
    fun onLoadMore(pageIndex: Int,pageSize: Int)

    /**
     * @des pageLoader向外传递刷新第一页数据事件
     */
    fun onRefresh()
}