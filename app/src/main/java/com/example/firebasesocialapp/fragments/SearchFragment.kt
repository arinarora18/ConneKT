package com.example.firebasesocialapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.adapters.UserAdapter
import com.example.firebasesocialapp.model.User
import com.google.firebase.database.*
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView

class SearchFragment : Fragment() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mUser: ArrayList<User>
    private lateinit var userAdapter: UserAdapter

    private lateinit var searchBar: SocialAutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        mRecyclerView = view.findViewById(R.id.recycler_view_users)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser, true) }!!
        mRecyclerView.adapter = userAdapter

        searchBar = view.findViewById(R.id.search_bar)

        readUser()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUser(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }

        searchBar.addTextChangedListener(textWatcher)
        return view
    }

    private fun readUser() {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (TextUtils.isEmpty(searchBar.text.toString())) {
                    mUser.clear()
                    for (dataSnapShot: DataSnapshot in snapshot.children) run {
                        val user: User? = dataSnapShot.getValue(User::class.java)
                        if (user != null) {
                            mUser.add(user)
                        }
                    }
                    userAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }

        }
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.addValueEventListener(valueEventListener)
    }

    private fun searchUser(s: String) {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUser.clear()
                for (dataSnapshot: DataSnapshot in snapshot.children) run {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser.add(user)
                    }
                    userAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        }

        val query: Query =
            FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username")
                .startAt(s).endAt(s + "\uf8ff")

        query.addValueEventListener(valueEventListener)
    }
}