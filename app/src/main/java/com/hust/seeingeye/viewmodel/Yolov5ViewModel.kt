package com.hust.seeingeye.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hust.seeingeye.Utils.ImageUtil
import com.hust.seeingeye.YoloV5Ncnn
import com.hust.seeingeye.data.FrameData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Yolov5ViewModel : ViewModel() {
    companion object {
        private const val TAG = "Yolov5ViewModel"
    }

    private val yolo = YoloV5Ncnn() // yolov5模型对象

    private var latestFrameData: FrameData? = null // 最近的一帧画面

    val uiState = MutableStateFlow<Bitmap?>(null)

    fun initYolov5(activity: Activity) {
        yolo.Init(activity.assets)
    }

    fun updateFrame(data: FrameData) {
        val frameData = latestFrameData
        if (frameData != null && !frameData.used) {
            latestFrameData?.close()
        }
        latestFrameData = data
    }

    fun startDetect() {
        viewModelScope.launch {
            while (true) {
                val frameData = latestFrameData
                if (frameData != null) {
                    frameData.used = true
                    detect(frameData)
                    if (frameData.objects.isNotEmpty()) {
                        val bitmap = getResultBitmap(frameData)
                        if (bitmap != null) {
                            uiState.value = bitmap
                        }
                    }
                }
                frameData?.close()
                delay(2000) // 等待2s
            }
        }
    }

    fun getResultBitmap(data: FrameData): Bitmap? {
        val bitmap = data.bitmap?.copy(Bitmap.Config.ARGB_8888, true)

        if (bitmap != null) {
            // 创建一个颜色列表，来绘图
            val colors = intArrayOf(
                Color.rgb(54, 67, 244),
                Color.rgb(99, 30, 233),
                Color.rgb(176, 39, 156),
                Color.rgb(183, 58, 103),
                Color.rgb(181, 81, 63),
                Color.rgb(243, 150, 33),
                Color.rgb(244, 169, 3),
                Color.rgb(212, 188, 0),
                Color.rgb(136, 150, 0),
                Color.rgb(80, 175, 76),
                Color.rgb(74, 195, 139),
                Color.rgb(57, 220, 205),
                Color.rgb(59, 235, 255),
                Color.rgb(7, 193, 255),
                Color.rgb(0, 152, 255),
                Color.rgb(34, 87, 255),
                Color.rgb(72, 85, 121),
                Color.rgb(158, 158, 158),
                Color.rgb(139, 125, 96)
            )
            val canvas = Canvas(bitmap)
            val paint = Paint() // 绘制检测框
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            val textPaint = Paint() // 给检测框绘制标签
            val textBgPaint = Paint() // 为标签添加背景
            textPaint.color = Color.BLACK
            textPaint.textSize = 16f
            textPaint.textAlign = Paint.Align.LEFT
            textBgPaint.color = Color.WHITE
            textBgPaint.style = Paint.Style.FILL

            data.objects.forEachIndexed { i, obj ->
                paint.color = colors[i % colors.size]
                canvas.drawRect(obj.x, obj.y, obj.x + obj.w, obj.y + obj.h, paint)

                // 绘制标签
                val distance =
                    data.getCameraDepth((obj.x + obj.w / 2).toInt(), (obj.y + obj.h / 2).toInt())
                val text = "${obj.label} dis:$distance"
                val textWidth = textPaint.measureText(text)
                val textHeight = -textPaint.ascent() + textPaint.descent()
                val x = if (obj.x + textWidth <= bitmap.width) obj.x else {
                    bitmap.width - textWidth
                }
                val y = if (obj.y - textHeight >= 0) {
                    obj.y - textHeight
                } else 0f

                canvas.drawRect(x, y, x + textWidth, y + textHeight, textBgPaint)
                canvas.drawText(text, x, y - textPaint.ascent(), textPaint)

            }
            return bitmap
        }
        return null
    }

    /**
     * 识别图片中的物体，会先旋转以及转换色彩空间，结果保存在[data]的objects属性中
     */
    fun detect(data: FrameData) {
        val data68 = ImageUtil.getBytesFromImageAsType(data.cameraImage, 2)
        val rgb = ImageUtil.decodeYUV420SP(data68, data.cameraWidth, data.cameraHeight)
        val bitmap =
            Bitmap.createBitmap(rgb, data.cameraWidth, data.cameraHeight, Bitmap.Config.ARGB_8888)
        // 旋转从640*480到480*640
        val matrix = Matrix();
        matrix.setRotate(90f, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        val verticalBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        // TODO 放大至填充

        //识别 使用gpu
        data.bitmap = verticalBitmap
        data.objects = yolo.Detect(verticalBitmap, true)

    }

}