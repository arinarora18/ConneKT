package com.example.firebasesocialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class OptionsActivity : AppCompatActivity() {

    private lateinit var logout: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        logout = findViewById(R.id.logout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Options"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(
                    this@OptionsActivity,
                    StartActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }
}