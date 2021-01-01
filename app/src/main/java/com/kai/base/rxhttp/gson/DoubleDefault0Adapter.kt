package com.kai.base.rxhttp.gson

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Created by Allen on 2017/11/20.
 *
 *
 * 定义为double类型,如果后台返回""或者null,则返回0.00
 */
class DoubleDefault0Adapter : JsonSerializer<Double?>, JsonDeserializer<Double> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Double {
        try {
            if (json.asString == "" || json.asString == "null") {
                return 0.00
            }
        } catch (ignore: Exception) {
        }
        return try {
            json.asDouble
        } catch (e: NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    override fun serialize(
        src: Double?,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src)
    }
}