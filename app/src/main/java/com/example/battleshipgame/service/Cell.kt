package com.example.battleshipgame.service

class Cell {
    var x: Int = 0
    var y: Int = 0
    var state: CellState = CellState.EMPTY
    var isShip: Boolean = false
}

enum class CellState {
    EMPTY, HIT, MISS
}