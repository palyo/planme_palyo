package aurora.reminder.todolist.calendar.database.converter

import androidx.room.*
import com.google.gson.*
import com.google.gson.reflect.*

class Converters {
    @TypeConverter
    fun fromIntString(value: String?): MutableList<Int>? {
        val listType = object : TypeToken<MutableList<Int>>() {}.type
        return value?.let { Gson().fromJson(it, listType) }
    }

    @TypeConverter
    fun fromIntList(list: MutableList<Int>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringList(value: String?): MutableList<String>? {
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return value?.let { Gson().fromJson(it, listType) }
    }

    @TypeConverter
    fun fromListString(list: MutableList<String>?): String? {
        return Gson().toJson(list)
    }
}