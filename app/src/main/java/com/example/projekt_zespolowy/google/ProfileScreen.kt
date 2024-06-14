package com.example.projekt_zespolowy.google

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.projekt_zespolowy.HomeActivity
import com.example.projekt_zespolowy.MainActivity
import com.example.projekt_zespolowy.R
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple

@Composable
fun ProfileScreen(
    activity: MainActivity,
    userData: UserData?,
    onSignOut: () -> Unit
){
    Column (
        modifier = Modifier
            .background(color = Dark_Purple)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if(userData?.username != null){
            Text(text = userData.username,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = Light_Purple
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ){
            Text("Witaj w aplikacji",
                style = MaterialTheme.typography.headlineLarge,
                color = Light_Purple)
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ){
            Text("Krasnale Wrocławskie!",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                color = Light_Purple)
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Icon(
            painterResource(id = R.drawable.start_page_dwarf),
            contentDescription = "Star Page Dwarf",
            tint = Light_Purple,
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Light_Purple),
            modifier = Modifier.padding(16.dp),
            onClick = {
            val navigate = Intent(activity, HomeActivity::class.java)
            activity.startActivity(navigate)
        }) {
            Text(text = "Przejdź do aplikacji")
        }
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = Light_Purple)
        ) {
            Text(text = "Wyloguj")
        }
    }
}