package com.campuswave.app.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtil {
    fun getFileFromUri(context: Context, uri: Uri): File? {
        val fileName = getFileName(context, uri)
        val file = File(context.cacheDir, fileName)
        
        try {
            val outputStream = FileOutputStream(file)
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
            inputStream?.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "temp_file"
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            if (nameIndex != -1) {
                name = returnCursor.getString(nameIndex)
            }
            returnCursor.close()
        }
        return name
    }
}
