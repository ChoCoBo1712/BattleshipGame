package com.example.battleshipgame.ui


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.database.*
import com.example.battleshipgame.service.BattleView

import com.example.battleshipgame.R
import com.example.battleshipgame.service.Constants.CELLS
import com.example.battleshipgame.service.Constants.GAMES
import com.example.battleshipgame.service.Constants.P1
import com.example.battleshipgame.service.Constants.P1READY
import com.example.battleshipgame.service.Constants.P2
import com.example.battleshipgame.service.Constants.P2READY
import com.example.battleshipgame.service.Orientation
import com.example.battleshipgame.service.Ship
import com.example.battleshipgame.viewmodels.ViewModel


class AllocateFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private var orientation: Orientation = Orientation.HORIZONTAL
    private lateinit var allocField: BattleView
    private lateinit var toggle: Button
    private lateinit var shipRankText: TextView
    private lateinit var startPlay: Button

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference
    private lateinit var infoRef: DatabaseReference

    var shipRank = 4
    var shipAmount = 4 - shipRank + 1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allocate, container, false)

        db = FirebaseDatabase.getInstance()
        gameRef = db.getReference(GAMES + "/${viewModel.gameId}")
        infoRef = db.getReference(CELLS + "/${viewModel.gameId}")

        allocField = view.findViewById(R.id.alloc_field)
        toggle = view.findViewById(R.id.orientation_toggle)
        shipRankText = view.findViewById(R.id.tv_ship_size)
        startPlay = view.findViewById(R.id.btn_start_play)

        allocField.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP && (shipAmount != 0 || shipRank != 1)) {
                val xTouch = event.x
                val yTouch = event.y
                val i = (xTouch / allocField.cellWidth).toInt()
                val j = (yTouch / allocField.cellHeight).toInt()

                if (checkShipLocation(i, j, orientation, shipRank)) {
                    allocField.addShip(i, j, orientation, shipRank)
                    shipAmount -= 1
                    when {
                        shipAmount == 0 && shipRank == 1 -> {
                            val myFieldPath = when (viewModel.playerNum) {
                                1 -> P1
                                else -> P2
                            }

                            val cellsCoord = mutableListOf<Pair<Int, Int>>()
                            for (x in 0..9) {
                                for (y in 0..9) {
                                    if(viewModel.myCells[x][y].isShip) {
                                        cellsCoord.add(Pair(x, y))
                                    }
                                }
                            }
                            infoRef.child(myFieldPath).setValue(cellsCoord)

                            shipRankText.text = getString(R.string.ships_are_set)
                            startPlay.visibility = VISIBLE
                            viewModel.shipRects = allocField.shipRects
                            val path = when (viewModel.playerNum) {
                                1 -> P1READY
                                else -> P2READY
                            }
                            gameRef.child(path).setValue(true)
                        }
                        shipAmount == 0 -> {
                            shipRank -= 1
                            shipAmount  = 4 - shipRank + 1
                            shipRankText.text = resources.getString(
                                R.string.place_ships,
                                shipAmount,
                                shipRank
                            )
                        }
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        toggle.setOnClickListener {
            orientation = when (toggle.text) {
                resources.getString(R.string.vertical) -> {
                    toggle.setText(R.string.horizontal)
                    Orientation.VERTICAL
                }
                else -> {
                    toggle.setText(R.string.vertical)
                    Orientation.HORIZONTAL
                }
            }
        }

        startPlay.setOnClickListener {
            findNavController().navigate(R.id.action_allocateFragment_to_gameFragment)
        }

        subscribeForReadyUser()

        return view
    }

    private fun checkShipLocation(i: Int, j: Int, orientation: Orientation, rank: Int): Boolean {

        if(shipAmount == 5) {
            return false
        }

        when (orientation) {
            Orientation.HORIZONTAL -> {

                if (i + rank - 1 > 9) {
                    return false
                }

                for (x in i until i + rank) {
                    if((viewModel.myCells[x][j].isShip) ||
                        ((j - 1 >= 0) && (viewModel.myCells[x][j - 1].isShip)) ||
                        ((j + 1 < 10) && (viewModel.myCells[x][j + 1].isShip))) {
                        return false
                    }
                }
                if (((i - 1 >= 0) &&
                    ((viewModel.myCells[i - 1][j].isShip) ||
                    ((j - 1 >= 0) && (viewModel.myCells[i - 1][j - 1].isShip)) ||
                    ((j + 1 < 10) && (viewModel.myCells[i - 1][j + 1].isShip)))) ||
                    ((i + rank < 10) &&
                    ((viewModel.myCells[i + rank][j].isShip) ||
                    ((j - 1 >= 0) && (viewModel.myCells[i + rank][j - 1].isShip)) ||
                    ((j + 1 < 10) && (viewModel.myCells[i + rank][j + 1].isShip))))) {
                    return false
                }

                val newShip = Ship()
                newShip.rank = shipRank
                newShip.orientation = orientation
                for (x in i until i + rank) {
                    viewModel.myCells[x][j].isShip = true
                    viewModel.myCells[x][j].x = x
                    viewModel.myCells[x][j].y = j
                    newShip.cells.add(viewModel.myCells[x][j])
                }
                viewModel.myShips.add(newShip)
            }
            else -> {
                if (j + rank - 1 > 9) {
                    return false
                }

                for (y in j until j + rank) {
                    if((viewModel.myCells[i][y].isShip) ||
                        ((i - 1 >= 0) && (viewModel.myCells[i - 1][y].isShip)) ||
                        ((i + 1 < 10) && (viewModel.myCells[i + 1][y].isShip))) {
                        return false
                    }
                }
                if (((j - 1 >= 0) &&
                    ((viewModel.myCells[i][j - 1].isShip) ||
                    ((i - 1 >= 0) && (viewModel.myCells[i - 1][j - 1].isShip)) ||
                    ((i + 1 < 10) && (viewModel.myCells[i + 1][j - 1].isShip)))) ||
                    ((j + rank < 10) &&
                    ((viewModel.myCells[i][j + rank].isShip) ||
                    ((i - 1 >= 0) && (viewModel.myCells[i - 1][j + rank].isShip)) ||
                    ((i + 1 < 9) && (viewModel.myCells[i + 1][j + rank].isShip))))) {
                    return false
                }

                val newShip = Ship()
                newShip.rank = shipRank
                newShip.orientation = orientation
                for (y in j until j + rank) {
                    viewModel.myCells[i][y].isShip = true
                    viewModel.myCells[i][y].x = i
                    viewModel.myCells[i][y].y = y
                    newShip.cells.add(viewModel.myCells[i][y])
                }
                viewModel.myShips.add(newShip)
            }
        }
        return true
    }

    private fun subscribeForReadyUser() {
        val path = when (viewModel.playerNum) {
            1 -> P2READY
            else -> P1READY
        }
        gameRef.child(path).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == true) {
                    startPlay.isEnabled = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
