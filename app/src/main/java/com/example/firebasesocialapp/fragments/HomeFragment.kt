package com.example.firebasesocialapp.fragments

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var recyclerViewPost: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>
    private lateinit var followingList: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerViewPost = view.findViewById(R.id.recycler_view_post)
        recyclerViewPost.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        recyclerViewPost.layoutManager = linearLayoutManager
        postList = ArrayList()
        followingList = ArrayList()
        postAdapter = PostAdapter(context, postList as ArrayList<Post>)
        recyclerViewPost.adapter = postAdapter

        checkFollowingUsers()
        readPosts()
        return view
    }

    private fun checkFollowingUsers(){
        FirebaseDatabase.getInstance().reference.child("follow").child(FirebaseAuth.getInstance()
            .currentUser!!.uid).child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    followingList.clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.key?.let { followingList.add(it) }
                    }
                    followingList.add(FirebaseAuth.getInstance().currentUser!!.uid)
                    readPosts()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
    private fun readPosts() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                for(snapShot: DataSnapshot in dataSnapshot.children){
                    val post: Post? = snapShot.getValue(Post::class.java)

                    for(id: String in followingList){
                        if (post != null) {
                            if(post.publisher == id){
                                postList.add(post)
                            }
                        }
                    }
                }
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

       FirebaseDatabase.getInstance().reference.child("Posts").addValueEventListener(postListener)
    }

}