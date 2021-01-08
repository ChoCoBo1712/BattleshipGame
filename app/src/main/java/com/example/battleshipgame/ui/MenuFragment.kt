package com.example.battleshipgame.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

import com.example.battleshipgame.R
import com.example.battleshipgame.viewmodels.ViewModel

class MenuFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var stats: Button
    private lateinit var startNewGame: Button
    private lateinit var joinGame: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        stats = view.findViewById(R.id.tv_game_stat)
        stats.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_profileFragment)
        }

        startNewGame = view.findViewById(R.id.btn_start_new_game)
        startNewGame.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_createGameFragment)
        }

        joinGame = view.findViewById(R.id.btn_join_game)
        joinGame.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_joinGameFragment)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        viewModel.clear()
    }
}
