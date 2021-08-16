package com.example.firebasesocialapp.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.MainActivity
import com.example.firebasesocialapp.fragments.ProfileFragment
import com.example.firebasesocialapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.HashMap

class UserAdapter(private val mContext: Context, private val mUser: List<User>, private val isFragment: Boolean) :
    Adapter<UserAdapter.ViewHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val user: User = mUser[position]
        holder.btnfollow.visibility = View.VISIBLE

        holder.username.text = user.username
        holder.fullname.text = user.name

        Picasso.get().load(user.imageUrl).placeholder(R.mipmap.ic_launcher)
            .into(holder.imageProfile)

        val userId:String = user.id
        isFollowed(userId, holder.btnfollow)

        if (userId == firebaseUser.uid) {
            holder.btnfollow.visibility = View.GONE
        }

        holder.btnfollow.setOnClickListener {
            if (holder.btnfollow.text.toString() == "follow") {
                FirebaseDatabase.getInstance().reference.child("follow").child(firebaseUser.uid)
                    .child("following").child(user.id).setValue(true)

                FirebaseDatabase.getInstance().reference.child("follow").child(user.id)
                    .child("followers").child(firebaseUser.uid).setValue(true)

                addNotification(user.id)
            } else {
                FirebaseDatabase.getInstance().reference.child("follow").child(firebaseUser.uid)
                    .child("following").child(user.id).removeValue()

                FirebaseDatabase.getInstance().reference.child("follow").child(user.id)
                    .child("followers").child(firebaseUser.uid).removeValue()
            }
        }

        holder.itemView.setOnClickListener {
            if (isFragment) {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                    .putString("profileId", user.id).apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ProfileFragment()
                ).commit()
            }
            else run {
                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("publisherId", user.id)
                mContext.startActivity(intent)
            }
        }
    }

    private fun isFollowed(id: String, btnFollow: Button) {

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(id).exists())
                    btnFollow.text = "following"
                else btnFollow.text = "follow"

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("follow").child(firebaseUser.uid)
                .child("following")
        databaseReference.addValueEventListener(valueEventListener)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: CircleImageView = itemView.findViewById(R.id.image_profile)
        val username: TextView = itemView.findViewById(R.id.username)
        val fullname: TextView = itemView.findViewById(R.id.fullname)
        val btnfollow: Button = itemView.findViewById(R.id.btn_follow)

    }

    private fun addNotification(userId: String) {
        val map = HashMap<String, Any>()
        map["userid"] = userId
        map["text"] = "started following you."
        map["postId"] = ""
        map["isPost"] = false
        FirebaseDatabase.getInstance().reference.child("Notifications").child(firebaseUser.uid)
            .push().setValue(map)
    }

}