package com.example.projekt_zespolowy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.projekt_zespolowy.ui.NavigationItem
import com.example.projekt_zespolowy.ui.theme.ProjektzespolowyTheme
import com.example.projekt_zespolowy.ui.theme.Purple40
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.compose.rememberNavController
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple
import com.example.projekt_zespolowy.ui.theme.White

//class NavBarActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ProjektzespolowyTheme {
//                MainScreen()
//            }
//        }
//    }
//
//    @Composable
//    fun MainScreen() {
//        val navController = rememberNavController()
//        Scaffold(
//            topBar = { TopBar() },
//            bottomBar = { BottomNavigationBar(navController) },
//            content = { padding ->
//                Box(modifier = Modifier.padding(padding)) {
//                    Navigation(navController = navController)
//                }
//            },
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White),
//        )
//    }
//
//    @Composable
//    fun Navigation(navController: NavHostController) {
//        NavHost(navController, startDestination = NavigationItem.Home.route) {
//            composable(NavigationItem.Home.route) {
//                HomeScreen()
//            }
//            composable(NavigationItem.History.route) {
//                HistoryScreen()
//            }
//            composable(NavigationItem.Badges.route) {
//                BadgesScreen()
//            }
//        }
//    }

//    @Composable
//    fun TopBar() {
//        TopAppBar(
//            title = {
//                Text(
//                    text = "Krasnale Wrocławskie",
//                    fontSize = 18.sp,
//                    color = Light_Purple,
//                    modifier = Modifier.fillMaxWidth(),
//                    textAlign = TextAlign.Center
//                )
//                DropDownPanel()
//            },
//            backgroundColor = Dark_Purple,
//        )
//    }

//@Composable
//fun DropDownPanel() {
//    var expanded by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .wrapContentSize(Alignment.TopStart)
//    )  {
//        IconButton(
//            onClick = { expanded = !expanded },
//            modifier = Modifier.padding(16.dp),
//            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Red)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Menu,
//                contentDescription = "..."
//            )
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            DropdownMenuItem(
//                text = { Text("Take Photo") },
//                onClick = {
//
//
//                }
//            )
//        }
//    }
//}
//
//
//    @Composable
//    fun BottomNavigationBar(navController: NavController) {
//        val items = listOf(
//            NavigationItem.Home,
//            NavigationItem.History,
//            NavigationItem.Badges
//        )
//        BottomNavigation(
//            backgroundColor = Dark_Purple,
//        ) {
//            val navBackStackEntry by navController.currentBackStackEntryAsState()
//            val currentRoute = navBackStackEntry?.destination?.route
//            items.forEach { item ->
//                BottomNavigationItem(
//                    icon = {
//                        Icon(
//                            painterResource(id = item.icon),
//                            contentDescription = item.title,
//                            tint = if (currentRoute == item.route) Light_Purple else Light_Purple.copy(0.4f),
//                        )
//                    },
//                    label = { Text(text = item.title, color = if (currentRoute == item.route) Light_Purple else Light_Purple.copy(0.4f)) },
//                    alwaysShowLabel = true,
//                    selected = currentRoute == item.route,
//                    onClick = {
//                        navController.navigate(item.route) {
//                            // Pop up to the start destination of the graph to
//                            // avoid building up a large stack of destinations
//                            // on the back stack as users select items
//                            navController.graph.startDestinationRoute?.let { route ->
//                                popUpTo(route) {
//                                    saveState = true
//                                }
//                            }
//                            // Avoid multiple copies of the same destination when
//                            // reselecting the same item
//                            launchSingleTop = true
//                            // Restore state when reselecting a previously selected item
//                            restoreState = true
//                        }
//                    }
//                )
//            }
//        }
//    }
//}
