package com.example.battleshipgame.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import com.example.battleshipgame.R
import com.example.battleshipgame.service.Game
import com.example.battleshipgame.viewmodels.GameViewModel
import kotlin.random.Random


class CreateGameFragment : Fragment() {

    private lateinit var startGame: Button
    private lateinit var gameIdText: TextView

    private lateinit var viewModel: GameViewModel
    private lateinit var db: FirebaseDatabase
    private lateinit var gamesRef: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProvider(this)[GameViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        db = FirebaseDatabase.getInstance()
        gamesRef = db.getReference("games")

        return inflater.inflate(R.layout.fragment_create_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startGame = view.findViewById(R.id.btn_start_game)
        gameIdText = view.findViewById(R.id.tv_game_id)

        startGame.setOnClickListener {
            viewModel.playerNum = 1
            findNavController().navigate(R.id.action_createGameFragment_to_allocateFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        val gameId = Random.nextInt(1, 1000000)
        gameIdText.text = "$gameId"
        viewModel.gameId = gameId
        val game = Game(player1 = viewModel.userId)
        viewModel.game = game
        gamesRef.child("$gameId").setValue(game)
    }

}
