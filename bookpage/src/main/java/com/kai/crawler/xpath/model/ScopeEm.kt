package com.kai.crawler.xpath.model

/**
 * 筛选作用域
 */
enum class ScopeEm(  //当前节点向下递归
    private val `val`: String
) {
    INCHILREN("/"),  //默认只在子代中筛选,有轴时由轴定义筛选域
    RECURSIVE("//"),  //向下递归查找
    CUR("./"),  //当前节点下
    CURREC(".//");

    fun `val`(): String {
        return `val`
    }
}