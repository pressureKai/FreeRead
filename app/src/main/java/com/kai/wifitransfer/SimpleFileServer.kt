package com.kai.wifitransfer

import android.app.Application
import android.content.Intent
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.kai.common.application.BaseApplication
import com.kai.common.utils.LogUtils
import com.kai.common.utils.LogUtils.Companion.e
import com.kai.util.FileHelper
import com.kai.util.FileUtils
import com.kai.util.FileUtils.Companion.createWifiTempFile
import com.kai.util.FileUtils.Companion.createWifiTranfesFile
import com.kai.util.FileUtils.Companion.fileChannelCopy
import com.kai.util.FileUtils.Companion.readAssets
import com.kai.util.PermissionHelper
import com.kai.wifitransfer.Defaults.Companion.extensions
import com.kai.wifitransfer.Defaults.Companion.getPort
import java.io.*
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * Wifi传书 服务端
 *
 * @author yuyh.
 * @date 2016/10/10.
 */
class SimpleFileServer(port: Int) : NanoHTTPD(port) {
    override fun serve(
        uri: String?, method: Method?,
        header: Map<String, String>?, parms: Map<String, String?>?,
        files: Map<String, String>?
    ): Response? {
        var uri = uri
        return if (Method.GET == method) {
            try {
                uri = String(uri!!.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
                e("ee", "uri= $uri")
            } catch (e: UnsupportedEncodingException) {
                e("ee", "URL参数编码转换错误：$e")
            }
            if (uri!!.contains("index.html") || uri == "/") {
                Response(
                    Response.Status.OK, "text/html", String(
                        readAssets("/index.html")!!
                    )
                )
            } else if (uri.startsWith("/files/") && uri.endsWith(".txt")) {
                val name = parms!!["name"]
                val start = parms["start"]
                var startIndex = 0
                try {
                    startIndex = start!!.toInt()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }
                val printName: String = try {
                    URLDecoder.decode(parms["name"], "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    "tt"
                }
                val bookid = uri.substring(7, uri.lastIndexOf("."))
                e("SimpleFileServer", "-->uri= $uri;name:$printName;start:$start")
                //先加载章节列表
                val resp = Response(Response.Status.OK, MIME_DEFAULT_BINARY, bookid, startIndex)
                resp.addHeader(
                    "content-disposition",
                    "attachment;filename=" + printName + "_" + startIndex + ".txt"
                )
                resp
            } else if (uri.startsWith("/files")) {
//                List<Recommend.RecommendBooks> collectionList = CollectionsManager.getInstance().getCollectionList();
//                List<HtmlBook> htmlBooks = new ArrayList<>();
//                for (Recommend.RecommendBooks recommendBooks : collectionList) {
//                    htmlBooks.add(new HtmlBook(recommendBooks.title, recommendBooks._id, recommendBooks.lastChapter));
//                }
                Response(Response.Status.OK, "text/json", Gson().toJson(""))
            } else {
                // 获取文件类型
                val type = extensions[uri.substring(
                    uri.lastIndexOf(".") + 1
                )]
                if (TextUtils.isEmpty(type)) return Response("")
                // 读取文件
                val b = readAssets(uri)
                if (b == null || b.isEmpty()) Response("") else Response(
                    Response.Status.OK,
                    type!!,
                    ByteArrayInputStream(b)
                )
                // 响应
            }
        } else {
            // 读取文件
            for (s in files!!.keys) {
                try {
                    val fis = FileInputStream(files[s])
                    var fileName = parms!!["newfile"]
                    if (fileName!!.lastIndexOf(".") > 0) {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."))
                    }

                    val outputFile = createWifiTempFile()
                    e("SimpleFileServer", "file is exist ${outputFile.exists()}")
                    val fos = FileOutputStream(outputFile)
                    val buffer = ByteArray(1024)
                    while (true) {
                        val byteRead = fis.read(buffer)
                        if (byteRead == -1) {
                            break
                        }
                        fos.write(buffer, 0, byteRead)
                    }
                    fos.close()

                    try {
                        // 创建目标文件
                        val desc = createWifiTranfesFile(
                            fileName!!
                        )
                        e("SimpleFileServer", "--" + desc.absolutePath)
                        fileChannelCopy(outputFile, desc)
                    } catch (e: Exception) {
                        e("SimpleFileServer", "-- copy error is $e")
                    }
                } catch (e: FileNotFoundException) {
                    e("SimpleFileServer", "-- copy error is $e")
                    e.printStackTrace()
                } catch (e: IOException) {
                    e("SimpleFileServer", "-- copy error is $e")
                    e.printStackTrace()
                }
            }
            Response("")
        }
    }

    companion object {
        private var server: SimpleFileServer? = null
        val instance: SimpleFileServer?
            get() {
                if (server == null) {
                    server = SimpleFileServer(getPort())
                }
                return server
            }
    }

}