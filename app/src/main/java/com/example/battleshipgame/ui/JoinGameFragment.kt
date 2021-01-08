package com.example.battleshipgame.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.battleshipgame.R
import com.example.battleshipgame.service.Constants.GAMES
import com.example.battleshipgame.service.Constants.PLAYER2
import com.example.battleshipgame.viewmodels.ViewModel


class JoinGameFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    lateinit var joinGame: Button
    lateinit var gameIdInput: EditText

    private lateinit var db: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join_game, container, false)

        db = FirebaseDatabase.getInstance()

        joinGame = view.findViewById(R.id.btn_join_start)
        gameIdInput = view.findViewById(R.id.game_id_input)
        joinGame.setOnClickListener {
            var gameId = 0
            try {
                gameId = gameIdInput.text.toString().toInt()
            } catch (e: TypeCastException) {
                Toast.makeText(activity, resources.getString(R.string.wrong_id_type), Toast.LENGTH_SHORT).show()
            }

            val gameRef = db.getReference(GAMES + "/${gameId}")
            gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    when {
                        snapshot.exists() -> {
                            gameRef.child(PLAYER2).setValue(viewModel.userId)
                            viewModel.playerNum = 2
                            viewModel.gameId = gameId
                            findNavController().navigate(R.id.action_joinGameFragment_to_allocateFragment)

                        }
                        else -> {
                            Toast.makeText(activity,  resources.getString(R.string.no_such_game), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }

        return view
    }
}
