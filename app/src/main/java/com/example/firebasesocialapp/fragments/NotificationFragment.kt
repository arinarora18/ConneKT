package com.example.firebasesocialapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasesocialapp.R
import com.example.firebasesocialapp.adapters.NotificationAdapter
import com.example.firebasesocialapp.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationList: MutableList<Notification>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_notification, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationList = ArrayList()
        notificationAdapter = context?.let { NotificationAdapter(it, notificationList) }!!
        recyclerView.adapter = notificationAdapter

        readNotifications();

        return view;
    }

    private fun readNotifications(){
        FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!
                .uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        notificationList.add(snapshot.getValue(Notification::class.java)!!)
                    }
                    notificationList.reverse()
                    notificationAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

    }
}