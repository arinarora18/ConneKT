package com.example.firebasesocialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {

    private lateinit var iconImage: ImageView
    private lateinit var linearLayout: LinearLayout
    private lateinit var register: Button
    private lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        iconImage = findViewById(R.id.icon_image)
        linearLayout = findViewById(R.id.linear_layout)
        register = findViewById(R.id.register_button)
        login = findViewById(R.id.login_button)

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                                          or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}