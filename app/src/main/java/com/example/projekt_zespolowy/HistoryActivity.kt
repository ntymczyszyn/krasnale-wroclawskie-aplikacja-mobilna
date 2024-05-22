package com.example.projekt_zespolowy

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple

@Composable
fun HistoryScreen(activity: HomeActivity) {
    //val dwarfsList by activity.getDwarfsList().collectAsState(initial = emptyList())
    val dwarfsList by activity.getDwarfsList().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Light_Purple),
        contentAlignment = Alignment.Center
    ){
        Text(text = "History")
        LazyColumn(){
            items(dwarfsList) {
                DwarfCard(dwarf = it)
            }
        }
    }
}

@Composable
fun DwarfCard(dwarf: Dwarfs) {
    lateinit var day: String
    lateinit var month: String
    if(dwarf.date_stamp.day < 10){
        day = "0" + dwarf.date_stamp.day.toString()
    }
    else{
        day = dwarf.date_stamp.day.toString()
    }
    if(dwarf.date_stamp.month < 10){
        month = "0" + dwarf.date_stamp.month.toString()
    }
    else{
        month = dwarf.date_stamp.month.toString()
    }
    val year = dwarf.date_stamp.year.toString()
    val date = day + "." + month + "." + year
    Row(
        modifier = Modifier.background(color = Dark_Purple).fillMaxWidth(),
    ){
        Column{
            Row{
                Text(text = dwarf.name)
            }
            Row{
                Text(date, color = Light_Purple)
            }
        }
    }
}
