package com.example.projekt_zespolowy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.source
import org.json.JSONObject
import java.io.IOException
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap


class MainActivity : ComponentActivity() {
    private lateinit var imageUri: Uri
    private lateinit var file: File
    val uploadedImage = mutableStateOf<ImageBitmap?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjektzespolowyTheme {
                MyApp()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun MyApp() {
        val imageUriState = remember { mutableStateOf<Uri?>(null) }
        imageUri = imageUriState.value ?: Uri.EMPTY

        file = createImageFile()

        val uri = FileProvider.getUriForFile(
            applicationContext,
            "com.app.id.fileProvider", file
        )

        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUriState.value = uri
                saveImageToGallery(uri)
            }
        }

        val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUriState.value = it }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUriState.value != null) {
                decodeBitmap(imageUri)?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (hasCameraPermission()) {
                            cameraLauncher.launch(uri)
                        } else {
                            requestCameraPermission()
                        }
                    }
                ) {
                    Text("Take a picture")
                }
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Choose from gallery")
                }
                Button(
                    onClick = {
                        imageUriState.value?.let { uri ->
                            uploadImage(uri)
                        }
                    }
                ) {
                    Text("Upload image")
                }
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val client = OkHttpClient()

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)

        if (bytes.isEmpty()) {
            Log.d("TEST_WYSYLANIA_ERROR", "Image file is empty")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes))
            .build()

        val request = Request.Builder()
            .url("https://krasnalewroclawskie.azurewebsites.net/image/upload")
            .header("Content-Type", requestBody.contentType().toString())
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d("TEST_WYSYLANIA_ERROR", "Upload failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("TEST_WYSYLANIA_ERROR", "Response Code: ${response.code}, Response Message: ${response.message}")
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string()
                val json = responseBody?.let { JSONObject(it) }
                val message = json?.getString("message")
                Log.d("TEST_WYSYLANIA", "Upload successful: $message")

                runOnUiThread {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }

//                val headers = json?.getJSONObject("headers")
//                val contentType = headers?.getString("Content-Type")
//
//                Log.d("TEST_WYSYLANIA", "Content-Type: $contentType")
//                val files = json?.getJSONObject("files")
//                val fileData = files?.getString("file")
//
//                val base64Image = fileData?.substringAfter("data:image/jpeg;base64,")
//                val decodedBytes = base64Image?.let { Base64.decode(it, Base64.DEFAULT) }
//                Log.d("TEST_WYSYLANIA", "Decoded File Data: ${decodedBytes?.size} bytes")
//                val bitmapWorks = decodedBytes?.size?.let { BitmapFactory.decodeByteArray(decodedBytes, 0, it) }
//
//                // Zapisz obraz w galerii
//                val savedImageURL = MediaStore.Images.Media.insertImage(
//                    contentResolver,
//                    bitmapWorks,
//                    "Image Title",
//                    "Image Description"
//                )
            }
        })
    }

    private fun requestBodyToString(request: Request): String {
        val copy = request.newBuilder().build()
        val buffer = okio.Buffer()
        copy.body?.writeTo(buffer)
        val requestBodyString = buffer.readUtf8()

        // Format the request body for logging
        val gson = GsonBuilder().setPrettyPrinting().create()
        return try {
            val json = gson.toJson(JsonParser.parseString(requestBodyString))
            json
        } catch (e: Exception) {
            Log.e("TEST_WYSYLANIA_ERROR", "Error parsing request body: ${e.message}")
            requestBodyString
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timeStamp"
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            externalCacheDir /* directory */
        )
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    private fun saveImageToGallery(uri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
            }
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { imageUri ->
            contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                contentResolver.openInputStream(uri)?.copyTo(outputStream)
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 123
    }

    @Composable
    private fun decodeBitmap(uri: Uri): ImageBitmap? {
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)?.asImageBitmap()
        }
    }
}
