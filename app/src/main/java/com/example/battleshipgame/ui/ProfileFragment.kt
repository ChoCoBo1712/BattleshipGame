package com.example.battleshipgame.ui


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.battleshipgame.Constants.Companion.PICK_IMAGE_CODE
import com.google.firebase.database.*

import com.example.battleshipgame.R
import com.example.battleshipgame.service.CircleTransform
import com.example.battleshipgame.viewmodels.ViewModel
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {

    lateinit var viewModel: ViewModel

    private lateinit var db: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private lateinit var winsTv: TextView
    private lateinit var totalTv: TextView
    private lateinit var image: ImageView
    private lateinit var upload: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = requireActivity().run {
            ViewModelProvider(this)[ViewModel::class.java]
        }

        db = FirebaseDatabase.getInstance()
        userRef = db.getReference("users/${viewModel.userId}")

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        winsTv = view.findViewById(R.id.tv_wins)
        totalTv = view.findViewById(R.id.tv_all_games)
        image = view.findViewById(R.id.profile_image)
        upload = view.findViewById(R.id.image_upload)

        upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, PICK_IMAGE_CODE)
            }
        }

        userRef.child("image").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val path = snapshot.getValue(String::class.java) ?: return
                Picasso.get().load(path).resize(400, 400).transform(CircleTransform()).into(image)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        userRef.child("wins").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                winsTv.text = getString(R.string.stats_wins, num.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })


        userRef.child("all").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val num = snapshot.getValue(Int::class.java) ?: return
                totalTv.text = getString(R.string.stats_total, num.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null && data.data != null) {
                        val inputStream = requireActivity().contentResolver.openInputStream(data.data!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val storage = FirebaseStorage.getInstance()

                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                        val ref = storage.reference
                            .child("profileImages")
                            .child("${viewModel.userId}.jpeg")

                        ref.putBytes(outputStream.toByteArray())
                            .addOnSuccessListener {
                                ref.downloadUrl
                                    .addOnSuccessListener { uri ->
                                        userRef.child("image").setValue(uri.toString())
                                    }
                            }
                    }
                }
            }
        }


    }

}
