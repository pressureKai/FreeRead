package com.kai.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.kai.common.utils.LogUtils
import okio.*
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.*

class FileHelper {
    companion object {
        const val TAG = "FileHelper"
        const val requestFileCode = 0x1011
        fun getAllSystemFile(activity: FragmentActivity) {
            PermissionHelper.instance.requestPermission(activity, PermissionHelper.writeStoragePermission, onConfirmListener = {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                activity.startActivityForResult(intent, requestFileCode)
            })
        }


        fun copy(uri: Uri, ctx: Context,copySuccessListener: CopySuccessListener) {
            LogUtils.e(TAG,"copy file start")
            var filePath = "${ctx.getExternalFilesDir("Bluetooth")!!.absolutePath}${File.separator}temp${File.separator}"
            var type = ctx.contentResolver.getType(uri)
            var name = ""
            uri.path?.let { path ->
                try {
                    type?.let { s ->
                        type =  FileType.getSuffixFromMineType(s)
                    }
                    val directory = path.lastIndexOf("/")
                    val point = path.lastIndexOf(".")
                    name = if(point>directory){
                        path.substring(directory,point)
                    } else {
                        path.substring(directory,path.length)
                    }
                    if(name.contains(":")){
                        name = name.replace(":","")
                    }
                }catch (e:java.lang.Exception){
                    LogUtils.e(TAG,"on take name error is $e")
                }
                name = name.replace(" ", "").trim()
                name = name.replace("/", "")
            }

            filePath += "${name}.${type}"
            LogUtils.e(TAG,"copy file start file path is $filePath  and name is $name")
            var inBuffer: BufferedSource? = null
            try {
                val outFile = File(filePath)
                if (!outFile.parentFile.exists()) {
                    outFile.parentFile.mkdirs()
                }
                inBuffer = Objects.requireNonNull<InputStream>(
                        ctx.contentResolver.openInputStream(uri)
                ).source().buffer()
                if (bufferCopy(inBuffer, outFile)) {
                    LogUtils.e(TAG,"COPY FILE SUCCESS PATH IS $filePath")
                    copySuccessListener.copySuccess(filePath)
                }
            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG,"COPY FILE FAIL ERROR IS $e")
                e.printStackTrace()
            } finally {
                if (inBuffer != null && inBuffer.isOpen) {
                    close(inBuffer)
                }
            }
        }


        private fun bufferCopy(inBuffer: BufferedSource, outFile: File?): Boolean {
            var outBuffer: BufferedSink? = null
            try {
                outBuffer = outFile?.let { it.sink().buffer() }
                outBuffer?.writeAll(inBuffer)
                outBuffer?.flush()
                return true
            } catch (e: Exception) {
                LogUtils.e(TAG,"BUFFER COPY ERROR IS $e")
                e.printStackTrace()
            } finally {
                close(inBuffer)
                close(outBuffer)
            }
            return false
        }


        private fun close(c: Closeable?) {
            if (c != null && c is Closeable) {
                try {
                    c.close()
                } catch (e: java.lang.Exception) {
                    // silence
                }
            }
        }
    }



    public interface  CopySuccessListener{
        fun copySuccess(path: String)
    }
}