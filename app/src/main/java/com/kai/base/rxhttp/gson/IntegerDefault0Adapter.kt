package com.kai.base.rxhttp.gson

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Created by Allen on 2017/11/20.
 *
 *
 * 定义为int类型,如果后台返回""或者null,则返回0
 */
class IntegerDefault0Adapter : JsonSerializer<Int?>, JsonDeserializer<Int> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Int {
        try {
            if (json.asString == "" || json.asString == "null") {
                return 0
            }
        } catch (ignore: Exception) {
        }
        return try {
            json.asInt
        } catch (e: NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    override fun serialize(
        src: Int?,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src)
    }
}