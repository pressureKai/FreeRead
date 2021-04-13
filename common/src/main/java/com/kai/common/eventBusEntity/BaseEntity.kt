package com.kai.common.eventBusEntity


/**
 *# 通用实体类 - (模拟传输网络数据)
 *   code : 0 成功
 *        ：1 失败
 *@author kai
 *@date  2021/4/13
 */
class BaseEntity<T> {
    companion object{
       const val ENTITY_SUCCESS_CODE = 0
       const val ENTITY_FAIL_CODE = 1
    }
    var code = -1
    var data :T ?= null
    var message :String = ""
}