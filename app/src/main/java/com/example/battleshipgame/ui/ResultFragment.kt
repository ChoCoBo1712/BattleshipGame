package com.example.battleshipgame.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.example.battleshipgame.R
import com.example.battleshipgame.viewmodels.ViewModel


class ResultFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var backToMenu: Button
    private lateinit var resText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        backToMenu = view.findViewById(R.id.btn_back_menu)
        resText = view.findViewById(R.id.tv_result)

        when (viewModel.playerNum) {
            viewModel.winnerNum -> {
                resText.setText(R.string.you_won)
            }
            else -> {
                resText.setText(R.string.defeat)
            }
        }

        backToMenu.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_menuFragment)
        }

        return view
    }
}
