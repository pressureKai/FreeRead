package com.kai.base.rxhttp.utils

import android.content.Context
import android.net.ConnectivityManager
import com.kai.base.rxhttp.RxHttpUtils.Companion.getContext

/**
 * <pre>
 * @author : Allen
 * date    : 2018/06/14
 * desc    : 管理管理类
 * version : 1.0
</pre> *
 */
object NetUtils {
    /**
     * 判断是否有网络
     *
     * @return 返回值
     */
    val isNetworkConnected: Boolean
        get() {
            val context = getContext()
            if (context != null) {
                val mConnectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
            return false
        }
}