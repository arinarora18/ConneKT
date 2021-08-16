package com.example.firebasesocialapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView
import com.theartofdev.edmodo.cropper.CropImage
import java.util.*
import kotlin.collections.HashMap

class AddPostActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var imageUrl: String

    private lateinit var close: ImageView
    private lateinit var addedImageView: ImageView
    private lateinit var post: TextView

    private lateinit var pb: ProgressBar
    private lateinit var description: SocialAutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        close = findViewById(R.id.close)
        addedImageView = findViewById(R.id.image_added)
        post = findViewById(R.id.post)
        description = findViewById(R.id.description)

        close.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        post.setOnClickListener {
            upload()
        }

        CropImage.activity().start(this@AddPostActivity)
    }

    private fun upload() {
        pb = findViewById(R.id.progress_bar)
        pb.visibility = View.VISIBLE

        if (imageUri != null) {

            val filePath: StorageReference = FirebaseStorage.getInstance().getReference("Posts")
                .child(System.currentTimeMillis().toString() + "." + getFileExtension(imageUri))

            val uploadTask = filePath.putFile(imageUri)
            uploadTask.continueWithTask {
                if (!it.isSuccessful) {
                    throw it.exception!!
                }

                return@continueWithTask filePath.downloadUrl
            }.addOnCompleteListener {
                val downloadUri: Uri? = it.result
                imageUrl = downloadUri.toString()

                val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
                val postId: String? = ref.push().key

                val map: HashMap<String, Object> = HashMap()
                map["postId"] = postId as Object
                map["imageUrl"] = imageUrl as Object
                map["description"] = description.text.toString() as Object
                map["publisher"] = FirebaseAuth.getInstance().currentUser?.uid as Object

                ref.child(postId).setValue(map)

                pb.visibility = View.GONE

                Toast.makeText(this, "uploaded", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this, "No image was selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(this.contentResolver.getType(uri))
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            addedImageView.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@AddPostActivity, MainActivity::class.java))
            finish()
        }
    }
}