package com.example.projekt_zespolowy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
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
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import android.graphics.Bitmap
import android.media.ExifInterface
import android.util.Base64
import android.widget.ProgressBar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.io.ByteArrayOutputStream
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
    }
}
class HomeActivity : ComponentActivity() {
    //Android Client ID - 492039840169-k15asq5q9pi8p1n8et6gvsg0tbo9j01o.apps.googleusercontent.com
    //Web Client ID - 492039840169-dco3kmv75kkr4djr4ua4fnu6a5g13beh.apps.googleusercontent.com
    //Web Secret ID - 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
    var imageUri = mutableStateOf<Uri?>(null)
    var imageWidth = mutableStateOf(0)
    var imageHeight = mutableStateOf(0)
    lateinit var file: File
    val responseInfo = mutableStateOf(false)
    var messageDwarf = mutableStateOf<String?>("")
    var alertBadge = mutableStateOf(false)
    companion object {
        lateinit var database: DwarfsDatabase
        private const val REQUEST_CAMERA_PERMISSION = 123
    }

    val isProgressDialogVisible = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Room.databaseBuilder(
            applicationContext, DwarfsDatabase::class.java, "dwarfs_database")
            //.fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2)
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
    fun getRotationDegrees(context: Context, uri: Uri): Int {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exifInterface = inputStream?.let { ExifInterface(it) }
        val orientation = exifInterface?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private var uploadJob: Job? = null

    suspend fun uploadImage(uri: Uri) = withContext(Dispatchers.IO) {
        uploadJob?.cancel()

        if (!isNetworkAvailable()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity, "No internet connection", Toast.LENGTH_LONG).show()
            }
            return@withContext
        }
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        imageWidth.value = options.outWidth
        imageHeight.value = options.outHeight

        val desiredSize = 2400 // 1200 / 1600 / 2400   -> Adjust the desired size as needed

        options.inSampleSize = max(imageWidth.value, imageHeight.value)/desiredSize
        options.inJustDecodeBounds = false

        val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()


        // ==================
        if (bytes.isEmpty()) {
            Log.d("UPLOAD_ERROR", "Image file is empty")
            return@withContext
        }

        Log.e("Upload URI", "Uri: ${uri}")

        uploadJob = CoroutineScope(Dispatchers.IO).launch {
            val encodedFile = Base64.encodeToString(bytes, Base64.DEFAULT)

            val API_KEY = "9zbTgwMWBvZgonLi9Jla"
            val MODEL_ENDPOINT = "krasnele_wro-g63lz/2"
            val uploadURL =
                "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY;

            var connection: HttpURLConnection? = null
            try {
                withContext(Dispatchers.Main) {
                    isProgressDialogVisible.value = true
                }

                // Timeout block
                withTimeout(30_000) { // 30 seconds
                    val url = URL(uploadURL)
                    connection = url.openConnection() as HttpURLConnection
                    connection!!.requestMethod = "POST"
                    connection!!.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    connection!!.setRequestProperty("Content-Length", encodedFile.toByteArray().size.toString())
                    connection!!.setRequestProperty("Content-Language", "en-US")
                    connection!!.useCaches = false
                    connection!!.doOutput = true
                    // Send request
                    val wr = DataOutputStream(connection!!.outputStream)
                    wr.writeBytes(encodedFile)
                    wr.close()

                    // Get Response
                    val stream = connection!!.inputStream
                    val reader = BufferedReader(InputStreamReader(stream))
                    val responseStringBuilder = StringBuilder()
                    reader.use { rdr ->
                        rdr.lineSequence().forEach { responseStringBuilder.append(it) }
                    }

                    if (isProgressDialogVisible.value){
                        val jsonResponse = JSONObject(responseStringBuilder.toString())
                        val predictions = jsonResponse.getJSONArray("predictions")
                        Log.d("Response", "Response: $predictions")
                        if (predictions.length() > 0) {
                            messageDwarf.value = predictions.getJSONObject(0).getString("class").replace("_", " ")
                            if (messageDwarf.value == "Slepak" || messageDwarf.value == "Gluchak" || messageDwarf.value == "W-Skers") {
                                messageDwarf.value = "Ślepak, Głuchak i W-Skers"
                            }
                            saveToDatabase(message = messageDwarf.value)
                        } else {
                            messageDwarf.value = "Nie odnaleziono krasnala"
                        }
                    }


                    withContext(Dispatchers.Main) {
                        isProgressDialogVisible.value = false
                    }

                    Log.d("TEST_ROBOFLOW", "Class: ${messageDwarf.value}")
                }
            } catch (e: TimeoutCancellationException) {
                withContext(Dispatchers.Main) {
                    isProgressDialogVisible.value = false
                    Toast.makeText(
                        this@HomeActivity,
                        "Upload timed out after 30 seconds.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("UPLOAD_ERROR", "Upload timed out", e)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isProgressDialogVisible.value = false
                    Toast.makeText(
                        this@HomeActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("UPLOAD_ERROR", "Upload failed", e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun cancelUpload() {
        uploadJob?.cancel()
        uploadJob = null // Clear the job reference
        CoroutineScope(Dispatchers.Main).launch {
            responseInfo.value = false
            Toast.makeText(this@HomeActivity, "Upload canceled", Toast.LENGTH_SHORT).show()
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
                }
                else{
                    database.dwarfs().insert(it)
                    if((database.dwarfs().getDwarfsCount() == 1) or
                        (database.dwarfs().getDwarfsCount() % 10 == 0)){
                        alertBadge.value = true
                    }
                }
                // Odkomentuj jak chcesz zobaczyć, bez dobijania do Odznaki
                // i ustaw obecną ilość w twojej bazie
//                if(database.dwarfs().getDwarfsCount() > 5){
//                    alertBadge.value = true
//                }
            }
        } catch (e: Exception) {
            Log.e("Database Insertion", "Error inserting dwarf", e)
        }
    }

    fun getDwarfsList(): Flow<List<Dwarfs>> {
        return database.dwarfs().getAllByDate()
    }

    fun deleteDwarf(dwarf: Dwarfs) {
        CoroutineScope(Dispatchers.IO).launch {
            database.dwarfs().delete(dwarf)
        }
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
                lateinit var ImageModifier: Modifier
                if( activity.imageHeight.value > activity.imageWidth.value){
                    ImageModifier = Modifier.fillMaxHeight()
                }
                else{
                    ImageModifier = Modifier.fillMaxWidth()
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(color = Color(R.color.Light_Red)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top
                        ){
                            if(!activity.responseInfo.value) {
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
                                    Text("Znadź krasnala")
                                }
                            }
                            else{
                                if(activity.isProgressDialogVisible.value){
                                    ProgressDialog(activity = activity, onCancel = { activity.cancelUpload() })
                                }
                                if(activity.alertBadge.value){
                                    NewBadgeDialog(activity = activity)
                                }
                                Text(activity.messageDwarf.value.toString())
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = ImageModifier,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            decodeBitmap(activity, activity.imageUri.value!!)?.let { bitmap ->
                                val rotationDegrees = activity.getRotationDegrees(activity, activity.imageUri.value!!)
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .rotate(rotationDegrees.toFloat())
                                        .padding(12.dp),
                                    contentScale = ContentScale.FillWidth
                                )
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
fun ProgressDialog(activity: HomeActivity, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "Proszę czekać rozpoznajemy twojego krasnala...",
                    color = Light_Purple,
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                )
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(20.dp))
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        activity.isProgressDialogVisible.value = false
                        onCancel()
                    }
                ) {
                    Text("Anuluj")
                }
            }
        }
    )
}

@Composable
fun NewBadgeDialog(activity: HomeActivity){
    AlertDialog(
        onDismissRequest = {
        },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Gratulacje", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Light_Purple)
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "Otrzymujesz nową odznakę!",
                    textAlign = TextAlign.Center,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = Light_Purple
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        activity.alertBadge.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.button_dwarf),
                        contentDescription = "...",
                        tint = Light_Purple
                    )
                }
            }
        }
    )
}