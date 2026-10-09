package com.psk.mybooks.core.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val STRING_LIST_SERIALIZER: KSerializer<List<String>> = ListSerializer(String.serializer())

/** 저자 목록처럼 문자열 목록을 JSON 한 칸에 저장한다. 이름에 쉼표가 있어도 깨지지 않는다. */
internal class StringListConverter {

    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(STRING_LIST_SERIALIZER, value)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(STRING_LIST_SERIALIZER, value)
}
