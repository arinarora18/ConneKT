package com.example.firebasesocialapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.EditProfileActivity
import com.example.firebasesocialapp.FollowersActivity
import com.example.firebasesocialapp.OptionsActivity
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.adapters.PhotoAdapter
import com.example.firebasesocialapp.model.Post
import com.example.firebasesocialapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    private lateinit var recyclerViewSaves: RecyclerView
    private lateinit var postAdapterSaves: PhotoAdapter
    private lateinit var mySavedPosts: MutableList<Post>

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var myPhotoList: MutableList<Post>

    private lateinit var imageProfile: CircleImageView
    private lateinit var options: ImageView
    private lateinit var followers: TextView
    private lateinit var following: TextView
    private lateinit var posts: TextView
    private lateinit var fullname: TextView
    private lateinit var bio: TextView
    private lateinit var username: TextView

    private lateinit var myPictures: ImageView
    private lateinit var savedPictures: ImageView

    private lateinit var editProfile: Button

    private lateinit var fUser: FirebaseUser

    lateinit var profileId: String
    lateinit var mDatabase: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        fUser = FirebaseAuth.getInstance().currentUser!!
        val data: String =
            context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                ?.getString("profileId", "none")
                .toString()
        if (data == "none") {
            profileId = fUser.uid
        } else {
            profileId = data
            context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
        }

        imageProfile = view.findViewById(R.id.image_profile)
        options = view.findViewById(R.id.options)
        followers = view.findViewById(R.id.followers)
        following = view.findViewById(R.id.following)
        posts = view.findViewById(R.id.posts)
        fullname = view.findViewById(R.id.fullname)
        bio = view.findViewById(R.id.bio)
        username = view.findViewById(R.id.username)
        myPictures = view.findViewById(R.id.my_pictures)
        savedPictures = view.findViewById(R.id.saved_pictures)
        editProfile = view.findViewById(R.id.edit_profile)

        recyclerView = view.findViewById(R.id.recucler_view_pictures)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        myPhotoList = ArrayList()
        photoAdapter = PhotoAdapter(requireContext(), myPhotoList)
        recyclerView.adapter = photoAdapter

        recyclerViewSaves = view.findViewById(R.id.recucler_view_saved)
        recyclerViewSaves.setHasFixedSize(true)
        recyclerViewSaves.layoutManager = GridLayoutManager(context, 3)
        mySavedPosts = ArrayList()
        postAdapterSaves = context?.let { PhotoAdapter(it, mySavedPosts) }!!
        recyclerViewSaves.adapter = postAdapterSaves

        mDatabase = FirebaseDatabase.getInstance()

        userInfo()
        getFollowersAndFollowingCount()
        getPostCount()
        myPhotos()
        getSavedPosts()

        if (profileId == fUser.uid) {
            editProfile.text = "Edit profile"
        } else {
            checkFollowingStatus()
        }

        editProfile.setOnClickListener {
            val btnText: String = editProfile.text.toString()

            if (btnText == "Edit profile") {
                startActivity(Intent(context, EditProfileActivity::class.java))
            } else {
                if (btnText == "follow") {
                    mDatabase.reference.child("follow").child(fUser.uid).child("following").child(profileId).setValue(true)

                    mDatabase.reference.child("follow").child(profileId)
                        .child("followers").child(fUser.uid).setValue(true)
                } else {
                    mDatabase.reference.child("follow").child(fUser.uid)
                        .child("following").child(profileId).removeValue()
                    mDatabase.reference.child("follow").child(profileId)
                        .child("followers").child(fUser.uid).removeValue()
                }
            }
        }

        recyclerView.visibility = View.VISIBLE
        recyclerViewSaves.visibility = View.GONE

        myPictures.setOnClickListener {
            recyclerView.visibility = View.VISIBLE
            recyclerViewSaves.visibility = View.GONE
        }

        savedPictures.setOnClickListener {
            recyclerView.visibility = View.GONE
            recyclerViewSaves.visibility = View.VISIBLE
        }

        followers.setOnClickListener {
            val intent = Intent(context, FollowersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        following.setOnClickListener {
            val intent = Intent(context, FollowersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        options.setOnClickListener {
            startActivity(Intent(context, OptionsActivity::class.java))
        }

        return view;
    }

    private fun getSavedPosts() {
        val savedIds: MutableList<String?> = ArrayList()

        mDatabase.reference.child("Saves").child(fUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        savedIds.add(snapshot.key)
                    }
                    FirebaseDatabase.getInstance().reference.child("Posts")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot1: DataSnapshot) {
                                mySavedPosts.clear()
                                for (snapshot1 in dataSnapshot1.children) {
                                    val post = snapshot1.getValue(Post::class.java)
                                    for (id in savedIds) {
                                        if (post != null) {
                                            if (post.postId == id) {
                                                mySavedPosts.add(post)
                                            }
                                        }
                                    }
                                }
                                postAdapterSaves.notifyDataSetChanged()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun myPhotos(){
        mDatabase.reference.child("Posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    myPhotoList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post != null) {
                            if (post.publisher == profileId) {
                                if (post != null) {
                                    myPhotoList.add(post)
                                }
                            }
                        }
                    }
                    myPhotoList.reverse()
                    photoAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun checkFollowingStatus(){
        mDatabase.reference.child("follow").child(fUser.uid).child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(profileId).exists()) {
                        editProfile.text = "following"
                    } else {
                        editProfile.text = "follow"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getPostCount(){
        mDatabase.reference.child("Posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var counter = 0
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post != null) {
                            if (post.publisher == profileId) counter++
                        }
                    }
                    posts.text = counter.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getFollowersAndFollowingCount(){
        val ref = mDatabase.reference.child("follow").child(profileId)

        ref.child("followers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followers.text = "" + dataSnapshot.childrenCount
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        ref.child("following").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                following.text = "" + dataSnapshot.childrenCount
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun userInfo(){
        mDatabase.reference.child("Users").child(profileId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Picasso.get().load(user.imageUrl).error(R.drawable.ic_person).into(imageProfile)
                    }
                    if (user != null) {
                        username.text = user.username
                    }
                    if (user != null) {
                        fullname.text = user.name
                    }
                    if (user != null) {
                        bio.text = user.bio
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}