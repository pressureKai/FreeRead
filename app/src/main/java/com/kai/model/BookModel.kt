package com.kai.model

/**
 *
 * @ProjectName:    App-bookPage
 * @Description:    项目关于书籍的Model
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:15
 */
interface BookModel {
    /**
     * des 获取书籍推荐列表
     * @return 书籍列表
     */
    fun getBookRecommend(): List<String>
}