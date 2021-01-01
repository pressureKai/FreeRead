package com.kai.base.rxhttp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.kai.base.rxhttp.config.OkHttpConfig
import com.kai.base.rxhttp.cookie.CookieJarImpl
import com.kai.base.rxhttp.cookie.store.CookieStore
import com.kai.base.rxhttp.download.DownloadHelper
import com.kai.base.rxhttp.factory.ApiFactory
import com.kai.base.rxhttp.manager.RxHttpManager
import com.kai.base.rxhttp.upload.UploadHelper
import io.reactivex.rxjava3.core.Observable
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.ResponseBody

/**
 * Created by allen on 2017/6/22.
 *
 * @author Allen
 * 网络请求
 */
class RxHttpUtils {
    /**
     * 必须在全局Application先调用，获取context上下文，否则缓存无法使用
     *
     * @param app Application
     */
    fun init(app: Application?): RxHttpUtils {
        context = app
        return this
    }

    fun config(): ApiFactory {
        checkInitialize()
        return ApiFactory.getInstance()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: RxHttpUtils ? = null
            get() {
                if (field == null) {
                    synchronized(RxHttpUtils::class.java) {
                        if (field == null) {
                            field = RxHttpUtils()
                        }
                    }
                }
                return field
            }
            private set

        @SuppressLint("StaticFieldLeak")
        private var context: Application? = null

        /**
         * 获取全局上下文
         */
        fun getContext(): Context? {
            checkInitialize()
            return context
        }

        /**
         * 检测是否调用初始化方法
         */
        private fun checkInitialize() {
            if (context == null) {
                throw ExceptionInInitializerError("请先在全局Application中调用 RxHttpUtils.getInstance().init(this) 初始化！")
            }
        }

        /**
         * 使用全局参数创建请求
         *
         * @param cls Class
         * @param <K> K
         * @return 返回
        </K> */
        fun <K> createApi(cls: Class<K>?): K {
            return ApiFactory.getInstance().createApi(cls)
        }

        /**
         * 切换baseUrl
         *
         * @param baseUrlKey   域名的key
         * @param baseUrlValue 域名的url
         * @param cls          class
         * @param <K>          k
         * @return k
        </K> */
        fun <K> createApi(baseUrlKey: String?, baseUrlValue: String?, cls: Class<K>?): K {
            return ApiFactory.getInstance().createApi(baseUrlKey, baseUrlValue, cls)
        }

        /**
         * 下载文件
         *
         * @param fileUrl 地址
         * @return ResponseBody
         */
        fun downloadFile(fileUrl: String?): Observable<ResponseBody> {
            return DownloadHelper.downloadFile(fileUrl)
        }

        /**
         * 上传单张图片
         *
         * @param uploadUrl 地址
         * @param filePath  文件路径
         * @return ResponseBody
         */
        fun uploadImg(uploadUrl: String?, filePath: String?): Observable<ResponseBody> {
            return UploadHelper.uploadImage(uploadUrl, filePath!!)
        }

        /**
         * 上传多张图片
         *
         * @param uploadUrl 地址
         * @param filePaths 文件路径
         * @return ResponseBody
         */
        fun uploadImages(uploadUrl: String?, filePaths: List<String>): Observable<ResponseBody> {
            return UploadHelper.uploadImages(uploadUrl, filePaths)
        }
        /**
         * 上传多张图片
         *
         * @param uploadUrl 地址
         * @param filePaths 文件路径
         * @return ResponseBody
         */
        /**
         * 上传多张图片
         *
         * @param uploadUrl 地址
         * @param fileName  后台接收文件流的参数名
         * @param paramsMap 参数
         * @param filePaths 文件路径
         * @return ResponseBody
         */
        fun uploadImagesWithParams(
            uploadUrl: String?,
            fileName: String?,
            paramsMap: Map<String?, Any?>?,
            filePaths: List<String>
        ): Observable<ResponseBody> {
            return UploadHelper.uploadFilesWithParams(uploadUrl, fileName, paramsMap, filePaths)
        }

        /**
         * 获取全局的CookieJarImpl实例
         */
        private val cookieJar: CookieJarImpl
            get() = OkHttpConfig.instance!!.okHttpClient!!.cookieJar() as CookieJarImpl

        /**
         * 获取全局的CookieStore实例
         */
        private val cookieStore: CookieStore
            get() = cookieJar.cookieStore

        /**
         * 获取所有cookie
         */
        val allCookie: List<Any>
            get() {
                val cookieStore: CookieStore = cookieStore
                return cookieStore.allCookie
            }

        /**
         * 获取某个url所对应的全部cookie
         */
        fun getCookieByUrl(url: String): List<Cookie> {
            val cookieStore: CookieStore = cookieStore
            val httpUrl: HttpUrl = HttpUrl.parse(url)!!
            return cookieStore.getCookie(httpUrl)
        }

        /**
         * 移除全部cookie
         */
        fun removeAllCookie() {
            val cookieStore: CookieStore = cookieStore
            cookieStore.removeAllCookie()
        }

        /**
         * 移除某个url下的全部cookie
         */
        fun removeCookieByUrl(url: String?) {
            val httpUrl: HttpUrl = HttpUrl.parse(url!!)!!
            val cookieStore: CookieStore = cookieStore
            cookieStore.removeCookie(httpUrl)
        }

        /**
         * 取消所有请求
         */
        fun cancelAll() {
            RxHttpManager.get()?.cancelAll()
        }

        /**
         * 取消某个或某些请求
         */
        fun cancel(vararg tag: Any?) {
            RxHttpManager.get()?.cancel(tag)
        }
    }
}