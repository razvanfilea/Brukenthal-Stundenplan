package net.theluckycoder.brukplan.grades

fun Float.roundDecimals(): Float {
    return (this * 100).toLong().toFloat() / 100
}