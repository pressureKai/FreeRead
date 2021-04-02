package com.kai.base.widget.load

/**
 *
 * @ProjectName:    app-bookpage
 * @Description:    判断是否能够加载更多,由接口实现方便扩展PageLoader
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/2 11:44
 */
interface ChargeLoadMoreListener {
    fun couldLoadMore(pageIndex: Int,totalPage: Int): Boolean
}