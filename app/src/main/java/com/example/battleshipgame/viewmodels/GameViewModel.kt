package com.example.battleshipgame.viewmodels

data class GameViewModel(
    var player1: String = "",
    var player2: String = "",
    var move: Int = 1,
    var p1Ready: Boolean = false,
    var p2Ready: Boolean = false,
    var isFinished: Boolean = false
)