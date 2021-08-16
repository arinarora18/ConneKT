package com.example.firebasesocialapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasesocialapp.fragments.ProfileFragment
import com.example.firebasesocialapp.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var close: ImageView
    private lateinit var imageProfile: CircleImageView
    private lateinit var save: TextView
    private lateinit var changePhoto: TextView
    private lateinit var fullName: EditText
    private lateinit var username: EditText
    private lateinit var bio: EditText
    private lateinit var fUser: FirebaseUser
    private lateinit var imageUri: Uri
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        close = findViewById(R.id.close)
        imageProfile = findViewById(R.id.image_profile)
        save = findViewById(R.id.save)
        changePhoto = findViewById(R.id.change_photo)
        fullName = findViewById(R.id.fullname)
        username = findViewById(R.id.username)
        bio = findViewById(R.id.bio)

        fUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance().reference.child("Uploads")

        FirebaseDatabase.getInstance().reference.child("Users").child(fUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        fullName.setText(user.name)
                    }
                    if (user != null) {
                        username.setText(user.username)
                    }
                    if (user != null) {
                        bio.setText(user.bio)
                    }
                    if (user != null) {
                        Picasso.get().load(user.imageUrl).into(imageProfile)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        close.setOnClickListener { finish() }

        changePhoto.setOnClickListener {
            CropImage.activity().setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }

        imageProfile.setOnClickListener {
            CropImage.activity().setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }

        save.setOnClickListener {
            updateProfile()
            startActivity(Intent(this@EditProfileActivity, ProfileFragment::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun updateProfile() {
        val map = HashMap<String, Any>()
        map["fullname"] = fullName.text.toString()
        map["username"] = username.text.toString()
        map["bio"] = bio.text.toString()
        FirebaseDatabase.getInstance().reference.child("Users").child(fUser.uid).updateChildren(map)
    }

    private fun uploadImage() {
        val pd = ProgressDialog(this)
        pd.setMessage("Uploading")
        pd.show()
        if (imageUri != null) {
            val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".jpeg")
            val uploadTask = fileRef.putFile(imageUri)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                    if(!it.isSuccessful){
                        it.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val url = downloadUri.toString()
                    FirebaseDatabase.getInstance().reference.child("Users").child(fUser.uid)
                        .child("imageUrl").setValue(url)
                    pd.dismiss()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Upload failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            uploadImage()
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

}