package com.kai.common.rxhttp.upload

import com.kai.common.rxhttp.factory.ApiFactory
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.util.*

/**
 * <pre>
 * @author : Allen
 * date    : 2018/06/14
 * desc    : 为上传单独建一个retrofit
 * version : 1.0
</pre> *
 */
object UploadHelper {
    /**
     * 上传一张图片
     *
     * @param uploadUrl 上传图片的服务器url
     * @param filePath  图片路径
     * @return Observable
     */
    fun uploadImage(uploadUrl: String?, filePath: String): Observable<ResponseBody> {
        val filePaths: MutableList<String> = ArrayList()
        filePaths.add(filePath)
        return uploadFilesWithParams(uploadUrl, "uploaded_file", null, filePaths)
    }

    /**
     * 只上传图片
     *
     * @param uploadUrl 上传图片的服务器url
     * @param filePaths 图片路径
     * @return Observable
     */
    fun uploadImages(uploadUrl: String?, filePaths: List<String>): Observable<ResponseBody> {
        return uploadFilesWithParams(uploadUrl, "uploaded_file", null, filePaths)
    }

    /**
     * 图片和参数同时上传的请求
     *
     * @param uploadUrl 上传图片的服务器url
     * @param fileName  后台协定的接受图片的name（没特殊要求就可以随便写）
     * @param paramsMap 普通参数
     * @param filePaths 图片路径
     * @return Observable
     */
    fun uploadFilesWithParams(
        uploadUrl: String?,
        fileName: String?,
        paramsMap: Map<String?, Any?>?,
        filePaths: List<String>
    ): Observable<ResponseBody> {
        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        if (null != paramsMap) {
            for (key in paramsMap.keys) {
                builder.addFormDataPart(key, paramsMap[key] as String)
            }
        }
        for (i in filePaths.indices) {
            val file = File(filePaths[i])
            val imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            //"fileName"+i 后台接收图片流的参数名
            builder.addFormDataPart(fileName!!, file.name, imageBody)
        }
        val parts = builder.build().parts()
        val defaultUploadKey = "defaultUploadUrlKey"
        val defaultBaseUrl = "https://api.github.com/"
        return ApiFactory.instance!!
                .createApi(defaultUploadKey, defaultBaseUrl, UploadFileApi::class.java)!!
                .uploadFiles(uploadUrl, parts)
    }
}