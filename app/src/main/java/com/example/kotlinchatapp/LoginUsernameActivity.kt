package com.example.kotlinchatapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinchatapp.model.UserModel
import com.example.kotlinchatapp.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject

class LoginUsernameActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var letMeInBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var phoneNumber: String
    private lateinit var printkar: String
    private var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_username)

        usernameInput = findViewById(R.id.login_username)
        letMeInBtn = findViewById(R.id.login_let_me_in_btn)
        progressBar = findViewById(R.id.login_progress_bar)

        phoneNumber = intent.extras?.getString("phone") ?: ""
        System.out.println("value kya hai0" +phoneNumber)
       // System.out.println("value kya hai" +emial)
        getUsername()

        letMeInBtn.setOnClickListener {
            setUsername()
        }
    }

    private fun setUsername() {
        val username = usernameInput.text.toString()
        if (username.isEmpty() || username.length < 3) {
            usernameInput.error = "Username length should be at least 3 chars"
            return
        }

        setInProgress(true)
        userModel = if (userModel != null) {
            userModel!!.apply { this.username = username }
        } else {
            System.out.println("value kya hai1" +phoneNumber)
            UserModel(phoneNumber, username, Timestamp.now(), FirebaseUtil.currentUserId())



        }
        System.out.println("Saving user: phone=${userModel?.phone},${"ye"+phoneNumber}, username=${userModel?.username}, userId=${userModel?.userId}")
      //  System.out.println("value kya hai" + UserModel);
        FirebaseUtil.currentUserDetails().set(userModel!!).addOnCompleteListener { task ->
                setInProgress(false)
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }

    private fun getUsername() {
        setInProgress(true)
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener { task ->
                setInProgress(false)
            if (task.isSuccessful) {
                userModel = task.result?.toObject<UserModel>()
                userModel?.let {
                    usernameInput.setText(it.username)
                }
            }
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        progressBar.visibility = if (inProgress) View.VISIBLE else View.GONE
        letMeInBtn.visibility = if (inProgress) View.GONE else View.VISIBLE
    }
}