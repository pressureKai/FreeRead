package com.kai.crawler.callback

/**
 *
 * @ProjectName:     bookPage
 * @Description:     小说主体内容回调
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:05
 */
interface ContentCallBack {
     fun onResponse(content: String)
     fun onError(msg: String)
}