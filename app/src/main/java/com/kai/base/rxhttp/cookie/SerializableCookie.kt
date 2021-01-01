/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kai.base.rxhttp.cookie

import android.content.ContentValues
import android.database.Cursor
import okhttp3.Cookie
import java.io.*
import java.util.*
import kotlin.experimental.and

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：16/9/11
 * 描    述：
 * 修订历史：
 * ================================================
 */
class SerializableCookie(var host: String?, @field:Transient private val cookie: Cookie?) :
    Serializable {
    var name: String?
    var domain: String?

    @Transient
    private var clientCookie: Cookie? = null
    fun getCookie(): Cookie? {
        var bestCookie = cookie
        if (clientCookie != null) {
            bestCookie = clientCookie
        }
        return bestCookie
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()
        out.writeObject(cookie!!.name())
        out.writeObject(cookie.value())
        out.writeLong(cookie.expiresAt())
        out.writeObject(cookie.domain())
        out.writeObject(cookie.path())
        out.writeBoolean(cookie.secure())
        out.writeBoolean(cookie.httpOnly())
        out.writeBoolean(cookie.hostOnly())
        out.writeBoolean(cookie.persistent())
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        `in`.defaultReadObject()
        val name = `in`.readObject() as String
        val value = `in`.readObject() as String
        val expiresAt = `in`.readLong()
        val domain = `in`.readObject() as String
        val path = `in`.readObject() as String
        val secure = `in`.readBoolean()
        val httpOnly = `in`.readBoolean()
        val hostOnly = `in`.readBoolean()
        val persistent = `in`.readBoolean()
        var builder = Cookie.Builder()
        builder = builder.name(name)
        builder = builder.value(value)
        builder = builder.expiresAt(expiresAt)
        builder = if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        builder = builder.path(path)
        builder = if (secure) builder.secure() else builder
        builder = if (httpOnly) builder.httpOnly() else builder
        clientCookie = builder.build()
    }

    /**
     * host, name, domain 标识一个cookie是否唯一
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as SerializableCookie
        if (if (host != null) host != that.host else that.host != null) {
            return false
        }
        if (if (name != null) name != that.name else that.name != null) {
            return false
        }
        return if (domain != null) domain == that.domain else that.domain == null
    }

    override fun hashCode(): Int {
        var result = if (host != null) host.hashCode() else 0
        result = 31 * result + if (name != null) name.hashCode() else 0
        result = 31 * result + if (domain != null) domain.hashCode() else 0
        return result
    }

    companion object {
        private const val serialVersionUID = 6374381323722046732L
        const val HOST = "host"
        const val NAME = "name"
        const val DOMAIN = "domain"
        const val COOKIE = "cookie"
        fun parseCursorToBean(cursor: Cursor): SerializableCookie {
            val host = cursor.getString(cursor.getColumnIndex(HOST))
            val cookieBytes = cursor.getBlob(cursor.getColumnIndex(COOKIE))
            val cookie = bytesToCookie(cookieBytes)
            return SerializableCookie(host, cookie)
        }

        fun getContentValues(serializableCookie: SerializableCookie): ContentValues {
            val values = ContentValues()
            values.put(HOST, serializableCookie.host)
            values.put(NAME, serializableCookie.name)
            values.put(DOMAIN, serializableCookie.domain)
            values.put(
                COOKIE,
                cookieToBytes(serializableCookie.host, serializableCookie.getCookie())
            )
            return values
        }

        /**
         * cookies 序列化成 string
         *
         * @param cookie 要序列化
         * @return 序列化之后的string
         */
        @JvmStatic
        fun encodeCookie(host: String?, cookie: Cookie?): String? {
            if (cookie == null) {
                return null
            }
            val cookieBytes = cookieToBytes(host, cookie)
            return byteArrayToHexString(cookieBytes)
        }

        fun cookieToBytes(host: String?, cookie: Cookie?): ByteArray? {
            val serializableCookie = SerializableCookie(host, cookie)
            val os = ByteArrayOutputStream()
            try {
                val outputStream = ObjectOutputStream(os)
                outputStream.writeObject(serializableCookie)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
            return os.toByteArray()
        }

        /**
         * 将字符串反序列化成cookies
         *
         * @param cookieString cookies string
         * @return cookie object
         */
        @JvmStatic
        fun decodeCookie(cookieString: String): Cookie? {
            val bytes = hexStringToByteArray(cookieString)
            return bytesToCookie(bytes)
        }

        fun bytesToCookie(bytes: ByteArray?): Cookie? {
            val byteArrayInputStream = ByteArrayInputStream(bytes)
            var cookie: Cookie? = null
            try {
                val objectInputStream = ObjectInputStream(byteArrayInputStream)
                cookie = (objectInputStream.readObject() as SerializableCookie).getCookie()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return cookie
        }

        /**
         * 二进制数组转十六进制字符串
         *
         * @param bytes byte array to be converted
         * @return string containing hex values
         */
        private fun byteArrayToHexString(bytes: ByteArray?): String {
            val sb = StringBuilder(bytes!!.size * 2)
            for (element in bytes) {
                val v: Int = (element.and(0xff.toByte())).toInt()
                if (v < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(v))
            }
            return sb.toString().toUpperCase(Locale.US)
        }

        /**
         * 十六进制字符串转二进制数组
         *
         * @param hexString string of hex-encoded values
         * @return decoded byte array
         */
        private fun hexStringToByteArray(hexString: String): ByteArray {
            val len = hexString.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(
                    hexString[i],
                    16
                ) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }
    }

    init {
        name = cookie!!.name()
        domain = cookie.domain()
    }
}