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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

import com.example.battleshipgame.R
import com.example.battleshipgame.viewmodels.GameViewModel

class MenuFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var signOutTv: TextView
    private lateinit var statsTv: TextView
    private lateinit var startNewGame: Button
    private lateinit var joinGame: Button

    lateinit var viewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        viewModel = activity?.run {
            ViewModelProvider(this)[GameViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signOutTv = view.findViewById(R.id.tv_sign_out)
        signOutTv.setOnClickListener {
            signOut()
        }

        statsTv = view.findViewById(R.id.tv_game_stat)
        statsTv.setOnClickListener {
            findNavController().navigate(R.id.action_optionsFragment_to_statsFragment)
        }

        startNewGame = view.findViewById(R.id.btn_start_new_game)
        startNewGame.setOnClickListener {
            findNavController().navigate(R.id.action_optionsFragment_to_createGameFragment)
        }

        joinGame = view.findViewById(R.id.btn_join_game)
        joinGame.setOnClickListener {
            findNavController().navigate(R.id.action_optionsFragment_to_joinGameFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.clear()
    }


    private fun signOut() {
        auth.signOut()

        googleSignInClient.signOut().addOnCompleteListener {
            findNavController().navigate(R.id.action_optionsFragment_to_authFragment2)
        }
    }

}
