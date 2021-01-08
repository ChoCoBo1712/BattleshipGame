package com.example.battleshipgame.ui


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.example.battleshipgame.service.BattleView

import com.example.battleshipgame.R
import com.example.battleshipgame.service.CellState
import com.example.battleshipgame.service.Constants.CELLS
import com.example.battleshipgame.service.Constants.FIRST
import com.example.battleshipgame.service.Constants.GAMES
import com.example.battleshipgame.service.Constants.MOVE
import com.example.battleshipgame.service.Constants.ONE
import com.example.battleshipgame.service.Constants.OPPMOVE
import com.example.battleshipgame.service.Constants.P1
import com.example.battleshipgame.service.Constants.P2
import com.example.battleshipgame.service.Constants.SECOND
import com.example.battleshipgame.service.Constants.TWO
import com.example.battleshipgame.service.Constants.USERS
import com.example.battleshipgame.service.Constants.WINS
import com.example.battleshipgame.viewmodels.ViewModel


class GameFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference
    private lateinit var infoRef: DatabaseReference
    private lateinit var moveNumText: TextView
    private lateinit var myField: BattleView
    private lateinit var userField: BattleView
    private var isInit: Boolean = true


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        db = FirebaseDatabase.getInstance()
        gameRef = db.getReference(GAMES + "/${viewModel.gameId}")
        infoRef = db.getReference(CELLS + "/${viewModel.gameId}")

        moveNumText = view.findViewById(R.id.tv_move_num)
        myField = view.findViewById(R.id.my_field)
        userField = view.findViewById(R.id.user_field)

        myField.shipRects = viewModel.shipRects
        myField.cells = viewModel.myCells
        userField.cells = viewModel.oppCells

        val oppFieldPath = when (viewModel.playerNum) {
            1 -> P2
            else -> P1
        }

        infoRef.child(oppFieldPath).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }

            override fun onDataChange(snap: DataSnapshot) {
                for (cell in snap.children) {
                    var i = cell.child(FIRST).getValue(Int::class.java)
                    var j = cell.child(SECOND).getValue(Int::class.java)

                    if (i == null || j == null) {
                        i = 0
                        j = 0
                    }
                    viewModel.oppCells[i][j].isShip = true
                }
            }
        })

        userField.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    val xTouch = event.x
                    val yTouch = event.y
                    val i = (xTouch / userField.cellWidth).toInt()
                    val j = (yTouch / userField.cellHeight).toInt()
                    if (viewModel.moveNum == viewModel.playerNum) {
                        makeMove(i, j)
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        gameRef.child(MOVE).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                when {
                    num != 0 -> {
                        viewModel.moveNum = num
                        when (viewModel.moveNum) {
                            viewModel.playerNum -> {
                                moveNumText.text = getString(R.string.your_move)
                            }
                            else -> {
                                moveNumText.text = getString(R.string.opp_move)
                            }
                        }
                    }
                    else -> {
                        if(viewModel.winnerNum == viewModel.playerNum) {
                            return
                        }
                        var oldAllValue = 0
                        val userRef = db.getReference(USERS + "/${viewModel.userId}")
                        userRef.child(GAMES).addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(p0: DataSnapshot) {
                                oldAllValue = p0.getValue(Int::class.java) ?: 0
                            }

                        })
                        userRef.child(GAMES).setValue(oldAllValue + 1)
                        navigateToRes()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        val opp = when (viewModel.playerNum) {
            1 -> TWO
            else -> ONE
        }
        gameRef.child(OPPMOVE).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val i = snapshot.child("$opp/" + FIRST).getValue(Int::class.java) ?: 0
                val j = snapshot.child("$opp/" + SECOND).getValue(Int::class.java) ?: 0
                when {
                    !isInit -> {
                        when {
                            viewModel.myCells[i][j].isShip -> {
                                myField.cells[i][j].state = CellState.HIT
                            }
                            else -> {
                                myField.cells[i][j].state = CellState.MISS
                            }
                        }
                        myField.refreshCanvas()
                    }
                    else -> {
                        isInit = false
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        return view
    }

    private fun makeMove(i: Int, j: Int) {
        if(viewModel.oppCells[i][j].state != CellState.EMPTY) {
            return
        }

        val playerNum = when (viewModel.playerNum) {
            1 -> "$OPPMOVE/$ONE"
            else -> "$OPPMOVE/$TWO"
        }
        gameRef.child(playerNum).setValue(Pair(i, j))

        when {
            viewModel.oppCells[i][j].isShip -> {
                viewModel.oppCells[i][j].state = CellState.HIT
                userField.cells[i][j].state = CellState.HIT
                userField.refreshCanvas()
                if (allCellsDefeated()) {
                    viewModel.winnerNum = viewModel.playerNum
                    gameRef.child(MOVE).setValue(0)
                    var oldAllValue = 0
                    var oldWinsValue = 0
                    val userRef = db.getReference(USERS + "/${viewModel.userId}")

                    userRef.child(GAMES).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            oldAllValue = p0.getValue(Int::class.java) ?: 0
                        }
                    })

                    userRef.child(WINS).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            oldWinsValue = p0.getValue(Int::class.java) ?: 0
                        }
                    })

                    userRef.child(GAMES).setValue(oldAllValue + 1)
                    userRef.child(WINS).setValue(oldWinsValue + 1)
                    navigateToRes()
                }
            }
            else -> {
                viewModel.oppCells[i][j].state = CellState.MISS
                userField.cells[i][j].state = CellState.MISS
                userField.refreshCanvas()
                val newMove = if(viewModel.moveNum == 1) 2 else 1
                gameRef.child(MOVE).setValue(newMove)
            }
        }
    }

    private fun allCellsDefeated(): Boolean {
        for(i in 0..9){
            for(j in 0..9) {
                if(viewModel.oppCells[i][j].isShip &&
                    viewModel.oppCells[i][j].state == CellState.EMPTY) {
                    return false
                }
            }
        }

        return true
    }

    private fun navigateToRes() {
        findNavController().navigate(R.id.action_gameFragment_to_resultFragment)
    }
}
