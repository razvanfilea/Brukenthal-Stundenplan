package net.theluckycoder.stundenplan

enum class TimetableType {
    HIGH_SCHOOL,
    MIDDLE_SCHOOL;

    fun getUrl() = when (this) {
        HIGH_SCHOOL -> "url_orar_liceu"
        MIDDLE_SCHOOL -> "url_orar_gimnaziu"
    }
}
