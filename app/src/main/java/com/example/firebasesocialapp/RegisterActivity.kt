package com.example.firebasesocialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signup: Button
    private lateinit var login: TextView

    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth

    private lateinit var pb: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        username = findViewById(R.id.username_edit_text)
        name = findViewById(R.id.name_edit_text)
        email = findViewById(R.id.email_edit_text)
        password = findViewById(R.id.password_edit_text)
        signup = findViewById(R.id.sign_up_button)
        login = findViewById(R.id.login_text_view)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        pb = findViewById(R.id.progress_bar)

        login.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        signup.setOnClickListener{
            val txtUsername: String = username.text.toString()
            val txtName: String = name.text.toString()
            val txtEmail: String = email.text.toString()
            val txtPassword: String = password.text.toString()

            if(TextUtils.isEmpty(txtUsername)||TextUtils.isEmpty(txtName)
                ||TextUtils.isEmpty(txtEmail)||TextUtils.isEmpty(txtPassword)){
                Toast.makeText(this, "Empty credentials", Toast.LENGTH_SHORT).show()
            }
            else if(txtPassword.length<6){
                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
            }
            else{
                registerUser(txtUsername, txtName, txtEmail, txtPassword)
            }
        }
    }

    private fun registerUser(username: String, name: String, email: String, password: String){

        pb.visibility = View.VISIBLE
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                val map: HashMap<String, Any> = HashMap()
                map["username"] = username
                map["name"] = name
                map["email"] = email
                map["password"] = password
                map["bio"] = ""
                map["imageUrl"] = "default"
                map["id"] = mAuth.currentUser!!.uid

                mAuth.currentUser?.let { it1 ->

                    mDatabase.reference.child("Users").child(mAuth.currentUser!!.uid).setValue(map).addOnCompleteListener {
                        if (it.isSuccessful) {
                            pb.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Update profile for better experience",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(
                                    this,
                                    MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()
                        } else {
                            pb.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "database creation unsuccessful",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }.addOnFailureListener {
                pb.visibility = View.GONE
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}