package com.example.battleshipgame.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

import com.example.battleshipgame.R
import com.example.battleshipgame.viewmodels.ViewModel

class MenuFragment : Fragment() {

    private lateinit var stats: Button
    private lateinit var startNewGame: Button
    private lateinit var joinGame: Button

    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = requireActivity().run {
            ViewModelProvider(this)[ViewModel::class.java]
        }

        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    override fun onStart() {
        super.onStart()
        viewModel.clear()
    }
}
