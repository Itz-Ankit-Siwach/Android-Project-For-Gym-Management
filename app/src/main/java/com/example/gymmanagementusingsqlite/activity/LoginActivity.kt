package com.example.gymmanagementusingsqlite.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.ActivityLoginBinding
import com.example.gymmanagementusingsqlite.databinding.ForgotPasswordDialogBinding
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction
import com.example.gymmanagementusingsqlite.manager.SessionManager

class LoginActivity : AppCompatActivity() {
    var db:DB?= null
    var session:SessionManager?=null
    var edtUserName: EditText?=null
    var edtPassword:EditText?=null

    lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db=DB(this)
        session=SessionManager(this)
        edtUserName=binding.edtUserName
        edtPassword=binding.edtPassword


        binding.btnLogin.setOnClickListener {
            if (validateLogin()){
                getLogin()
            }else{
                Toast.makeText(this,"ERROR .....",Toast.LENGTH_LONG).show()
            }

        }

        binding.txtForgotPassword.setOnClickListener{
            showDialog()

        }


    }
    private fun getLogin(){
        try {
            val sqlQuery = "SELECT * FROM ADMIN WHERE USER_NAME='${edtUserName?.text.toString().trim()}' AND PASSWORD='${edtPassword?.text.toString().trim()}' AND ID=1"
            db?.fireQuery(sqlQuery)?.use {
                if (it.count>0){
                    session?.setLogin(true)
                    Toast.makeText(this,"Successfully Log In",Toast.LENGTH_LONG).show()
                    val intent=Intent(this,HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(this,"Log In failed....",Toast.LENGTH_LONG).show()
        }
    }

    private fun validateLogin():Boolean{
        if (edtUserName?.text.toString().trim().isEmpty()){
            Toast.makeText(this,"Enter User Name",Toast.LENGTH_LONG).show()
            return false
        }else if (edtPassword?.text.toString().trim().isEmpty()){
            Toast.makeText(this,"Enter Password",Toast.LENGTH_LONG).show()
            return false
        }
        return true

    }

    private fun showDialog() {
        val binding2 = ForgotPasswordDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this, R.style.AlterDialogCustom)
        dialog.setContentView(binding2.root)
        dialog.setCancelable(false)
        dialog.show()

        binding2.btnForgetSubmit.setOnClickListener {
            val mobile = binding2.edtForgotMobile.text.toString().trim()
            if (mobile.isNotEmpty()) {
                if (validateMobileNumber(mobile)) {
                    checkData(mobile, binding2.txtYourPassword)
                } else {
                    Toast.makeText(this, "Enter a valid 10-digit Mobile Number", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show()
            }
        }

        binding2.imgBackButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun checkData(mobile: String, txtShowPassword: TextView) {
        try {
            val sqlQuery = "SELECT * FROM ADMIN WHERE MOBILE='$mobile'"
            db?.fireQuery(sqlQuery)?.use {
                if (it.count > 0) {
                    val password = MyFunction.getValue(it, "PASSWORD")
                    txtShowPassword.visibility = View.VISIBLE
                    txtShowPassword.text = "Your Password is $password"
                } else {
                    Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show()
                    txtShowPassword.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error occurred while retrieving password", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateMobileNumber(mobile: String): Boolean {

        return mobile.length == 10 && mobile.all { it.isDigit() }
    }


}