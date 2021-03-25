package com.kai.model

/**
 *
 * @ProjectName:    App-bookPage
 * @Description:    项目中关于书籍的Model  //model 层参考链接
 * // https://mp.weixin.qq.com/s?__biz=MzA4NTQwNDcyMA%3D%3D&mid=2650662653&idx=1&sn=e15a36e4460eb3d1890d92aa921c0962&scene=45#wechat_redirect
 * // https://github.com/android/architecture-samples
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