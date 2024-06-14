package com.example.projekt_zespolowy

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple

@Composable
fun HistoryScreen(activity: HomeActivity) {
    val dwarfsList by activity.getDwarfsList().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Light_Purple.copy(0.1f)),
    ){
        Column(){
            Row(
                modifier = Modifier
                    .background(color = Dark_Purple.copy(alpha = 0.1f))
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    ) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Column(
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Nazwa Krasnala",
                                style = MaterialTheme.typography.titleMedium,
                                color = Dark_Purple
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Column (
                            verticalArrangement = Arrangement.Center,
                        ){
                            Text(
                                text = "Data odnalezienia",
                                style = MaterialTheme.typography.titleMedium,
                                color = Dark_Purple
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn() {
                items(dwarfsList) {
                    DwarfCard(dwarf = it, activity = activity)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun DwarfCard(dwarf: Dwarfs,
              activity: HomeActivity) {
    lateinit var day: String
    lateinit var month: String
    day = if(dwarf.date_stamp.date < 10){
        "0" + dwarf.date_stamp.date.toString()
    }
    else{
        dwarf.date_stamp.date.toString()
    }
    month = if(dwarf.date_stamp.month < 9){
        "0" + (dwarf.date_stamp.month + 1).toString()
    }
    else{
        (dwarf.date_stamp.month + 1).toString()
    }
    val year = (dwarf.date_stamp.year + 1900).toString()
    val date = day + "." + month + "." + year
    Row(
        modifier = Modifier
            .background(color = Dark_Purple.copy(alpha = 0.1f))
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            Row {
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = dwarf.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Dark_Purple
                )
            }
        }
        Column(){
            Button(
                onClick = {
                    activity.deleteDwarf(dwarf)
                }
            ){
                Text("usun")
            }
        }
        Column(verticalArrangement = Arrangement.Center,) {
            Row {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    color = Dark_Purple.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}
