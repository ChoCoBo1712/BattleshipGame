package com.example.battleshipgame.viewmodels

import android.graphics.RectF
import androidx.lifecycle.ViewModel
import com.example.battleshipgame.service.Cell
import com.example.battleshipgame.service.Game
import com.example.battleshipgame.service.Ship

class ViewModel: ViewModel() {

    var playerNum: Int = 0
    var userId: String = ""
    var userEmail: String = ""
    var gameId: Int = -1
    var game: Game? = null
    var moveNum: Int = 1
    var winnerNum: Int = 0

    var myCells: Array<Array<Cell>> = Array(10) { Array(10) { Cell() } }
    var oppCells: Array<Array<Cell>> = Array(10) { Array(10) { Cell() } }
    var oppShips = mutableListOf<Ship>()
    var myShips = mutableListOf<Ship>()
    var shipRects = mutableListOf<RectF>()


    fun clear() {
        playerNum = 0
        gameId = 0
        game = null
        myCells = Array(10) { Array(10) { Cell() } }
        oppCells = Array(10) { Array(10) { Cell() } }
        shipRects = mutableListOf()
        moveNum = 1
    }
}