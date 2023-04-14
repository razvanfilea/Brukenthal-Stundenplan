package net.theluckycoder.brukplan.grades.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import net.theluckycoder.brukplan.grades.round2Decimals
import kotlin.math.roundToInt

data class Grades(
    @ColumnInfo(name = "grades")
    val grades: List<Int>
) {

    @Ignore
    val average: Int

    init {
        val sum = grades.sum().toFloat()
        val avg = (sum / grades.count { it != 0 }).round2Decimals()

        average = if (avg.isNaN())
            0
        else
            avg.roundToInt()
    }
}
