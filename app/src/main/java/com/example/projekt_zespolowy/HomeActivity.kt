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
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.data.DwarfsDatabase
import com.example.projekt_zespolowy.ui.theme.Light_Purple
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
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
    var imageUri = mutableStateOf<Uri?>(null)
    lateinit var file: File
    // *TODO* Check where it should be place when changed to false
    val responseInfo = mutableStateOf(false)
    var messageDwarf = mutableStateOf<String?>("")
    companion object {
        lateinit var database: DwarfsDatabase
        private const val REQUEST_CAMERA_PERMISSION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // *TODO* Do I need to migrate somehow here?
        database = Room.databaseBuilder(
            applicationContext, DwarfsDatabase::class.java, "dwarfs_database")
            .build()

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
                messageDwarf.value = json?.getString("message")
                Log.d("TEST_WYSYLANIA", "Upload successful: ${messageDwarf.value}")

                saveToDatabase(message = messageDwarf.value)

//                runOnUiThread {
//                    Toast.makeText(this@HomeActivity, messageDwarf.value, Toast.LENGTH_LONG).show()
//                }
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun saveToDatabase(message: String?){
//        // Parse the JSON string
//        val jsonObject = message?.let { JSONObject(it) }
//        val name = jsonObject?.getString("name")

        val name = message
        val uploadedDwarf = name?.let { Dwarfs(name = it, date_stamp = Date(), count = 1) }
        try {
            uploadedDwarf?.let {
                database.dwarfs().insert(it)
                runOnUiThread{
                    Toast.makeText(this, "Saved to database", Toast.LENGTH_SHORT).show()
                }
                messageDwarf.value +=  "Added to database"
            }
        } catch (e: Exception) {
            Log.e("Database Insertion", "Error inserting dwarf", e)
        }

    }

    fun getDwarfsList(): LiveData<List<Dwarfs>> {
        return database.dwarfs().getAllByName()
    }

}

@SuppressLint("SimpleDateFormat")
@Composable
fun HomeScreen(activity: HomeActivity) {
    activity.file = activity.createImageFile()

    val uri = FileProvider.getUriForFile(
        activity.applicationContext,
        "com.app.id.fileProvider", activity.file
    )

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            activity.imageUri.value = uri
            activity.saveImageToGallery(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { activity.imageUri.value = it}
    }

    Box(modifier = Modifier
        .background(color = Light_Purple.copy(0.1f))
        .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                Button(
                    onClick = {
                        if (activity.hasCameraPermission()) {
                            cameraLauncher.launch(uri)
                            activity.responseInfo.value = false
                        } else {
                            activity.requestCameraPermission()
                        }
                    }
                ) {
                    Text("Take a picture")
                }

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        activity.responseInfo.value = false
                    }
                ) {
                    Text("Choose from gallery")
                }
            }

            if (activity.imageUri.value != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(color = Color(R.color.Light_Red)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    item{
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            verticalAlignment = Alignment.Top
                        ) {
                            decodeBitmap(activity, activity.imageUri.value!!)?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                    if(!activity.responseInfo.value) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        activity.imageUri.value?.let { uri ->
                                            activity.responseInfo.value = true
                                            activity.uploadImage(uri)
                                        }
                                    }
                                ) {
                                    Text("Upload image")
                                }
                            }
                        }
                    }
                    else{
                        item{
                            Row(modifier = Modifier.wrapContentSize(),
                                horizontalArrangement = Arrangement.Center
                            ){
                                Text(activity.messageDwarf.value.toString())
                            }
                            // *TODO* when response returned is corect uncoment this
//                val jsonObject = activity.messageDwarf?.let { JSONObject(it) }
//                val name = jsonObject?.getString("name")
//                val location = jsonObject?.getString("location")
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    if (name != null) {
//                        Text(name, modifier = Modifier.padding(16.dp))
//                    }
//                }
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    if (location != null) {
//                        // *TODO* Check if it is multiline and if not change this
//                        Text(location, modifier = Modifier.padding(16.dp).wrapContentSize())
//                    }
//                }
                        }
                    }
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

@Composable
fun DropDownPanel(activity: HomeActivity,
                  cameraLauncher:  ManagedActivityResultLauncher<Uri, Boolean>,
                  galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
                  uri: Uri) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    )  {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(16.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(R.color.Light_Red))
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "..."
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Take Photo") },
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
            )
            DropdownMenuItem(
                text = { Text("From Gallery") },
                onClick = { galleryLauncher.launch("image/*") }
            )
        }
    }
}


