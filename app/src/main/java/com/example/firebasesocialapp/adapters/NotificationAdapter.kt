package com.example.firebasesocialapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.fragments.PostDetailFragment
import com.example.firebasesocialapp.fragments.ProfileFragment
import com.example.firebasesocialapp.model.Notification
import com.example.firebasesocialapp.model.Post
import com.example.firebasesocialapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class NotificationAdapter(private var mContext: Context, private val mNotifications: List<Notification> ): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification: Notification = mNotifications[position]
        getUser(holder.imageProfile, holder.username, notification.userid)
        holder.comment.text = notification.text
        if (notification.isPost) {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.postid)
        } else {
            holder.postImage.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            if (notification.isPost) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                    .edit().putString("postId", notification.postid).apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction().replace(R.id.fragment_container, PostDetailFragment())
                    .commit()
            } else {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", notification.userid).apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: ImageView = itemView.findViewById(R.id.image_profile)
        var postImage: ImageView = itemView.findViewById(R.id.post_image)
        var username: TextView = itemView.findViewById(R.id.username)
        var comment: TextView = itemView.findViewById(R.id.comment)

    }

    private fun getPostImage(imageView: ImageView, postid: String) {
        FirebaseDatabase.getInstance().reference.child("Posts").child(postid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val post: Post? = dataSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        Picasso.get().load(post.imageUrl).placeholder(R.mipmap.ic_launcher)
                            .into(imageView)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getUser(imageView: ImageView, textView: TextView, userId: String) {
        FirebaseDatabase.getInstance().reference.child("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (user.imageUrl == "http://i.imgur.com/DvpvklR.png") {
                            imageView.setImageResource(R.mipmap.ic_launcher)
                        } else {
                            if (user != null) {
                                Picasso.get().load(user.imageUrl).into(imageView)
                            }
                        }
                    }
                    if (user != null) {
                        textView.text = user.username
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}