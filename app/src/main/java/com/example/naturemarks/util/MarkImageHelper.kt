package com.example.naturemarks.util

import com.example.naturemarks.R

object MarkImageHelper {

    fun getMarkImage(resName: String): Int =
        when (resName) {
            "tu_sofia" -> R.drawable.tu_sofia
            "forest" -> R.drawable.forest_mark
            "seven_lakes" -> R.drawable.seven_lakes_mark
            "pirin_vihren" -> R.drawable.pirin_vihren_mark
            "iskar_panega" -> R.drawable.iskar_panega_mark
            "boyana_waterfall" -> R.drawable.boyana_waterfall_mark
            else -> 0
        }

}