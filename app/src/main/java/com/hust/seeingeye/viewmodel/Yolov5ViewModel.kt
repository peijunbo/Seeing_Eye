package com.hust.seeingeye.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import com.hust.seeingeye.YoloV5Ncnn
import com.hust.seeingeye.data.FrameData
import kotlinx.coroutines.launch

class Yolov5ViewModel : ViewModel() {
    companion object {
        private const val TAG = "Yolov5ViewModel"
    }
    private val yolo = YoloV5Ncnn()

    fun initYolov5(activity: Activity) {
        yolo.Init(activity.assets)
    }

    fun processFrame(data: FrameData) {
        viewModelScope.launch {
            val depth = data.getCameraDepth(data.cameraImage.width / 2, data.cameraImage.height / 2)
            Log.d(TAG, "processFrame: depth$depth")
            data.close()
        }
    }
}