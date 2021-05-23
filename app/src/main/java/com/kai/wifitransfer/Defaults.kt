/**
 * Copyright 2016 JustWayward Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kai.wifitransfer

import java.util.*

/**
 * @author yuyh.
 * @date 2016/10/10.
 */
class Defaults {


    companion object{
        const val port: Int = 8080
        var extensions: Map<String, String> = object : HashMap<String, String>() {
            init {
                put("htm", "text/html")
                put("html", "text/html")
                put("xml", "text/xml")
                put("txt", "text/plain")
                put("json", "text/plain")
                put("css", "text/css")
                put("ico", "image/x-icon")
                put("png", "image/png")
                put("gif", "image/gif")
                put("jpg", "image/jpg")
                put("jpeg", "image/jpeg")
                put("zip", "application/zip")
                put("rar", "application/rar")
                put("js", "text/javascript")
            }
        }
        const val HTML_STRING = ("<html>"
                + "<head><title>Air Drop</title>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">"
                + "</head>"
                + "<body>"
                + "<form action=\"\" method=\"post\" enctype=\"multipart/form-data\" name=\"form1\" id=\"form1\">"
                + "<label><input type=\"file\" name=\"file\" id=\"file\" /></label>"
                + "<input type=\"submit\" name=\"button\" id=\"button\" value=\"Submit\" />"
                + "</form></body></html>")
        const val root = "file:///android_asset/uploader/"
        const val indexPage = "index.html"


        fun getPort():Int{
            return port
        }
    }

}