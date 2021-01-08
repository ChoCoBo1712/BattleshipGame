package com.example.battleshipgame.ui


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import com.example.battleshipgame.R
import com.example.battleshipgame.service.Constants.DEFAULT_IMAGE
import com.example.battleshipgame.service.Constants.EMAIL
import com.example.battleshipgame.service.Constants.GAMES
import com.example.battleshipgame.service.Constants.GOOGLE_SIGN_IN
import com.example.battleshipgame.service.Constants.IMAGE
import com.example.battleshipgame.service.Constants.WINS
import com.example.battleshipgame.viewmodels.ViewModel

class AuthFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInBtn: SignInButton
    private lateinit var viewModel: ViewModel
    private lateinit var db: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        viewModel = requireActivity().run {
            ViewModelProvider(this)[ViewModel::class.java]
        }

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        db = FirebaseDatabase.getInstance()
        usersRef = db.getReference("users")
        auth = FirebaseAuth.getInstance()

        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInBtn = view.findViewById(R.id.btn_sign_in)
        signInBtn.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        goToOptionsFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GOOGLE_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account!!)
                    usersRef.child(viewModel.userId).child(GAMES).setValue(0)
                    usersRef.child(viewModel.userId).child(WINS).setValue(0)
                    usersRef.child(viewModel.userId).child(IMAGE).setValue(DEFAULT_IMAGE)
                    usersRef.child(viewModel.userId).child(EMAIL).setValue(account.email)
                } catch (e: ApiException) {
                    Toast.makeText(requireContext(), resources.getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        goToOptionsFragment()
                    }
                    else -> {
                        Toast.makeText(requireContext(), resources.getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun signIn() {
        val signInIntent =  googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun goToOptionsFragment() {
        val user = auth.currentUser
        if (user != null) {
            viewModel.userId = user.uid
            viewModel.userEmail = user.email.toString()
            findNavController().navigate(R.id.action_authFragment_to_menuFragment)
        }
    }
}
