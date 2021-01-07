package com.example.battleshipgame.service

class Ship {
    var rank: Int = 4
    var orientation: Orientation = Orientation.HORIZONTAL
    var cells: MutableList<Cell> = mutableListOf()
}

enum class Orientation {
    HORIZONTAL, VERTICAL
}