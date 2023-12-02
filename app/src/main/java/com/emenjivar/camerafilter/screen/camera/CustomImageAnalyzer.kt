package com.emenjivar.camerafilter.screen.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class CustomImageAnalyzer(
    private val onDrawImage: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private var mat = Mat()

    override fun analyze(image: ImageProxy) {
        // Convert cameraX image to openCV mat
        val bitmap = image.image?.toBitmap()
        Utils.bitmapToMat(bitmap, mat)

        // Rotate the image 45 degrees
        val centerX = mat.width() / 2.0
        val centerY = mat.height() / 2.0
        val rotation = Imgproc.getRotationMatrix2D(Point(centerX, centerY), -90.0, 1.0)
        Imgproc.warpAffine(
            mat,
            mat,
            rotation,
            Size(mat.width().toDouble(), mat.height().toDouble())
        )

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)

        // Put the mat result on the original bitmap
        Utils.matToBitmap(mat, bitmap)
        image.close()

        bitmap?.let { safeGrayBitmap ->
            onDrawImage(safeGrayBitmap)
        }
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // YU
        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)
        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
