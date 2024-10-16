package com.example.gymmanagementusingsqlite

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gymmanagementusingsqlite.activity.LoginActivity

class SplashScreenActivity : AppCompatActivity() {

    private var mDelayHandler: Handler?=null
    private var splashDelay:Long=3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        
        mDelayHandler=Handler()
        mDelayHandler?.postDelayed(mRunnable,splashDelay)

    }
    private var mRunnable:Runnable= Runnable {
        val intent= Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}