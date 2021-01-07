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

import com.example.battleshipgame.R
import com.example.battleshipgame.viewmodels.GameViewModel


class ResultFragment : Fragment() {

    private lateinit var backToMenu: Button
    private lateinit var resText: TextView

    private lateinit var viewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProvider(this)[GameViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backToMenu = view.findViewById(R.id.btn_back_menu)
        resText = view.findViewById(R.id.tv_result)

        if (viewModel.playerNum == viewModel.winnerNum) {
            resText.text = "You're a winner!"
        } else {
            resText.text = "Defeat"
        }

        backToMenu.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_optionsFragment)
        }
    }
}
