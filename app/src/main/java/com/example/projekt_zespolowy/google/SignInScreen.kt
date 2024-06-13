package com.example.projekt_zespolowy.google

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.projekt_zespolowy.HomeActivity
import com.example.projekt_zespolowy.MainActivity
import com.example.projekt_zespolowy.R
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple
import com.example.projekt_zespolowy.ui.theme.Pink40

@Composable
fun SignInScreen(
    state: SignInState,
    onSignInClik: () -> Unit
){
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(context,error, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Pink40),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ){
                Text("Witaj w aplikacji",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black)
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
                    color = Color.Black)
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text("Wyszukuj krasnale w mieście Wrocław",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black)
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Icon(
                painterResource(id = R.drawable.start_page_dwarf),
                contentDescription = "Star Page Dwarf",
                tint = Dark_Purple,
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Light_Purple),
                    onClick = onSignInClik
                ) {
                    Text(text = "Zaloguj się")
                }
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Light_Purple),
                    onClick = {
                        val navigate = Intent((context as MainActivity), HomeActivity::class.java)
                        (context as MainActivity).startActivity(navigate)
                    }
                ) {
                    Text(text = "Kontunuuj bez logowania")
                }
            }
        }
    }

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        contentAlignment = Alignment.Center
//    ){
//        Button(onClick = onSignInClik) {
//            Text(text = "Zaloguj się")
//        }
//    }
}