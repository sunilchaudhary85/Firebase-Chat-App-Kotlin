package com.example.kotlinchatapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinchatapp.utils.AndroidUtil
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.hbb20.CountryCodePicker

class LoginPhoneNumberActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etConfPass: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    private lateinit var progressBar: ProgressBar
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var tvRedirectLogin: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone_number)

        etEmail = findViewById(R.id.etSEmailAddress)
        etConfPass = findViewById(R.id.etSConfPassword)
        etPass = findViewById(R.id.etSPassword)
        btnSignUp = findViewById(R.id.send_otp_btn)
        progressBar = findViewById(R.id.login_progress_bar)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)
        progressBar.visibility = View.GONE


        tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginOtpActivity::class.java)
            startActivity(intent)
        }
        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val confirmPassword = etConfPass.text.toString().trim()

            // Validation
            if (email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPassword) {
                Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            setInProgress(true)

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                setInProgress(false)

                if (task.isSuccessful) {
                    // Success case
                    Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginUsernameActivity::class.java).apply {
                        putExtra("phone", email)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    // Failure case
                    val errorMsg = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                        is FirebaseAuthUserCollisionException -> "Email already in use"
                        else -> task.exception?.message ?: "Sign up failed"
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    Log.e("SignUpError", "Sign up failed", task.exception)
                }
            }
        }
    }



    private fun setInProgress(inProgress: Boolean) {
        progressBar.visibility = if (inProgress) View.VISIBLE else View.GONE
        btnSignUp.isEnabled = !inProgress
    }
}