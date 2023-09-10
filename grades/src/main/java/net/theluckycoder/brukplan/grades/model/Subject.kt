package net.theluckycoder.brukplan.grades.model

import androidx.room.*
import net.theluckycoder.brukplan.grades.round2Decimals
import kotlin.math.roundToInt

@Entity(tableName = "subject")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "grades")
    val grades: List<Int>
) {

    @Ignore
    val gradesAverage: Int

    init {
        val sum = grades.sum().toFloat()
        val avg = (sum / grades.count { it != 0 }).round2Decimals()

        gradesAverage = if (avg.isNaN())
            0
        else
            avg.roundToInt()
    }

    @Ignore
    constructor() : this(
        0,
        "",
        emptyList(),
    )

    constructor(name: String) : this(
        0,
        name,
        emptyList(),
    )
}
