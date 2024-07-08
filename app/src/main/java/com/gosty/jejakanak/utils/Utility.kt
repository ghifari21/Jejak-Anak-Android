package com.gosty.jejakanak.utils

import android.app.ActivityManager
import android.content.Context
import android.text.format.DateUtils
import com.kennyc.view.MultiStateView
import java.text.NumberFormat
import java.util.Locale

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
    return (1..6)
        .map { allowedChars.random() }
        .joinToString("")
}

fun Float.toKm(): Float = this / 1000

fun MultiStateView.showLoadingState() {
    this.viewState = MultiStateView.ViewState.LOADING
}

fun MultiStateView.showEmptyState() {
    this.viewState = MultiStateView.ViewState.EMPTY
}

fun MultiStateView.showErrorState() {
    this.viewState = MultiStateView.ViewState.ERROR
}

fun MultiStateView.showContentState() {
    this.viewState = MultiStateView.ViewState.CONTENT
}

fun String.capitalizeEachWord(): String {
    return this.trim().split("\\s+".toRegex()).joinToString(" ") { it.capitalize(Locale.ROOT) }
}

fun Float.formatFloatWithSeparator(): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.ROOT).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    return numberFormat.format(this)
}

fun Int.formatIntWithSeparator(): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.ROOT).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    return numberFormat.format(this)
}

fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}