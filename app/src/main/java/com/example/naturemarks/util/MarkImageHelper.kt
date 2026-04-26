package com.example.naturemarks.util

import com.example.naturemarks.R

object MarkImageHelper {

    fun getMarkImage(resName: String): Int =
        when (resName) {
            "tu_sofia" -> R.drawable.tu_sofia
            "forest" -> R.drawable.forest
            "seven_lakes" -> R.drawable.seven_lakes
            "pirin_vihren" -> R.drawable.pirin_vihren
            "iskar_panega" -> R.drawable.iskar_panega
            "boyana_waterfall" -> R.drawable.boyana_waterfall
            else -> 0
        }

}