package net.theluckycoder.brukplan.grades.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import net.theluckycoder.brukplan.grades.round2Decimals
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
        var avg = grades.sum().toFloat()
        avg = (avg / grades.count { it != 0 }).round2Decimals()

        if (semesterPaper != 0) {
            avg = if (avg != 0f)
                ((avg * 3 + semesterPaper) / 4).round2Decimals()
            else semesterPaper.toFloat()
        }

        average = if (avg.isNaN())
            0
        else
            avg.roundToInt()
    }
}
