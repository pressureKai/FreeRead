package com.kai.model.book

import io.reactivex.rxjava3.core.Observable


/**
 *
 * @ProjectName:    App-bookPage
 * @Description:    项目中书籍数据源
 * // https://mp.weixin.qq.com/s?__biz=MzA4NTQwNDcyMA%3D%3D&mid=2650662653&idx=1&sn=e15a36e4460eb3d1890d92aa921c0962&scene=45#wechat_redirect
 * // https://github.com/android/architecture-samples
 *
 * 1.引入Rxjava 防止回调地狱
 *    思路 1). 在Model的实现类中 使用Rxjava向外传递一个 Observable 可观测数据 传递给Presenter
 *        2). presenter 层接收到可观测数据后 使用 subscribe 方法监听数据变化回调相应的View层中的方法
 *        3). 如何将Model与View关联并下沉到BasePresenter中
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/23 18:15
 */
interface BookDataSource {
    /**
     * des 获取书籍推荐列表
     * @return 书籍列表
     */
    fun getBookRecommend():Observable<List<String>>?
}