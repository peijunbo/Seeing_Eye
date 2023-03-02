package com.hust.seeingeye.data

import android.graphics.Bitmap
import com.hust.seeingeye.YoloV5Ncnn

class DetectionResult(
    val bitmap: Bitmap,
    objects: List<DetectionObj>
) {
}

class DetectionObj(
    obj: YoloV5Ncnn.Obj,
    val depth: Int?,
) {
    val x = obj.x
    val y = obj.y
    val w = obj.w
    val h = obj.h
    val label: String = obj.label
}