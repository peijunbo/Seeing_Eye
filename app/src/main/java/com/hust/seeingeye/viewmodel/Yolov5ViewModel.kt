package com.hust.seeingeye.viewmodel

import android.app.Activity
import android.graphics.*
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hust.seeingeye.SeeingEyeApp
import com.hust.seeingeye.utils.ImageUtil
import com.hust.seeingeye.YoloV5Ncnn
import com.hust.seeingeye.data.DetectionObj
import com.hust.seeingeye.data.DetectionResult
import com.hust.seeingeye.data.FrameData
import com.hust.seeingeye.utils.SoundUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class Yolov5ViewModel : ViewModel() {

    companion object {
        private const val TAG = "Yolov5ViewModel"
    }


    private val yolo = YoloV5Ncnn() // yolov5模型对象
    private var latestFrameData: FrameData? = null // 最近的一帧画面

    private lateinit var soundPool: SoundPool
    private val soundIdMap = hashMapOf<Int, Int>() // rawId to duration
    private val rawToSoundIdMap = hashMapOf<Int, Int>()
    // TODO test
    private val audios = hashMapOf<Int, Int>()
    private var soundIsReady = false
    var targetObjLabel = "bottle" // 记录当前识别对象
    val uiState = MutableStateFlow<Bitmap?>(null)


    /**
     * 加载Yolov5与音频
     */
    fun initYolov5(activity: Activity) {
        yolo.Init(activity.assets)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(SoundUtil.soundRawMap.size)
            .setAudioAttributes(audioAttributes)
            .build()
        // 将所有音频添加到map
        viewModelScope.launch {
            for (rawId in SoundUtil.soundRawMap.values) {
                val soundId = soundPool.load(SeeingEyeApp.context, rawId, 1)
                val uri =
                    Uri.parse("android.resource://" + SeeingEyeApp.context.packageName + "/" + rawId)
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(SeeingEyeApp.context, uri)
                mediaPlayer.setAudioAttributes(audioAttributes)
                mediaPlayer.prepare()
                soundIdMap[soundId] = mediaPlayer.duration
                rawToSoundIdMap[rawId] = soundId
            }
            soundIsReady = true
            Log.d(TAG, "initYolov5: sound is ready")
        }
    }

    fun updateFrame(data: FrameData) {
        val frameData = latestFrameData
        if (frameData == null) {
            startDetection()
        }
        if (frameData != null && !frameData.used) {
            latestFrameData?.close()
        }
        latestFrameData = data
        val y = latestFrameData?.getPixelIntensity()
        Log.d(TAG, "updateFrame: $y")
    }

    fun startDetection() {
        viewModelScope.launch {
            while (true) {
                val frameData = latestFrameData
                if (frameData != null && !frameData.used) {
                    latestFrameData?.used = true
                    frameData.used = true
                    detect(frameData)
                    if (frameData.objects.isNotEmpty() && soundIsReady) {
                        // TODO 修改函数后在这里检查结果并播放语音
                        val result = frameData.getDetectionResult()
                        val rawIds = SoundUtil.genRawIdsFromResult(result, targetObjLabel)
                        playResultSound(rawIds)
                        uiState.value = result.bitmap
                    } else {
                        // TODO 没有检测结果时的提示
                    }
                    frameData.close()
                }
                delay(5000) // 等待2s
            }
        }
    }

    fun playResultSound(rawIds: List<Int>) {
        viewModelScope.launch {
            rawIds.forEach {
                Log.d(TAG, "playResultSound: ${rawIds.size}")
                soundPool.play(rawToSoundIdMap[it] ?: 0, 1f, 1f, 0, 0, 1f)
                delay(soundIdMap[rawToSoundIdMap[it] ?: 0]?.toLong() ?: 0)
            }
        }
    }

    private fun FrameData.getDetectionResult(): DetectionResult {
        val data: FrameData = this
        val bitmap = data.bitmap.copy(Bitmap.Config.ARGB_8888, true)
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

        val objects = mutableListOf<DetectionObj>()

        data.objects.forEachIndexed { i, obj ->
            val distance =
                data.getCameraDepth((obj.x + obj.w / 2).toInt(), (obj.y + obj.h / 2).toInt())
            // 将检测结果保存起来
            objects.add(
                DetectionObj(obj, distance)
            )

            paint.color = colors[i % colors.size]
            canvas.drawRect(obj.x, obj.y, obj.x + obj.w, obj.y + obj.h, paint)

            // 绘制标签
            var text = obj.label
            if (distance != -1) {
                text = "$text dis:$distance"
            }
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
        return DetectionResult(
            bitmap = bitmap,
            objects
        )
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


    // TODO test
    fun audioTest() {
        for (resId in SoundUtil.soundRawMap.values) {
            val soundId = audios[resId]
            if (soundId != null) {
                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
            }
        }
    }

}

private data class Sound(
    val rawId: Int,
    val soundId: Int,
    val duration: Int,
    val label: String
)