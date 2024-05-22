package com.example.projekt_zespolowy

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple

@Composable
fun HistoryScreen(activity: HomeActivity) {
    // val dwarfsList by activity.getDwarfsList().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Light_Purple),
        contentAlignment = Alignment.Center
    ){
        Text(text = "History")
        LazyColumn(){
//            items(dwarfsList) {
//                DwarfCard(dwarf = it)
//            }
        }
    }
}

@Composable
fun DwarfCard(dwarf: Dwarfs) {
    Row(
        modifier = Modifier.background(color = Dark_Purple),
    ){
        Text(text = dwarf.name)
        Text(text = dwarf.date_stamp.toString(), color = Light_Purple)
    }
}
