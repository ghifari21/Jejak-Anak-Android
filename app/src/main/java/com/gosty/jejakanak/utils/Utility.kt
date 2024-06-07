package com.gosty.jejakanak.utils

import android.text.format.DateUtils

fun String.splitName(): Pair<String?, String?> {
    val names = this.trim().split(Regex("\\s+"))
    if (names.size == 1) {
        return names.firstOrNull() to ""
    } else if (names.size >= 3) {
        var firstName = ""
        val firstNameSize = names.size - 1;
        names.forEachIndexed { index, firstNameSplit ->
            if (index < firstNameSize) {
                firstName = "$firstName $firstNameSplit"
            }
        }
        return firstName to names.lastOrNull()
    }
    return names.firstOrNull() to names.lastOrNull()
}

fun Long.toDateTime(): String {
    return DateUtils.getRelativeTimeSpanString(this).toString()
}

fun getRandomString(): String {
    val allowedChars = ('0'..'9') + ('A'..'Z') + ('a'..'z')
    return (1..5)
        .map { allowedChars.random() }
        .joinToString("")
}

fun Float.toKm(): Float = this / 1000
