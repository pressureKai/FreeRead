package com.kai.view.cardstack.tools

/**
 * @author vondear
 * @date 2018/6/11 11:36:40 整合修改
 */
interface RxScrollDelegate {
    fun scrollViewTo(x: Int, y: Int)
    var viewScrollY: Int
    var viewScrollX: Int
}