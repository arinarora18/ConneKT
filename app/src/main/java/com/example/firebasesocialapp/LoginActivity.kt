package com.example.firebasesocialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var signup: TextView

    private lateinit var pb: ProgressBar

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email_edit_text)
        password = findViewById(R.id.password_edit_text)
        login = findViewById(R.id.login_in_button)
        signup = findViewById(R.id.sign_up_textview)

        mAuth = FirebaseAuth.getInstance()

        signup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        login.setOnClickListener{
            val txtEmail = email.text.toString()
            val txtPassword = password.text.toString()

            if(TextUtils.isEmpty(txtPassword) || TextUtils.isEmpty(txtEmail)){
                Toast.makeText(this, "Empty credentials", Toast.LENGTH_SHORT)
            }
            else{
                loginUser(txtEmail, txtPassword)
            }
        }
    }

    private fun loginUser(email: String, password: String){

        pb = findViewById(R.id.progress_bar)
        pb.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
            if(it.isSuccessful){
                pb.visibility = View.GONE
                Toast.makeText(this, "Update profile for better experience", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                finish()
            }
        }.addOnFailureListener{
            pb.visibility = View.GONE
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}