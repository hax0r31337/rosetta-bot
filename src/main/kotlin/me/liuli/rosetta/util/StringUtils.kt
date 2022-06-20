package me.liuli.rosetta.util

import java.util.regex.Pattern

private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

fun String.stripColor(): String {
    return COLOR_PATTERN.matcher(this).replaceAll("")
}