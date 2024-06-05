package com.example.projekt_zespolowy.ui

import com.example.projekt_zespolowy.R

sealed class BadgesItem(var icon1: Int, var title1: String, var value1: Int, var icon2: Int, var title2: String, var value2: Int) {
    data object firstRow : BadgesItem(R.drawable.badge_1_dwarf, "Znaleziono pierwszego krasnala", 1, R.drawable.badge_10_dwarfs, "Odnaleziono 10 krasnali", 10)
    data object secondRow : BadgesItem(R.drawable.badge_20_dwarfs, "Odnaleziono 20 krasnali", 20, R.drawable.badge_30_dwarfs, "Odnaleziono 30 krasnali", 30)
    data object thirdRow : BadgesItem(R.drawable.badge_40_dwarfs, "Odnaleziono 40 krasnali", 40,R.drawable.badge_50_dwarfs, "Odnaleziono 50 krasnali", 50)
}