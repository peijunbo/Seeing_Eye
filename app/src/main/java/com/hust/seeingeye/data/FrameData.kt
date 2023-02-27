package com.hust.seeingeye.data

import android.media.Image
import android.util.Log
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteOrder

class FrameData(
    val frame: Frame,
    val cameraImage: Image,
    val depthImage: Image
) {
    companion object {
        private const val TAG = "FrameData"
    }
    /** Obtain the depth in millimeters for depthImage at coordinates ([x], [y]). */
    fun getMillimetersDepth(x: Int, y: Int): Int {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.
        val plane = depthImage.planes[0]
        val byteIndex = x * plane.pixelStride + y * plane.rowStride
        val buffer = plane.buffer.order(ByteOrder.nativeOrder())
        val depthSample = buffer.getShort(byteIndex)
        return depthSample.toInt()
    }

    fun getCameraDepth(x: Int, y: Int): Int {
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
        Log.d(TAG, "getCameraDepth: camera_x${x}," +
                "camera_y${y}," +
                "depth_x${depthCoordinates.first}," +
                "depth_y${depthCoordinates.second}")
        return getMillimetersDepth(depthCoordinates.first, depthCoordinates.second)
    }

    fun close() {
        cameraImage.close()
        depthImage.close()
    }
}
