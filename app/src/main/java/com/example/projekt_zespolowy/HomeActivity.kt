package com.example.projekt_zespolowy

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.data.DwarfsDatabase
import com.example.projekt_zespolowy.ui.NavigationItem
import com.example.projekt_zespolowy.ui.theme.Light_Purple
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class HomeActivity : ComponentActivity() {
    lateinit var imageUri: Uri
    lateinit var file: File
    val uploadedImage = mutableStateOf<ImageBitmap?>(null)
    lateinit var database: DwarfsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = DwarfsDatabase.getDatabase(this)

        setContent {
            ProjektzespolowyTheme {
                MainScreen(this)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timeStamp"
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            externalCacheDir /* directory */
        )
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    fun saveImageToGallery(uri: Uri) {
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

    fun uploadImage(uri: Uri) {
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
                    Toast.makeText(this@HomeActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this@HomeActivity, message, Toast.LENGTH_LONG).show()
                    saveToDatabase(message ?: message.toString(), "Krasnale")
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 123
    }

    fun saveToDatabase(message: String, name: String){
        val uploadDwarfMessage = Dwarfs(Name = name, DateStamp = Date(), Count = 1)
//        lifecycleScope.launch {
//            database.dwarfs().insert(uploadDwarfMessage)
//        }
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun HomeScreen(activity: HomeActivity) {
    val imageUriState = remember { mutableStateOf<Uri?>(null) }
    activity.imageUri = imageUriState.value ?: Uri.EMPTY

    activity.file = activity.createImageFile()

    val uri = FileProvider.getUriForFile(
        activity.applicationContext,
        "com.app.id.fileProvider", activity.file
    )

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUriState.value = uri
            activity.saveImageToGallery(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUriState.value = it }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (activity.hasCameraPermission()) {
                        cameraLauncher.launch(uri)
                    } else {
                        activity.requestCameraPermission()
                        if (activity.hasCameraPermission()) {
                            cameraLauncher.launch(uri)
                        }
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
                        activity.uploadImage(uri)
                    }
                }
            ) {
                Text("Upload image")
            }
            if (imageUriState.value != null) {
                decodeBitmap(activity, activity.imageUri)?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun decodeBitmap(activity: HomeActivity, uri: Uri): ImageBitmap? {
    return activity.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)?.asImageBitmap()
    }
}

