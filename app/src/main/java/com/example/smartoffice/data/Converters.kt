package com.example.smartoffice.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String>? {
        val listType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, listType)
    }
}
