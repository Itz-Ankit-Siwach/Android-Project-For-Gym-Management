    package com.example.gymmanagementusingsqlite

    import android.content.Intent
    import android.os.Bundle
    import android.os.Handler
    import android.util.Log
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import com.example.gymmanagementusingsqlite.activity.HomeActivity
    import com.example.gymmanagementusingsqlite.activity.LoginActivity
    import com.example.gymmanagementusingsqlite.databinding.ActivityMainBinding
    import com.example.gymmanagementusingsqlite.global.DB
    import com.example.gymmanagementusingsqlite.manager.SessionManager

    class SplashScreenActivity : AppCompatActivity() {

        private var mDelayHandler: Handler? = null
        private var splashDelay: Long = 3000
        var db: DB? = null
        var session: SessionManager? = null
        private lateinit var binding: ActivityMainBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            db = DB(this)
            session = SessionManager(this)

            insertAdminData()

            mDelayHandler = Handler()
            mDelayHandler?.postDelayed(mRunnable, splashDelay)

        }

        private var mRunnable: Runnable = Runnable {
            if (session?.isLoggedIn==true){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

        private fun insertAdminData() {
            try {
                val sqlCheck = "SELECT * FROM ADMIN"
                db?.fireQuery(sqlCheck)?.use {
                    if (it.count > 0) {
                        Log.d("SplashActivity", "data available")
                    } else {
                        val sqlQuery =
                            "INSERT OR REPLACE INTO ADMIN(ID,USER_NAME,PASSWORD,MOBILE)VALUES('1','Admin','123456','0000000000')"
                        db?.executeQuery(sqlQuery)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            mDelayHandler?.removeCallbacks(mRunnable)
        }

    }