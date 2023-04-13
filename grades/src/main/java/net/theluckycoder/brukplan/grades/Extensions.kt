package net.theluckycoder.brukplan.grades

fun Float.round2Decimals(): Float {
    return (this * 100).toLong().toFloat() / 100
}