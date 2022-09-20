package id.rllyhz.imagecompressorapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.FileOutputStream

object MyImageCompressor {
    /*
     * targetMB is the size of the result compression
     * targetMB = 1.0 means compressing image size under 1MB
     */
    fun compressImage(filePath: String, targetMB: Double = 1.0): List<Double> {
        var beforeCompression = 0.0
        var afterCompression = 0.0

        var image: Bitmap = BitmapFactory.decodeFile(filePath)

        val exif = ExifInterface(filePath)
        val exifOrientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )

        val exifDegree: Int = exifOrientationToDegree(exifOrientation)

        image = rotateImage(image, exifDegree.toFloat())

        try {
            val fileSizeInMB = MyFileUtil.getFileSizeInMB(filePath)
            beforeCompression = fileSizeInMB

            var quality = 100

            if (fileSizeInMB > targetMB) {
                quality = ((targetMB / fileSizeInMB) * 100).toInt()
            }

            val fileOutputStream = FileOutputStream(filePath)
            image.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
            fileOutputStream.close()

            afterCompression = MyFileUtil.getFileSizeInMB(filePath)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf(beforeCompression, afterCompression)
    }

    /*
     * In some phones, the image got from camera would be rotated 90 degree
     * So this is to fix it
     */
    private fun exifOrientationToDegree(exfOrientation: Int): Int =
        when (exfOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}