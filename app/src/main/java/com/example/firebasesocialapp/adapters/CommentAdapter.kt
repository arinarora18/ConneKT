package com.example.firebasesocialapp.adapters

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.MainActivity
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.model.Comment
import com.example.firebasesocialapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private var mContext: Context, private var mComments: List<Comment>, private val postId: String): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val comment: Comment = mComments!![position]
        holder.comment.text = comment.comment
        FirebaseDatabase.getInstance().reference.child("Users").child(comment.publisher)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        holder.username.text = user.username
                    }
                    if (user != null) {
                        if (user.imageUrl == "default") {
                            holder.imageProfile.setImageResource(R.mipmap.ic_launcher)
                        } else {
                            Picasso.get().load(user.imageUrl).into(holder.imageProfile)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        holder.comment.setOnClickListener {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra("publisherId", comment.publisher)
            mContext!!.startActivity(intent)
        }
        holder.imageProfile.setOnClickListener {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra("publisherId", comment.publisher)
            mContext!!.startActivity(intent)
        }
        holder.itemView.setOnLongClickListener {
            if (comment.publisher.endsWith(fUser!!.uid)) {
                val alertDialog = AlertDialog.Builder(
                    mContext!!).create()
                alertDialog.setTitle("Do you want to delete?")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO"
                ) { dialog, _ -> dialog.dismiss() }
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES"
                ) { dialog, _ ->
                    FirebaseDatabase.getInstance().reference.child("Comments")
                        .child(postId!!).child(comment.id).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(mContext,
                                    "Comment deleted successfully!",
                                    Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }
                }
                alertDialog.show()
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return mComments!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: CircleImageView = itemView.findViewById(R.id.image_profile)
        var username: TextView = itemView.findViewById(R.id.username)
        var comment: TextView = itemView.findViewById(R.id.comment)

    }
}