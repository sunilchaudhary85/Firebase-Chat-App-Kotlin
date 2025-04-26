package com.example.kotlinchatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinchatapp.utils.AndroidUtil
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.*
import java.util.concurrent.TimeUnit

class LoginOtpActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private var timeoutSeconds = 60L
    private lateinit var verificationCode: String
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var otpInput: EditText
    private lateinit var nextBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resendOtpTextView: TextView
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var tvRedirectSignUp: TextView
    lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    lateinit var btnLogin: Button

    // Creating firebaseAuth object
    //lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_otp)
        tvRedirectSignUp = findViewById(R.id.tvRedirectSignUp)
        nextBtn = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etEmailAddress)
        etPass = findViewById(R.id.etPassword)

       // otpInput = findViewById(R.id.login_otp)
       // nextBtn = findViewById(R.id.login_next_btn)
        progressBar = findViewById(R.id.login_progress_bar)
        progressBar.visibility = View.GONE
        resendOtpTextView = findViewById(R.id.resend_otp_textview)

        phoneNumber = intent.extras?.getString("phone") ?: ""

        //sendOtp(phoneNumber, false)

      /*  nextBtn.setOnClickListener {
            val enteredOtp = otpInput.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp)
            signIn(credential)
        }*/

        tvRedirectSignUp.setOnClickListener {
            val intent = Intent(this, LoginPhoneNumberActivity::class.java)
            startActivity(intent)
            // using finish() to end the activity
            finish()
        }

        nextBtn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()
            //val confirmPassword = etConfPass.text.toString().trim()

            // Validation
            if (email.isBlank() || pass.isBlank() ) {
                Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*if (pass != confirmPassword) {
                Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }*/

           // setInProgress(true)

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                setInProgress(false)

                if (task.isSuccessful) {
                    // Success case
                    Toast.makeText(this, "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginUsernameActivity::class.java).apply {
                        putExtra("phone", email)
                      //  intent.putExtra("phone", phoneNumber)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    task.exception?.let { exception ->
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                when {
                                    email.isEmpty() || pass.isEmpty() -> "Please enter both email and password"
                                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
                                    else -> "Wrong email or password"
                                }
                            }
                            is FirebaseAuthInvalidUserException -> "Account doesn't exist"
                            is FirebaseTooManyRequestsException -> "Too many attempts. Try again later"
                            else -> exception.message ?: "Login failed"
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("LoginError", "Login failed: ${exception.javaClass.simpleName}"+errorMessage, exception)
                    }
                }
            }
        }

        resendOtpTextView.setOnClickListener {
            sendOtp(phoneNumber, true)
        }
    }

    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        startResendTimer()
        setInProgress(true)

        val builder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signIn(credential)
                    setInProgress(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("OTP Error", "Verification failed", e)
                    AndroidUtil.showToast(applicationContext, "OTP verification1 failed: ${e.message}")
                    setInProgress(false)
                }

                override fun onCodeSent(
                    s: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(s, token)
                    verificationCode = s
                    resendingToken = token
                    AndroidUtil.showToast(applicationContext, "OTP sent successfully")
                    setInProgress(false)
                }
            })

        if (isResend && resendingToken != null) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken!!).build())
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            nextBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    val intent = Intent(this, LoginUsernameActivity::class.java)
                    intent.putExtra("phone", phoneNumber)
                    startActivity(intent)
                } else {
                    AndroidUtil.showToast(applicationContext, "OTP verification2 failed")
                }
            }
    }

    private fun startResendTimer() {
        resendOtpTextView.isEnabled = false
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    timeoutSeconds--
                    resendOtpTextView.text = "Resend OTP in $timeoutSeconds seconds"
                    if (timeoutSeconds <= 0) {
                        timeoutSeconds = 60L
                        timer.cancel()
                        resendOtpTextView.isEnabled = true
                    }
                }
            }
        }, 0, 1000)
    }
}
