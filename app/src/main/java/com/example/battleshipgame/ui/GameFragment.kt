package com.example.battleshipgame.ui


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.example.battleshipgame.service.BattleField

import com.example.battleshipgame.R
import com.example.battleshipgame.service.CellState
import com.example.battleshipgame.service.CircleTransform
import com.example.battleshipgame.viewmodels.ViewModel
import com.squareup.picasso.Picasso


class GameFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var db: FirebaseDatabase
    private lateinit var gameRef: DatabaseReference
    private lateinit var infoRef: DatabaseReference

    private lateinit var moveNumText: TextView
    private lateinit var myField: BattleField
    private lateinit var userField: BattleField

    private var isInit: Boolean = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseDatabase.getInstance()
        gameRef = db.getReference("games/${viewModel.gameId}")
        infoRef = db.getReference("cells/${viewModel.gameId}")

        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moveNumText = view.findViewById(R.id.tv_move_num)
        myField = view.findViewById(R.id.my_field)
        userField = view.findViewById(R.id.user_field)

        myField.shipRects = viewModel.shipRects
        myField.cells = viewModel.myCells
        userField.cells = viewModel.oppCells

        val oppFieldPath = if (viewModel.playerNum == 1) "p2" else "p1"

        infoRef.child(oppFieldPath).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }

            override fun onDataChange(snap: DataSnapshot) {
                Log.d(TAG, "${snap.childrenCount}")
                for (cell in snap.children) {
                    var i = cell.child("first").getValue(Int::class.java)
                    var j = cell.child("second").getValue(Int::class.java)

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

        gameRef.child("move").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                if (num != 0) {
                    viewModel.moveNum = num
                    if(viewModel.moveNum == viewModel.playerNum) {
                        moveNumText.text = getString(R.string.your_move)
                    } else {
                        moveNumText.text = getString(R.string.opp_move)
                    }
                } else {
                    if(viewModel.winnerNum == viewModel.playerNum) {
                        return
                    }
                    var oldAllValue = 0
                    val userRef = db.getReference("users/${viewModel.userId}")
                    userRef.child("all").addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            oldAllValue = p0.getValue(Int::class.java) ?: 0
                        }

                    })
                    userRef.child("all").setValue(oldAllValue + 1)
                    navigateToRes()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        val opp = if(viewModel.playerNum == 1) "2" else "1"
        gameRef.child("oppMove").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val i = snapshot.child("$opp/first").getValue(Int::class.java) ?: 0
                val j = snapshot.child("$opp/second").getValue(Int::class.java) ?: 0
                if(!isInit) {
                    if (viewModel.myCells[i][j].isShip) {
                        myField.cells[i][j].state = CellState.HIT
                    } else {
                        myField.cells[i][j].state = CellState.MISS
                    }
                    myField.refreshCanvas()
                } else {
                    isInit = false
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        Log.d(TAG, "Init ended")
    }

    private fun makeMove(i: Int, j: Int) {
        if(viewModel.oppCells[i][j].state != CellState.EMPTY) {
            return
        }

        val playerNum = if(viewModel.playerNum == 1) "oppMove/1" else "oppMove/2"
        gameRef.child(playerNum).setValue(Pair(i, j))

        if(viewModel.oppCells[i][j].isShip) {
            viewModel.oppCells[i][j].state = CellState.HIT
            userField.cells[i][j].state = CellState.HIT
            userField.refreshCanvas()
            if (allCellsDefeated()) {
                viewModel.winnerNum = viewModel.playerNum
                gameRef.child("move").setValue(0)
                var oldAllValue = 0
                var oldWinsValue = 0
                val userRef = db.getReference("users/${viewModel.userId}")
                userRef.child("all").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        oldAllValue = p0.getValue(Int::class.java) ?: 0
                    }

                })
                userRef.child("wins").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        oldWinsValue = p0.getValue(Int::class.java) ?: 0
                    }

                })
                userRef.child("all").setValue(oldAllValue + 1)
                userRef.child("wins").setValue(oldWinsValue + 1)
                navigateToRes()
            }
        } else {
            viewModel.oppCells[i][j].state = CellState.MISS
            userField.cells[i][j].state = CellState.MISS
            userField.refreshCanvas()
            val newMove = if(viewModel.moveNum == 1) 2 else 1
            gameRef.child("move").setValue(newMove)
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

    companion object {
        const val TAG = "GameFragment"
    }
}
