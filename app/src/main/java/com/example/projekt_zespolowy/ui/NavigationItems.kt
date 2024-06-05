package com.example.projekt_zespolowy.ui

import com.example.projekt_zespolowy.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Home : NavigationItem("home", R.drawable.baseline_home, "Start")
    data object History : NavigationItem("history", R.drawable.baseline_history, "Historia")
    data object Badges : NavigationItem("badges", R.drawable.baseline_badges, "Odznaki")
}