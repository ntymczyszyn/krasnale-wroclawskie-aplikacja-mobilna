package com.example.projekt_zespolowy.google

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.projekt_zespolowy.HomeActivity
import com.example.projekt_zespolowy.MainActivity
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
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Light_Purple),
            modifier = Modifier.padding(16.dp),
            onClick = {
            val navigate = Intent(activity, HomeActivity::class.java)
            activity.startActivity(navigate)
        }) {
            Text(text = "Przejdź do aplikacji")
        }
        if(userData?.profilePictureUrl != null){
            AsyncImage(
                model = userData.profilePictureUrl,
                contentDescription = "Profilowe",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if(userData?.username != null){
            Text(text = userData.username,
                textAlign = TextAlign.Center,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = Light_Purple
            )
            Spacer(modifier = Modifier.height(16.dp))
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