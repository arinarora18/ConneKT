package com.example.firebasesocialapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.firebasesocialapp.fragments.HomeFragment
import com.example.firebasesocialapp.fragments.NotificationFragment
import com.example.firebasesocialapp.fragments.ProfileFragment
import com.example.firebasesocialapp.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = HomeFragment()

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> fragment = HomeFragment()

                R.id.nav_add -> {

                    startActivity(Intent(this@MainActivity, AddPostActivity::class.java))

                }

                R.id.nav_search -> fragment = SearchFragment()

                R.id.nav_profile -> fragment = ProfileFragment()

                R.id.nav_heart -> fragment = NotificationFragment()
            }

            if(fragment!=null){
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            }
            return@setOnNavigationItemSelectedListener true
        }

        val intent = intent.extras
        if (intent != null) {
            val profileId = intent.getString("publisherId")
            getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId)
                .apply()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
            bottomNav.selectedItemId = R.id.nav_profile
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
        }
    }
}