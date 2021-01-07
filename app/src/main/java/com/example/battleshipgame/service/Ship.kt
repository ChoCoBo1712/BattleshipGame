package com.example.battleshipgame.service

class Ship {
    var rank: Int = 5
    var orientation: Orientation = Orientation.HORIZONTAL
    var cells: MutableList<Cell> = mutableListOf()

    fun isKilled(): Boolean {
        return cells.all { cell -> cell.state == CellState.HIT }
    }
}