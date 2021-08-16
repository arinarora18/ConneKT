package com.example.firebasesocialapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.adapters.UserAdapter
import com.example.firebasesocialapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowersActivity : AppCompatActivity() {

    private lateinit var id: String
    private lateinit var title: String
    private lateinit var idList: MutableList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var mUserAdapter: UserAdapter
    private lateinit var mUsers: MutableList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)

        //intent = intent
        id = intent.getStringExtra("id")
        title = intent.getStringExtra("title")

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        mUsers = ArrayList()
        mUserAdapter = UserAdapter(this, mUsers, false)
        recyclerView.adapter = mUserAdapter

        idList = ArrayList()

        when(title){
            "followers"  -> getFollowers()
            "following"  -> getFollowing()
            "likes"  -> getLikes()
        }
    }

    private fun getFollowers(){
        FirebaseDatabase.getInstance().reference.child("follow").child(id).child("followers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    idList.clear()
                    for (snapshot in dataSnapshot.children) {
                        idList.add(snapshot.key!!)
                    }
                    showUsers()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getFollowing(){
        FirebaseDatabase.getInstance().reference.child("follow").child(id).child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    idList.clear()
                    for (snapshot in dataSnapshot.children) {
                        idList.add(snapshot.key!!)
                    }
                    showUsers()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getLikes(){
        FirebaseDatabase.getInstance().reference.child("Likes").child(id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    idList.clear()
                    for (snapshot in dataSnapshot.children) {
                        idList.add(snapshot.key!!)
                    }
                    showUsers()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun showUsers(){
        FirebaseDatabase.getInstance().reference.child("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mUsers.clear()
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        for (id in idList) {
                            if (user != null) {
                                if (user.id == id) {
                                    mUsers.add(user)
                                }
                            }
                        }
                    }
                    Log.d("list f", mUsers.toString())
                    mUserAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}