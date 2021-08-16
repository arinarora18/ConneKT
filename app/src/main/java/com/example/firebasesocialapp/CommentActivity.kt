package com.example.firebasesocialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.adapters.CommentAdapter
import com.example.firebasesocialapp.model.Comment
import com.example.firebasesocialapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.ArrayList
import java.util.HashMap

class CommentActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: MutableList<Comment>

    private lateinit var addComment: EditText
    private lateinit var imageProfile: CircleImageView
    private lateinit var post: TextView

    private lateinit var postId: String
    private lateinit var authorId: String

    var fUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Comments")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val intent = intent
        postId = intent.getStringExtra("postId")
        authorId = intent.getStringExtra("authorId")

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList, postId)

        recyclerView.adapter = commentAdapter

        addComment = findViewById(R.id.add_comment)
        imageProfile = findViewById(R.id.image_profile)
        post = findViewById(R.id.post)

        fUser = FirebaseAuth.getInstance().currentUser

        getUserImage()

        post.setOnClickListener {
            if (TextUtils.isEmpty(addComment.text.toString())) {
                Toast.makeText(this@CommentActivity, "No comment added!", Toast.LENGTH_SHORT).show()
            } else {
                putComment()
            }
        }

        getComment()
    }

    private fun getComment() {
        FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    commentList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val comment = snapshot.getValue(Comment::class.java)
                        if (comment != null) {
                            commentList.add(comment)
                        }
                    }
                    commentAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun putComment() {
        val map = HashMap<String, Any?>()
        val ref = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        val id = ref.push().key
        map["id"] = id
        map["comment"] = addComment.text.toString()
        map["publisher"] = fUser!!.uid
        addComment.setText("")
        ref.child(id!!).setValue(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@CommentActivity, "Comment added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@CommentActivity, task.exception!!.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getUserImage() {
        FirebaseDatabase.getInstance().reference.child("Users").child(fUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (user.imageUrl.equals("default")) {
                            imageProfile.setImageResource(R.mipmap.ic_launcher)
                        } else {
                            Picasso.get().load(user.imageUrl).into(imageProfile)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}