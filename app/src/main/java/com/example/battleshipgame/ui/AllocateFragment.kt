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
import com.example.battleshipgame.service.BattleField

import com.example.battleshipgame.R
import com.example.battleshipgame.service.Orientation
import com.example.battleshipgame.service.Ship
import com.example.battleshipgame.viewmodels.ViewModel


class AllocateFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private var orientation: Orientation = Orientation.HORIZONTAL
    private lateinit var allocField: BattleField
    private lateinit var horizontal: Button
    private lateinit var vertical: Button
    private lateinit var shipRankText: TextView
    private lateinit var startPlay: Button

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference
    private lateinit var infoRef: DatabaseReference

    var shipRank = 4
    var shipAmount = 4 - shipRank + 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseDatabase.getInstance()
        gameRef = db.getReference("games/${viewModel.gameId}")
        infoRef = db.getReference("cells/${viewModel.gameId}")

        return inflater.inflate(R.layout.fragment_allocate, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allocField = view.findViewById(R.id.alloc_field)
        horizontal = view.findViewById(R.id.btn_horizontal)
        vertical = view.findViewById(R.id.btn_vertical)
        shipRankText = view.findViewById(R.id.tv_ship_size)
        startPlay = view.findViewById(R.id.btn_start_play)

        allocField.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    if (shipAmount == 0 && shipRank == 1) {
                        Toast.makeText(activity,"No more ships", Toast.LENGTH_SHORT).show()
                    } else {
                        val xTouch = event.x
                        val yTouch = event.y
                        val i = (xTouch / allocField.cellWidth).toInt()
                        val j = (yTouch / allocField.cellHeight).toInt()
                        if (checkShipLocation(i, j, orientation, shipRank)) {
                            allocField.addShip(i, j, orientation, shipRank)
                            shipAmount -= 1
                            if (shipAmount == 0 && shipRank == 1) {
                                Toast.makeText(activity,"No more ships", Toast.LENGTH_SHORT).show()

                                val myFieldPath = if (viewModel.playerNum == 1) "p1" else "p2"
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
                                val path = if(viewModel.playerNum == 1) "p1Ready" else "p2Ready"
                                gameRef.child(path).setValue(true)
                            }
                            else if(shipAmount == 0) {
                                shipRank -= 1
                                shipAmount  = 4 - shipRank + 1
                                shipRankText.text = "Place $shipAmount ships of rank $shipRank"
                            }
                        } else {
                            Toast.makeText(activity,"Can't locate ship here", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        horizontal.setOnClickListener {
            orientation = Orientation.HORIZONTAL
        }

        vertical.setOnClickListener {
            orientation = Orientation.VERTICAL
        }

        startPlay.setOnClickListener {
            findNavController().navigate(R.id.action_allocateFragment_to_gameFragment)
        }

        subscribeForReadyUser()
    }

    private fun checkShipLocation(i: Int, j: Int, orientation: Orientation, rank: Int): Boolean {

        if(shipAmount == 5) {
            Toast.makeText(activity, "No more ships", Toast.LENGTH_SHORT).show()
            return false
        }

        if(orientation == Orientation.HORIZONTAL) {

            if (i + rank - 1 > 9) {
                return false
            }

            for (x in i until i + rank) {
                if(viewModel.myCells[x][j].isShip) {
                    return false
                }
                if (j - 1 >= 0) {
                    if(viewModel.myCells[x][j - 1].isShip) {
                        return false
                    }
                }
                if(j + 1 < 10) {
                    if(viewModel.myCells[x][j + 1].isShip) {
                        return false
                    }
                }
            }
            if (i - 1 >= 0) {
                if(viewModel.myCells[i - 1][j].isShip) {
                    return false
                }
                if(j - 1 >= 0) {
                    if(viewModel.myCells[i - 1][j - 1].isShip) {
                        return false
                    }
                }
                if(j + 1 < 10) {
                    if(viewModel.myCells[i - 1][j + 1].isShip) {
                        return false
                    }
                }
            }
            if (i + rank < 10) {
                if(viewModel.myCells[i + rank][j].isShip) {
                    return false
                }
                if(j - 1 >= 0) {
                    if(viewModel.myCells[i + rank][j - 1].isShip) {
                        return false
                    }
                }
                if(j + 1 < 10) {
                    if(viewModel.myCells[i + rank][j + 1].isShip) {
                        return false
                    }
                }
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
        } else {
            if (j + rank - 1 > 9) {
                return false
            }

            for (y in j until j + rank) {
                if(viewModel.myCells[i][y].isShip) {
                    return false
                }
                if (i - 1 >= 0) {
                    if(viewModel.myCells[i - 1][y].isShip) {
                        return false
                    }
                }
                if(i + 1 < 10) {
                    if(viewModel.myCells[i + 1][y].isShip) {
                        return false
                    }
                }
            }

            if (j - 1 >= 0) {
                if(viewModel.myCells[i][j - 1].isShip) {
                    return false
                }
                if(i - 1 >= 0) {
                    if(viewModel.myCells[i - 1][j - 1].isShip) {
                        return false
                    }
                }
                if(i + 1 < 10) {
                    if(viewModel.myCells[i + 1][j - 1].isShip) {
                        return false
                    }
                }
            }

            if (j + rank < 10) {
                if(viewModel.myCells[i][j + rank].isShip) {
                    return false
                }
                if(i - 1 >= 0) {
                    if(viewModel.myCells[i - 1][j + rank].isShip) {
                        return false
                    }
                }
                if(i + 1 < 9) {
                    if(viewModel.myCells[i + 1][j + rank].isShip) {
                        return false
                    }
                }
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
        return true
    }

    private fun subscribeForReadyUser() {
        val path = if (viewModel.playerNum == 1) "p2Ready" else "p1Ready"
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
