package com.kai.base.rxhttp.download

import com.kai.base.rxhttp.factory.ApiFactory
import com.kai.base.rxhttp.interceptor.Transformer
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody

/**
 * Created by allen on 2017/6/14.
 *
 *
 *
 * @author Allen
 * 为下载单独建一个retrofit
 */
object DownloadHelper {
    fun downloadFile(fileUrl: String?): Observable<ResponseBody> {
        val defaultDownloadKey = "defaultDownloadUrlKey"
        val defaultBaseUrl = "https://api.github.com/"
        return ApiFactory.instance!!
            .setOkClient(OkHttpClient.Builder().addInterceptor(DownloadInterceptor()).build())
            .createApi(defaultDownloadKey, defaultBaseUrl, DownloadApi::class.java)
            ?.downloadFile(fileUrl)!!
            .compose(Transformer.switchSchedulers())
    }
}