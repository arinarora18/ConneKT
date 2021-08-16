package com.example.firebasesocialapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.adapters.PostAdapter
import com.example.firebasesocialapp.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailFragment : Fragment() {

    private lateinit var postId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: ArrayList<Post>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_post_detail, container, false)
        postId =
            context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.getString("postId", "none")
                .toString()
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        postList = ArrayList()
        postAdapter = PostAdapter(context, postList)
        recyclerView.adapter = postAdapter

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                dataSnapshot.getValue(Post::class.java)?.let { postList.add(it) }
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        FirebaseDatabase.getInstance().reference.child("Post").child(postId).addValueEventListener(postListener)

        return view
    }

}