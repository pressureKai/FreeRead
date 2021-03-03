package com.kai.common.rxhttp.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Created by Allen on 2017/11/20.
 */
object GsonAdapter {
    fun buildGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Int::class.java, IntegerDefault0Adapter())
            .registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDefault0Adapter())
            .registerTypeAdapter(Double::class.java, DoubleDefault0Adapter())
            .registerTypeAdapter(Double::class.javaPrimitiveType, DoubleDefault0Adapter())
            .registerTypeAdapter(Long::class.java, LongDefault0Adapter())
            .registerTypeAdapter(Long::class.javaPrimitiveType, LongDefault0Adapter())
            .create()
    }
}