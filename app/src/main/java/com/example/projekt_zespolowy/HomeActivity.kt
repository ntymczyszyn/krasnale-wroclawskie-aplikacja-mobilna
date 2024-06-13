package com.example.projekt_zespolowy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.TopAppBar
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projekt_zespolowy.ui.NavigationItem
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.data.DwarfsDatabase
import com.example.projekt_zespolowy.ui.theme.Light_Purple
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
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
import android.graphics.Bitmap
import android.util.Base64
import android.widget.ProgressBar
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import java.io.ByteArrayOutputStream
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
    }
}
class HomeActivity : ComponentActivity() {
    //Android Client ID - 492039840169-k15asq5q9pi8p1n8et6gvsg0tbo9j01o.apps.googleusercontent.com
    //Web Client ID - 492039840169-dco3kmv75kkr4djr4ua4fnu6a5g13beh.apps.googleusercontent.com
    //Web Secret ID - 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
    var imageUri = mutableStateOf<Uri?>(null)
    lateinit var file: File
    // *TODO* Check where it should be place when changed to false
    val responseInfo = mutableStateOf(false)
    var messageDwarf = mutableStateOf<String?>("")
    companion object {
        lateinit var database: DwarfsDatabase
        private const val REQUEST_CAMERA_PERMISSION = 123
    }

    lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(
            applicationContext, DwarfsDatabase::class.java, "dwarfs_database")
            //.fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2)
            .build()

        val progressBar = ProgressBar(this)
        val builder = AlertDialog.Builder(this).apply {
            setTitle("Proszę czekać rozpoznajemy twojego krasnala...")
            setCancelable(false)
            setView(progressBar)
        }
        progressDialog = builder.create()

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

    suspend fun uploadImage(uri: Uri) = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this@HomeActivity, "No internet connection", Toast.LENGTH_LONG).show()
            return@withContext
        }

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)

        if (bytes.isEmpty()) {
            Log.d("UPLOAD_ERROR", "Image file is empty")
            return@withContext
        }

        val encodedFile = Base64.encodeToString(bytes, Base64.DEFAULT)

        val API_KEY = "9zbTgwMWBvZgonLi9Jla"
        val MODEL_ENDPOINT = "krasnele_wro-g63lz/2"
        val uploadURL = "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY + "&name=YOUR_IMAGE.jpg";

        var connection: HttpURLConnection? = null
        try {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }

            val url = URL(uploadURL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Content-Length", Integer.toString(encodedFile.toByteArray().size))
            connection.setRequestProperty("Content-Language", "en-US")
            connection.useCaches = false
            connection.doOutput = true

            //Send request
            val wr = DataOutputStream(connection.outputStream)
            wr.writeBytes(encodedFile)
            wr.close()

            // Get Response
            val stream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(stream))
            var line: String?
            val responseStringBuilder = StringBuilder()
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }
            reader.close()

            val jsonResponse = JSONObject(responseStringBuilder.toString())
            val predictions = jsonResponse.getJSONArray("predictions")
            messageDwarf.value = predictions.getJSONObject(0).getString("class")

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
            }

            Log.d("TEST_ROBOFLOW", "Class: ${ messageDwarf.value}")

            saveToDatabase(message = messageDwarf.value)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity,  messageDwarf.value, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun saveToDatabase(message: String?){
        val name = message
        val uploadedDwarf = name?.let { Dwarfs(name = it, date_stamp = Date(), count = 1) }
        try {
            uploadedDwarf?.let {
                if(database.dwarfs().dwarfExists(it.name)){
                    database.dwarfs().updateDwarfCount(it.name)
                    return
                }
                else{
                    database.dwarfs().insert(it)
                }
            }
        } catch (e: Exception) {
            Log.e("Database Insertion", "Error inserting dwarf", e)
        }
    }

    fun getDwarfsList(): Flow<List<Dwarfs>> {
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Light_Purple)
                ) {
                    Text("Zrób zdjęcie")
                }

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        activity.responseInfo.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Light_Purple)
                ) {
                    Text("Wybierz z galerii")
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
                                        activity.messageDwarf.value = ""
                                        activity.responseInfo.value = true
                                        CoroutineScope(Dispatchers.IO).launch {
                                            activity.uploadImage(activity.imageUri.value!!)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Light_Purple)
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
fun DropDownPanel(activity: HomeActivity) {
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
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(color = Dark_Purple)
                .padding(16.dp)
                .wrapContentSize(Alignment.Center)
        ) {
            DropdownMenuItem(
                text = { Text(text ="WYLOGUJ", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    val navigate = Intent(activity, MainActivity::class.java)
                    activity.startActivity(navigate)
                }
            )

        }
    }
}


