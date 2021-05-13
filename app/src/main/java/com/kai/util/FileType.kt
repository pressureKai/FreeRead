package com.kai.util

import android.util.Log
import java.io.FileInputStream
import kotlin.experimental.and

/**
 * @ProjectName: COB
 * @Description:  文件类型判断根据类
 * @Author: 广巨天下
 * @UpdateDate: 2020/9/22 16:51
 */
object FileType {
    private fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder()
        if (src == null || src.isEmpty()) {
            return null
        }
        for (i in src.indices) {
            val v: Int = (src[i].toInt()).and(0xFF)
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    fun getFileType(path: String?): String {
        var fileType = "text"
        try {
            val `is` = FileInputStream(path)
            val b = ByteArray(3)
            `is`.read(b, 0, b.size)
            var originByte = bytesToHexString(b)
            originByte = originByte!!.toUpperCase()
            // Log.e("FileType","头文件是 :"+originByte);
            fileType = TypeDict.checkType(originByte)
            // Log.e("FileType","后缀名是 :"+ fileType);
            `is`.close()
        } catch (e: Exception) {
            Log.e("FileType", e.toString())
        }
        return fileType
    }

    fun getSuffixFromMineType(mineType: String?): String {
        var suffix = "txt"
        when (mineType) {
            "application/msword" -> {
                suffix = "doc"
            }
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                suffix = "docx"
            }
            "application/vnd.ms-powerpoint" -> {
                suffix = "ppt"
            }
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                suffix = "pptx"
            }
            "application/pdf" -> {
                suffix = "pdf"
            }
            "audio/mpeg" -> {
                suffix = "mpeg"
            }
            "image/gif" -> {
                suffix = "gif"
            }
            "image/jpeg" -> {
                suffix = "jpg"
            }
            "image/png" -> {
                suffix = "png"
            }
            "text/plain" -> {
                suffix = "txt"
            }
            "text/xml" -> {
                suffix = "xml"
            }
            "text/html" -> {
                suffix = "html"
            }
            "text/css" -> {
                suffix = "css"
            }
            "text/javascript" -> {
                suffix = "js"
            }
            "image/bmp" -> {
                suffix = "bmp"
            }
            "video/mp4" -> {
                suffix = "mp4"
            }
            "audio/x-wav" -> {
                suffix = "wav"
            }
            "audio/wav" -> {
                suffix = "wav"
            }
            "audio/x-ms-wma" -> {
                suffix = "wma"
            }
            "video/x-ms-wmv" -> {
                suffix = "wmv"
            }
            "application/x-zip-compressed" -> {
                suffix = "zip"
            }
            "audio/flac" -> {
                suffix = "flac"
            }
            "application/rar" -> {
                suffix = "rar"
            }
            "application/zip" -> {
                suffix = "zip"
            }
            "application/vnd.android.package-archive" -> {
                suffix = "apk"
            }
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                suffix = "xlsx"
            }
            "application/vnd.ms-excel" -> {
                suffix = "xls"
            }
            "application/x-compress" -> {
                suffix = "z"
            }
            "application/pgp-keys" -> {
                suffix = "key"
            }
            "application/json" -> {
                suffix = "json"
            }
            "application/x-info" -> {
                suffix = "info"
            }
            "application/octet-stream" -> {
                suffix = ""
            }
            "application/x-msdos-program" -> {
                suffix = "bat"
            }
            else -> {
            }
        }
        return suffix
    }

    object TypeDict {
        fun checkType(fileType: String?): String {
            return when (fileType) {
                "FFD8FF" -> "jpg"
                "89504E" -> "png"
                "474946" -> "jif"
                "D0CF11" -> "doc"
                "255044" -> "pdf"
                "504B03" -> "docx"
                "4D4143" -> "ape"
                "664C61" -> "flac"
                "FFFB90" -> "mpeg"
                "32574D" -> "key"
                "623939" -> "tdck"
                "4A4379" -> "cfg"
                "494433" -> "mp3"
                "7B0A20" -> "json"
                "57776F" -> "info"
                "8147BE" -> "bat"
                else -> "txt"
            }
        }
    }
}