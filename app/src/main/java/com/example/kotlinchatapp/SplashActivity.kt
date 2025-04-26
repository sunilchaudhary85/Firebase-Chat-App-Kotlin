package com.example.kotlinchatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinchatapp.utils.FirebaseUtil

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (intent.extras != null) {
            // From notification
            val userId = intent.extras?.getString("userId")
            println("yha tak aya1")

            Handler().postDelayed({
                    println("yha tak aya9")
            if (FirebaseUtil.isLoggedIn()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                println("yha tak aya10")
            } else {
                println("yha tak aya11")
                startActivity(Intent(this@SplashActivity, LoginPhoneNumberActivity::class.java))
                println("yha tak aya12")
            }
            println("yha tak aya13")
            finish()
            println("yha tak aya14")
            }, 1000)

            println("yha tak aya7")
        } else {
            println("yha tak aya8")
            Handler().postDelayed({
                    println("yha tak aya9")
            if (FirebaseUtil.isLoggedIn()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                println("yha tak aya10")
            } else {
                println("yha tak aya11")
                startActivity(Intent(this@SplashActivity, LoginPhoneNumberActivity::class.java))
                println("yha tak aya12")
            }
            println("yha tak aya13")
            finish()
            println("yha tak aya14")
            }, 1000)
        }
        println("yha tak aya15")
    }
}