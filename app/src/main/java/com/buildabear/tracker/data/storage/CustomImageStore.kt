package com.buildabear.tracker.data.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val imageDir: File
        get() = File(context.filesDir, "custom-images").also { it.mkdirs() }

    fun imageFileFor(bearId: String): File = File(imageDir, "$bearId.jpg")

    fun saveFromUri(bearId: String, uri: Uri): String {
        val dest = imageFileFor(bearId)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to read image from URI")
        return dest.absolutePath
    }

    fun saveFromFile(bearId: String, source: File): String {
        val dest = imageFileFor(bearId)
        source.inputStream().use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        return dest.absolutePath
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    fun createCameraTempFile(): File {
        val cacheDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        return File.createTempFile("capture_", ".jpg", cacheDir)
    }
}
