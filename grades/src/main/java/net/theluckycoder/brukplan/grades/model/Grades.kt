package net.theluckycoder.brukplan.grades.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import net.theluckycoder.brukplan.grades.roundDecimals
import kotlin.math.roundToInt

data class Grades(
    @ColumnInfo(name = "grades")
    val grades: List<Int>,
    @ColumnInfo(name = "semesterPaper")
    val semesterPaper: Int = 0
) {

    @Ignore
    val average: Int

    init {
        var avg = 0f
        grades.forEach {
            avg += it
        }
        avg = (avg / grades.count { it != 0 }).roundDecimals()

        if (semesterPaper != 0) {
            avg = if (avg != 0f)
                ((avg * 3 + semesterPaper) / 4).roundDecimals()
            else semesterPaper.toFloat()
        }

        if (avg.isNaN())
            avg = 0f
        average = avg.roundToInt()
    }
}
