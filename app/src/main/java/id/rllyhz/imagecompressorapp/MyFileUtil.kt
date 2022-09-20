package id.rllyhz.imagecompressorapp

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object MyFileUtil {
    private fun createImageFile(context: Context, filename: String): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpeg", storageDir)
    }

    fun getFileSizeInMB(filePath: String): Double {
        val file = File(filePath)
        val length = file.length()

        val fileSizeInKB = (length / 1024).toString().toDouble()

        return (fileSizeInKB / 1024).toString().toDouble()
    }

    fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
        context.contentResolver.openInputStream(uri)?.let { inputStream ->
            val tempFile = createImageFile(context, fileName)
            val fileOutputStream = FileOutputStream(tempFile)

            inputStream.copyTo(fileOutputStream)
            inputStream.close()
            fileOutputStream.close()

            return tempFile
        }
        return null
    }
}