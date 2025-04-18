package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.exa.android.reflekt.loopit.util.Constants.APP_NAME
import com.exa.android.reflekt.loopit.util.Constants.CAMERA_REQUEST_CODE
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun checkAndOpenOrDownloadMedia(
    context: Context,
    mediaUrl: String,
    mimeType: String,
    onDownloadRequired: (fileName: String) -> Unit
) {
    val fileName = getFileNameFromUrl(mediaUrl)
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val myAppFolder = File(downloadsFolder, APP_NAME)
    val file = File(myAppFolder, fileName)

    if (file.exists()) {
        // File already downloaded, open it
        context.openFileWithIntent(file, mimeType)
    } else {
        // File not found, trigger download
        onDownloadRequired(fileName)
    }
}


fun Context.openFileWithIntent(file: File, mimeType: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider", // Make sure this matches your authority in Manifest
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app found to open this file.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this, "Error opening file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}


fun launchCameraWithPermission(
    activity: Activity,
    onPermissionDenied: () -> Unit,
    onImageUriReady: (Uri) -> Unit,
    launchCamera: (Uri) -> Unit
) {
    val permission = Manifest.permission.CAMERA
    when {
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED -> {
            val imageFile = createImageFile(activity)
            val imageUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.provider",
                imageFile
            )
            onImageUriReady(imageUri)
            launchCamera(imageUri)
        }
        else -> {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                CAMERA_REQUEST_CODE
            )
            onPermissionDenied()
        }
    }
}

fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "JPEG_${timestamp}_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    return File.createTempFile(fileName, ".jpg", storageDir)
}


fun openCamera(context: Context): Uri {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val imageFile = File(context.cacheDir, "temp_image.jpg")
    val photoUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
    (context as Activity).startActivityForResult(intent, CAMERA_REQUEST_CODE)
    return photoUri
}

