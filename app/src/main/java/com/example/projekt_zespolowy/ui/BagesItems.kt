package com.example.projekt_zespolowy.ui

import com.example.projekt_zespolowy.R

sealed class BadgesItem(var icon1: Int, var title1: String, var value1: Int, var icon2: Int, var title2: String, var value2: Int) {
    data object FirstRow : BadgesItem(R.drawable.badge_1_dwarf, "Znaleziono pierwszego krasnala", 1, R.drawable.badge_10_dwarfs, "Odnaleziono 10 krasnali", 10)
    data object SecondRow : BadgesItem(R.drawable.badge_20_dwarfs, "Odnaleziono 20 krasnali", 20, R.drawable.badge_30_dwarfs, "Odnaleziono 30 krasnali", 30)
    data object ThirdRow : BadgesItem(R.drawable.badge_40_dwarfs, "Odnaleziono 40 krasnali", 40,R.drawable.badge_50_dwarfs, "Odnaleziono 50 krasnali", 50)
    data object FourthRow: BadgesItem(R.drawable.badge_60_dwarfs, "Odnaleziono 60 krasnali", 60,R.drawable.badge_70_dwarfs, "Odnaleziono 70 krasnali", 70)
    data object FifthRow: BadgesItem(R.drawable.badge_80_dwarfs, "Odnaleziono 80 krasnali", 80,R.drawable.badge_90_dwarfs, "Odnaleziono 90 krasnali", 90)

}