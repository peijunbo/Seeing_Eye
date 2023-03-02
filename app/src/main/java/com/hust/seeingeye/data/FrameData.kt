package com.hust.seeingeye.data

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import com.hust.seeingeye.YoloV5Ncnn
import java.nio.ByteOrder

class FrameData(
    val frame: Frame,
    val cameraImage: Image,
    val depthImage: Image?
) {
    companion object {
        private const val TAG = "FrameData"
        const val NO_DEPTH_IMAGE = -1
    }

    var used = false
    val cameraHeight
        get() = cameraImage.height
    val cameraWidth
        get() = cameraImage.width
    val depthHeight
        get() = depthImage?.height
    val depthWidth
        get() = depthImage?.width

    var objects: Array<YoloV5Ncnn.Obj> = arrayOf()
    lateinit var bitmap: Bitmap


    /** Obtain the depth in millimeters for depthImage at coordinates ([x], [y]). */
    private fun getMillimetersDepth(x: Int, y: Int): Int {
        if (depthImage == null) return -1
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.
        val plane = depthImage.planes[0]
        val byteIndex = x * plane.pixelStride + y * plane.rowStride
        val buffer = plane.buffer.order(ByteOrder.nativeOrder())
        val depthSample = buffer.getShort(byteIndex)
        return depthSample.toInt()
    }

    fun getCameraDepth(x: Int, y: Int): Int {
        if (depthImage == null) return -1
        val cpuCoordinates = floatArrayOf(x.toFloat(), y.toFloat())
        val textureCoordinates = FloatArray(2)
        frame.transformCoordinates2d(
            Coordinates2d.IMAGE_PIXELS,
            cpuCoordinates,
            Coordinates2d.TEXTURE_NORMALIZED,
            textureCoordinates
        )
        if (textureCoordinates[0] < 0 || textureCoordinates[1] < 0) {
            // There are no valid depth coordinates, because the coordinates in the CPU image are in the
            // cropped area of the depth image.
            return -1
        }
        val depthCoordinates = (textureCoordinates[0] * depthImage.width).toInt() to
                (textureCoordinates[1] * depthImage.height).toInt()
        Log.d(
            TAG, "getCameraDepth: camera_x${x}," +
                    "camera_y${y}," +
                    "depth_x${depthCoordinates.first}," +
                    "depth_y${depthCoordinates.second}"
        )
        return getMillimetersDepth(depthCoordinates.first, depthCoordinates.second)
    }

    fun getPixelIntensity(): Double {
        val rgb = frame.lightEstimate.environmentalHdrMainLightIntensity
        Log.d(TAG, "getPixelIntensity: ${rgb.size}")
        val y = 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]
        return y
    }

    fun close() {
        cameraImage.close()
        depthImage?.close()
    }
}
