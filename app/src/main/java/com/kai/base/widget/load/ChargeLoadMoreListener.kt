package com.kai.base.widget.load

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/4/2 11:44
 */
interface ChargeLoadMoreListener {
    fun couldLoadMore(pageIndex: Int,totalPage: Int): Boolean
}