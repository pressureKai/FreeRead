package com.kai.crawler.source.callback

import com.kai.crawler.source.model.SearchBook

/**
 *
 * @ProjectName:     bookpage
 * @Description:     搜索书籍回调
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:08
 */
interface SearchCallBack {
    /**
     * @param keyword 搜索关键字
     * @param appendList 书源过多搜索优化分批回调
     */
     fun onResponse(keyword:String,appendList: List<SearchBook>)

    /**
     * des 所有源查询结束回调
     */
     fun onFinish()

     fun onError(msg: String)
}