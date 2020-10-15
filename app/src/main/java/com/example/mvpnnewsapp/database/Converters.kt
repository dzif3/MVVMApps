package com.example.mvpnnewsapp.database

import androidx.room.TypeConverter
import com.example.mvpnnewsapp.model.Source

class Converters {
    @TypeConverter
    fun fromSource(source: Source): String{
        return source.name
    }
    @TypeConverter
    fun toSource(name: String): Source{
        return Source(name, name)
    }
}