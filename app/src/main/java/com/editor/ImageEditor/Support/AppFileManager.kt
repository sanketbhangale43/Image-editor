package com.editor.ImageEditor.Support

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URLConnection
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AppFileManager(private val context: Context) {

    // Create an unique file name
    @SuppressLint("SimpleDateFormat")
    fun getNewFileName(): String{
        val date = Calendar.getInstance().time
        val dateFormat: DateFormat = SimpleDateFormat("hh_mm_ss")
        return "Image_" + dateFormat.format(date) + ".jpg"
    }

    // It returns the path of a folder to save images
    private fun getDefaultFolderPath(): File {
        return File(context.getExternalFilesDir(null).toString() + "/Image Editor")
    }

    // It returns the path of a folder to save captured images
    fun getCapturedImagesFolderPath(): File {
        return File(getDefaultFolderPath().toString() + "/Captured Images")
    }

    // It returns the path of a folder to save edited images
    fun getEditedImagesFolderPath(): File {
        return File(getDefaultFolderPath().toString() + "/Edited Images")
    }

    // It creates folders if are already not created
    fun createFolders(){
        val file: File = getDefaultFolderPath()
        if (!file.exists()) { file.mkdirs() }
        val file2: File = getEditedImagesFolderPath()
        if (!file2.exists()) { file2.mkdirs() }
        val file3: File = getCapturedImagesFolderPath()
        if (!file3.exists()) { file3.mkdirs() }
    }

    // It returns an array of captured images
    fun getAllCapturedImages(): Array<File>{
        var data = getCapturedImagesFolderPath().listFiles()
        if (data !=null){
            data = data.reversed().toTypedArray()
        } else{
            data = arrayOf<File>()
        }
        return data
    }

    // It returns an array of edited images
    fun getAllEditedImages(): Array<File> {
        var data = getEditedImagesFolderPath().listFiles()
        if (data !=null){
            data = data.reversed().toTypedArray()
        } else{
            data = arrayOf<File>()
        }
        return data
    }

    fun getContentUriFromPath(file: File): Uri{
        return FileProvider.getUriForFile(context, context.applicationContext.packageName.toString() + ".provider", file)
    }

    // Share the files with other apps
    fun shareFile(file: File) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        var fileURI: Uri? = null
        fileURI = getContentUriFromPath(file)
        println(fileURI)
        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileURI);
        intentShareFile.setDataAndType(fileURI, URLConnection.guessContentTypeFromName(file.name))
        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intentShareFile, "Share File"))
    }

    // Convert bitmap to png and save
    fun saveBitMapFile(bitmap: Bitmap, pathToStore: String){
        // Create a file to write bitmap data
        val file = File(pathToStore)

        return try {
            file.createNewFile()

            // Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapdata = bos.toByteArray()

            // Write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}