package com.example.projekt_zespolowy

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.projekt_zespolowy.ui.NavigationItem
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
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
    private lateinit var imageUri: Uri
    private lateinit var file: File
    val uploadedImage = mutableStateOf<ImageBitmap?>(null)
    //Android Client ID - 492039840169-k15asq5q9pi8p1n8et6gvsg0tbo9j01o.apps.googleusercontent.com
    //Web Client ID - 492039840169-dco3kmv75kkr4djr4ua4fnu6a5g13beh.apps.googleusercontent.com
    //Web Secret ID - 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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjektzespolowyTheme {
                MainScreen()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun HomeScreen() {
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
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (hasCameraPermission()) {
                            cameraLauncher.launch(uri)
                        } else {
                            requestCameraPermission()
                            if (hasCameraPermission()) {
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


    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        Scaffold(
            topBar = { TopBar() },
            bottomBar = { BottomNavigationBar(navController) },
            content = { padding ->
                Box(modifier = Modifier
                    .padding(padding)
                    .background(Light_Purple)
                ) {
                    Navigation(navController = navController)
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )
    }

    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Home.route) {
            composable(NavigationItem.Home.route) {
                HomeScreen()
            }
            composable(NavigationItem.History.route) {
                HistoryScreen()
            }
            composable(NavigationItem.Badges.route) {
                BadgesScreen()
            }
        }
    }

    @Composable
    fun DropDownPanel() {
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopStart)
        )  {
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(16.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Red)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Profile"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        val navigate = Intent(this@HomeActivity, MainActivity::class.java)
                        startActivity(navigate)

                    }
                )
            }
        }
    }

    @Composable
    fun TopBar() {
        TopAppBar(
            title = {
                Text(
                    text = "Krasnale Wrocławskie",
                    fontSize = 18.sp,
                    color = Light_Purple,
                    modifier = Modifier.width(300.dp),
                    textAlign = TextAlign.Center
                )
                DropDownPanel()
            },
            backgroundColor = Dark_Purple,
        )
    }

    @Composable
    fun BottomNavigationBar(navController: NavController) {
        val items = listOf(
            NavigationItem.Home,
            NavigationItem.History,
            NavigationItem.Badges
        )
        BottomNavigation(
            backgroundColor = Dark_Purple,
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEach { item ->
                BottomNavigationItem(
                    icon = {
                        Icon(
                            painterResource(id = item.icon),
                            contentDescription = item.title,
                            tint = if (currentRoute == item.route) Light_Purple else Light_Purple.copy(0.4f),
                        )
                    },
                    label = { Text(text = item.title, color = if (currentRoute == item.route) Light_Purple else Light_Purple.copy(0.4f)) },
                    alwaysShowLabel = true,
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

}



