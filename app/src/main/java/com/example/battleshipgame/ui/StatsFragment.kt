package com.example.battleshipgame.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*

import com.example.battleshipgame.R
import com.example.battleshipgame.viewmodels.GameViewModel


class StatsFragment : Fragment() {

    lateinit var viewModel: GameViewModel

    private lateinit var db: FirebaseDatabase
    private lateinit var statsRef: DatabaseReference

    private lateinit var winsTv: TextView
    private lateinit var totalTv: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProvider(this)[GameViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        db = FirebaseDatabase.getInstance()
        statsRef = db.getReference("users/${viewModel.userId}")
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        winsTv = view.findViewById(R.id.tv_wins)
        totalTv = view.findViewById(R.id.tv_all_games)

        statsRef.child("wins").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                winsTv.text = getString(R.string.stats_wins, num.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })


        statsRef.child("all").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                totalTv.text = getString(R.string.stats_total, num.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

    }

}
