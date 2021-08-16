package com.example.firebasesocialapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.fragments.PostDetailFragment
import com.example.firebasesocialapp.model.Post
import com.squareup.picasso.Picasso

class PhotoAdapter(mContext: Context, mPost: List<Post>) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    private var mPost: List<Post> = mPost
    private var mContext: Context = mContext


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost[position]
        Picasso.get().load(post.imageUrl).placeholder(R.mipmap.ic_launcher).into(holder.postImage)

        holder.postImage.setOnClickListener {
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                .putString("postId", post.postId).apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailFragment()).commit()
        }
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val postImage: ImageView = itemView.findViewById(R.id.post_image)!!
    }

}