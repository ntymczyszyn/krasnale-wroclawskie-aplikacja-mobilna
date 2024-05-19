package com.example.projekt_zespolowy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.projekt_zespolowy.ui.theme.Light_Purple

@Composable
fun HistoryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Light_Purple),
        contentAlignment = Alignment.Center
    ){
        Text(text = "History")
    }
}
