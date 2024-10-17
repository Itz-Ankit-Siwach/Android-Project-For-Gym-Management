package com.example.gymmanagementusingsqlite.activity

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.ActivityHomeBinding
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.manager.SessionManager
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var TAG = "HomeActivity"
    var session: SessionManager? = null
    var db: DB? = null
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DB(this)
        session = SessionManager(this)
        setSupportActionBar(binding.homeInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setNavigationItemSelectedListener(this)
        drawer = binding.drawerLayout

        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            binding.homeInclude.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer.addDrawerListener(toggle)
        toggle.syncState()

    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home ->{
                Toast.makeText(this,"Home",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_add -> {
                Toast.makeText(this,"Add",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_fee_panding ->{
                Toast.makeText(this,"Pending",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_update_fee ->{
                Toast.makeText(this,"Update Fee",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_change_password ->{
                Toast.makeText(this,"Change Password",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_log_out ->{
//                session?.setLogin(false)
                Toast.makeText(this,"Logout",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }

        }
        return true
    }

    override fun onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }


}