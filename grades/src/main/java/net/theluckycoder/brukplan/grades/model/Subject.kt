package net.theluckycoder.brukplan.grades.model

import androidx.room.*

@Entity(tableName = "subject")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @Embedded(prefix = "sem1_")
    val semester1: Grades,
    @Embedded(prefix = "sem2_")
    val semester2: Grades,
) {
    @Ignore
    constructor() : this(
        0,
        "",
        Grades(emptyList()),
        Grades(emptyList())
    )

    operator fun get(semester: Semester) = if (semester == Semester.ONE) semester1 else semester2

    enum class Semester {
        ONE,
        TWO
    }
}
