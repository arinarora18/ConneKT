package com.example.firebasesocialapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.CommentActivity
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.fragments.ProfileFragment
import com.example.firebasesocialapp.model.Post
import com.example.firebasesocialapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*

class PostAdapter(context: Context?, arrayList: ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>(){

    private val context = context
    private val mpost = arrayList

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mpost[position]
        Picasso.get().load(post.imageUrl).into(holder.postImage)
        holder.description?.text = post.description

        FirebaseDatabase.getInstance().reference.child("Users").child(post.publisher)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user?.imageUrl.equals("default")) {
                        holder.imageProfile.setImageResource(R.mipmap.ic_launcher)
                    } else {
                        if (user != null) {
                            Picasso.get().load(user.imageUrl).placeholder(R.mipmap.ic_launcher)
                                .into(holder.imageProfile)
                        }
                    }
                    holder.username?.text = user?.username
                    holder.author?.text = user?.name
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        isLiked(post.postId, holder.like)
        holder.noOfLikes?.let { noOfLikes(post.postId, it) }
        holder.noOfComments?.let { getComments(post.postId, it) }
        isSaved(post.postId, holder.save)

        holder.like.setOnClickListener {
            if (holder.like.tag == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(post.postId).child(firebaseUser.uid).setValue(true)
                addNotification(post.postId, post.publisher)
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(post.postId).child(firebaseUser.uid).removeValue()
            }
        }

        holder.comment.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", post.postId)
            intent.putExtra("authorId", post.publisher)
            context?.startActivity(intent)
        }

        holder.noOfComments!!.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", post.postId)
            intent.putExtra("authorId", post.publisher)
            context?.startActivity(intent)
        }

        holder.save.setOnClickListener {
            if (holder.save.tag == "save") {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser.uid).child(post.postId).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser.uid).child(post.postId).removeValue()
            }
        }

        holder.imageProfile.setOnClickListener {
            context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                ?.edit()?.putString("profileId", post.publisher)?.apply()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.username!!.setOnClickListener {
            context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                ?.edit()?.putString("profileId", post.publisher)?.apply()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.author!!.setOnClickListener {
            context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                ?.edit()?.putString("profileId", post.publisher)?.apply()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

//        holder.postImage.setOnClickListener {
//            context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
//                ?.putString("postId", post.postId)?.apply()
//            (context as FragmentActivity).supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, PostDetailFragment()).commit()
//        }

    }

    override fun getItemCount(): Int {
        return mpost.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: ImageView = itemView.findViewById(R.id.image_profile)
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
        val like: ImageView = itemView.findViewById(R.id.like)
        val comment: ImageView = itemView.findViewById(R.id.comment)
        val save: ImageView = itemView.findViewById(R.id.save)

        val username:TextView? = itemView.findViewById(R.id.username)
        val noOfLikes:TextView? = itemView.findViewById(R.id.no_of_likes)
        val author:TextView? = itemView.findViewById(R.id.author)
        val noOfComments:TextView? = itemView.findViewById(R.id.no_of_comments)
        val description:TextView? = itemView.findViewById(R.id.description)
    }

    private fun isSaved(postId: String, image: ImageView) {
        FirebaseDatabase.getInstance().reference.child("Saves").child(
            FirebaseAuth.getInstance().currentUser!!.uid
        ).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(postId).exists()) {
                    image.setImageResource(R.drawable.ic_save_black)
                    image.tag = "saved"
                } else {
                    image.setImageResource(R.drawable.ic_save)
                    image.tag = "save"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun isLiked(postId: String, imageView: ImageView) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(firebaseUser.uid).exists()) {
                        imageView.setImageResource(R.drawable.ic_liked)
                        imageView.tag = "liked"
                    } else {
                        imageView.setImageResource(R.drawable.ic_like)
                        imageView.tag = "like"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun noOfLikes(postId: String, text: TextView) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    text.text = dataSnapshot.childrenCount.toString() + " likes"
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getComments(postId: String, text: TextView) {
        FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    text.text = "View All " + dataSnapshot.childrenCount + " Comments"
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun addNotification(postId: String, publisherId: String) {
        val map = HashMap<String, Any>()
        map["userid"] = publisherId
        map["text"] = "liked your post."
        map["postId"] = postId
        map["isPost"] = true
        FirebaseDatabase.getInstance().reference.child("Notifications").child(firebaseUser.uid)
            .push().setValue(map)
    }

}