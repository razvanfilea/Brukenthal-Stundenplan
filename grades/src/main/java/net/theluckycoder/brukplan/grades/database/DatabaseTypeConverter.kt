package net.theluckycoder.brukplan.grades.database

import androidx.room.TypeConverter

internal object DatabaseTypeConverter {

    @TypeConverter
    @JvmStatic
    fun fromIntList(list: List<Int>): String {
        return buildString {

            if (list.isNotEmpty()) {
                list.forEach {
                    append(it).append(';')
                }

                deleteCharAt(length - 1)
            }
        }
    }

    @TypeConverter
    @JvmStatic
    fun toIntList(str: String): List<Int> {
        return str.splitToSequence(';')
            .map { it.toIntOrNull() }
            .filterNotNull()
            .toList()
    }
}