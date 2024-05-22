package com.example.projekt_zespolowy


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt_zespolowy.google.GoogleAuthUiClient
import com.example.projekt_zespolowy.google.ProfileScreen
import com.example.projekt_zespolowy.google.SignInScreen
import com.example.projekt_zespolowy.google.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjektzespolowyTheme {
               //StartScreen()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in" ){
                        composable("sign_in"){
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if(googleAuthUiClient.getSignedInUser() != null){
                                    navController.navigate("profile")
//                                    val navigate = Intent(this@MainActivity, HomeActivity::class.java)
//                                    startActivity(navigate)
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult ={result ->
                                    if(result.resultCode == RESULT_OK){
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful){
                                    Toast.makeText(applicationContext,"Zalogowano pomyślnie",Toast.LENGTH_LONG).show()
//                                    navController.navigate("profile")
                                    navController.navigate("PicturePage")
                                    viewModel.resetState()
                                }
                            }

                            SignInScreen(
                                state = state,
                                onSignInClik ={
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }

                        composable("profile"){
                            ProfileScreen(
                                activity = this@MainActivity,
                                userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(applicationContext,"Wylogowano pomyślnie",Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                    }
                                }

                            )
                        }
                        composable("PicturePage"){
                            val navigate = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(navigate)
                        }
                    }
                }

            }
        }
    }


    @Composable
    private fun StartScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
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
                    Text("Witaj w aplikacji Krasnale Wrocławskie!",
                        style = MaterialTheme.typography.titleLarge,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        onClick = {
                            val navigate = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(navigate)
                        }
                    ) {
                        Text("Zaczynajmy!")
                    }
                }
            }
        }
    }
}


