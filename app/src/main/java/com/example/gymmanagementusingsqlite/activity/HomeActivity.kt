package com.example.gymmanagementusingsqlite.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.ActivityHomeBinding
import com.example.gymmanagementusingsqlite.fragment.FragmentAddMember
import com.example.gymmanagementusingsqlite.fragment.FragmentAddUpdateFee
import com.example.gymmanagementusingsqlite.fragment.FragmentAllMembers
import com.example.gymmanagementusingsqlite.fragment.FragmentChangePassword
import com.example.gymmanagementusingsqlite.fragment.FragmentFeePending
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

        val fragment=FragmentAllMembers()
        loadFragment(fragment)

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

        if (item.itemId==R.id.logOutMenu){
            logout()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home ->{
                val fragment=FragmentAllMembers()
                loadFragment(fragment)
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_add -> {
                loadFragment()

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_fee_panding ->{
                val fragment=FragmentFeePending()
                loadFragment(fragment)

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_update_fee ->{
                val fragment=FragmentAddUpdateFee()
                loadFragment(fragment)

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_change_password ->{
                val fragment=FragmentChangePassword()
                loadFragment(fragment)

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }
            R.id.nav_log_out ->{
                logout()

                Toast.makeText(this,"Logout",Toast.LENGTH_LONG).show()
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                }
            }

        }
        return true
    }

    private fun logout(){
        session?.setLogin(false)
        val intent=Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    private fun loadFragment(fragment:Fragment){
        var fragmentManager:FragmentManager?=null
        fragmentManager=supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.frame_container,fragment,"Home").commit()
    }
    private fun loadFragment(){
        val fragment=FragmentAddMember()
        val args=Bundle()
        args.putString("ID","")
        fragment.arguments=args
        val fragmentManager:FragmentManager=supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.frame_container,fragment,"FragmentAdd").commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        try {
            var inflater=menuInflater
            inflater.inflate(R.menu.menu_main,menu)
        }catch (e:Exception){
            e.printStackTrace()
        }

        return super.onCreateOptionsMenu(menu)
    }


}