package com.kai.common.rxhttp.upload
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

/**
 * Created by allen on 2017/6/15.
 *
 *
 *
 * @author Allen
 * 文件上传
 */
interface UploadFileApi {
    /**
     * 上传多个文件
     *
     * @param uploadUrl 地址
     * @param files      文件
     * @return ResponseBody
     */
    @Multipart
    @POST
    fun uploadFiles(
        @Url uploadUrl: String?,
        @Part files: List<MultipartBody.Part>
    ): Observable<ResponseBody>
}