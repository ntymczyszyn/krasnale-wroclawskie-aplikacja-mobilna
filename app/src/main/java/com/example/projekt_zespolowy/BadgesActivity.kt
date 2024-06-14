package com.example.projekt_zespolowy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.projekt_zespolowy.data.Dwarfs
import com.example.projekt_zespolowy.ui.BadgesItem
import com.example.projekt_zespolowy.ui.theme.Dark_Purple
import com.example.projekt_zespolowy.ui.theme.Light_Purple

@Composable
fun BadgesScreen(activity: HomeActivity) {
    val dwarfsList by activity.getDwarfsList().collectAsState(initial = emptyList())
    val items = listOf(
        BadgesItem.FirstRow,
        BadgesItem.SecondRow,
        BadgesItem.ThirdRow,
        BadgesItem.FourthRow,
        // BadgesItem.FifthRow
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Light_Purple.copy(0.1f))
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Łączna liczba odnalezionych krasnali: ${dwarfsList.count()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Dark_Purple
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row() {
                LazyColumn() {
                    items(items) {
                        BadgesItems(dwarfsList = dwarfsList, item = it)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesItems(
    dwarfsList: List<Dwarfs>,
    item: BadgesItem
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(id = item.icon1),
                contentDescription = "Dwarfs Badge 1",
                tint = if (dwarfsList.count() >= item.value1) Dark_Purple else Dark_Purple.copy(
                    0.3f
                ),
            )
            if (item.value1 == 1){
                Row(){
                    Text("Znaleziono pierwszego", style = MaterialTheme.typography.bodyMedium)
                }
                Row(){
                    Text("krasnala", style = MaterialTheme.typography.bodyMedium)
                }
            }
            else {
                Text(item.title1, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(id = item.icon2),
                contentDescription = "Dwarf Badge 2",
                tint = if (dwarfsList.count() >= item.value2) Dark_Purple else Dark_Purple.copy(
                    0.3f
                ),
            )
            Text(item.title2, style = MaterialTheme.typography.bodyMedium)
        }
    }
}