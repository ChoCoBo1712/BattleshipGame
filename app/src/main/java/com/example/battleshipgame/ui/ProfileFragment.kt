package com.example.battleshipgame.ui


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.battleshipgame.R
import com.example.battleshipgame.service.CircleTransform
import com.example.battleshipgame.service.Constants.GAMES
import com.example.battleshipgame.service.Constants.GRAVATAR_LEFT
import com.example.battleshipgame.service.Constants.GRAVATAR_RIGHT
import com.example.battleshipgame.service.Constants.IMAGE
import com.example.battleshipgame.service.Constants.PICK_IMAGE_CODE
import com.example.battleshipgame.service.Constants.PROFILE_IMAGES
import com.example.battleshipgame.service.Constants.WINS
import com.example.battleshipgame.service.Gravatar
import com.example.battleshipgame.viewmodels.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private lateinit var winsTv: TextView
    private lateinit var totalTv: TextView
    private lateinit var image: ImageView
    private lateinit var upload: Button
    private lateinit var gravatar: Button
    private lateinit var signOut: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        auth = FirebaseAuth.getInstance()

        db = FirebaseDatabase.getInstance()
        userRef = db.getReference("users/${viewModel.userId}")

        winsTv = view.findViewById(R.id.tv_wins)
        totalTv = view.findViewById(R.id.tv_all_games)
        image = view.findViewById(R.id.profile_image)
        upload = view.findViewById(R.id.image_upload)
        gravatar = view.findViewById(R.id.image_gravatar)
        signOut = view.findViewById(R.id.tv_sign_out)

        signOut.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                findNavController().navigate(R.id.action_profileFragment_to_authFragment)
            }
        }

        gravatar.setOnClickListener {
            val hash: String = Gravatar.md5Hex(viewModel.userEmail)!!
            val uri = GRAVATAR_LEFT + hash + GRAVATAR_RIGHT
            userRef.child(IMAGE).setValue(uri)
        }

        upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "$IMAGE/*"
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, PICK_IMAGE_CODE)
            }
        }

        userRef.child(IMAGE).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val path = snapshot.getValue(String::class.java) ?: return
                Picasso.get().load(path).resize(400, 400).transform(CircleTransform()).into(image)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child(WINS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                winsTv.text = getString(R.string.stats_wins, num.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        userRef.child(GAMES).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                totalTv.text = getString(R.string.stats_total, num.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null && data.data != null) {
                        val inputStream =
                            requireActivity().contentResolver.openInputStream(data.data!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val storage = FirebaseStorage.getInstance()

                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                        val ref = storage.reference
                            .child(PROFILE_IMAGES)
                            .child("${viewModel.userId}.jpeg")

                        ref.putBytes(outputStream.toByteArray()).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                userRef.child(IMAGE).setValue(uri.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}
