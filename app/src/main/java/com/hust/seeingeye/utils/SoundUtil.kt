package com.hust.seeingeye.utils

import android.util.Log
import com.hust.seeingeye.R
import com.hust.seeingeye.data.DetectionResult

class SoundUtil {
    companion object {
        private const val TAG = "SoundUtil"
        val soundRawMap = mapOf<String, Int>(
            "about" to R.raw.about ,
            "bottle" to R.raw.bottle,
            "book" to R.raw.book,
            "bowl" to R.raw.bowl,
            "cell_phone" to R.raw.cell_phone,
            "chair" to R.raw.chair,
            "cm" to R.raw.cm,
            "cup" to R.raw.cup,
            "far" to R.raw.far,
            "num1" to R.raw.num1,
            "num2" to R.raw.num2,
            "num3" to R.raw.num3,
            "num4" to R.raw.num4,
            "num5" to R.raw.num5,
            "num6" to R.raw.num6,
            "num7" to R.raw.num7,
            "num8" to R.raw.num8,
            "num9" to R.raw.num9,
            "num10" to R.raw.num10,
            "num20" to R.raw.num20,
            "num30" to R.raw.num30,
            "num40" to R.raw.num40,
            "num50" to R.raw.num50,
            "num60" to R.raw.num60,
            "num70" to R.raw.num70,
            "num80" to R.raw.num80,
            "num90" to R.raw.num90,
            "remote" to R.raw.remote,
            "tv" to R.raw.tv
        )

        const val DEPTH_TOO_FAR = 1000
        const val SOUND_NOT_FOUND = -1

        fun genRawIdsFromResult(result: DetectionResult, target: String): List<Int>{
            val ls = mutableListOf<Int>()
            val obj = result.objects.firstOrNull {
                it.label == target
            } ?: return listOf()
            Log.d(TAG, "genRawIdsFromResult: $target")
            if (!soundRawMap.containsKey(target)) {
                return listOf()
            } else {
                ls.add(soundRawMap[target] ?: SOUND_NOT_FOUND)
            }
            if (obj.depth != null) {
                Log.d(TAG, "genRawIdsFromResult: depth ${obj.depth}")
                if (obj.depth < DEPTH_TOO_FAR) {
                    ls.add(soundRawMap["about"] ?: SOUND_NOT_FOUND)
                    val x = obj.depth;
                    val n1 = x / 100;
                    val n2 = (x / 10) % 10
                    if (n1 != 0) {
                        ls.add(soundRawMap["num${n1}0"] ?: SOUND_NOT_FOUND)
                    }
                    ls.add(soundRawMap["num$n2"] ?: SOUND_NOT_FOUND)
                    ls.add(soundRawMap["cm"] ?: SOUND_NOT_FOUND)
                }
                else {
                    ls.add(soundRawMap["far"] ?: SOUND_NOT_FOUND)
                }
            }

            return ls
        }
    }


}