package com.example.gymmanagementusingsqlite.fragment

import android.database.DatabaseUtils
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.FragmentChangePasswordBinding
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction

class FragmentChangePassword : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
    private var db: DB? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Change Password"

        db = activity?.let { DB(it) }

        fillOldMobile()

        binding.btnChangePassword.setOnClickListener {
            val newPassword = binding.edtNewPassword.text.toString().trim()
            val confirmPassword = binding.edtConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty()) {
                showToast("Please Enter New Password")
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }

            // Update password in the database
            val sqlQuery = "UPDATE ADMIN SET PASSWORD=${DatabaseUtils.sqlEscapeString(newPassword)} WHERE ID='1'" // Change '1' to the actual admin ID
            db?.fireQuery(sqlQuery)
            showToast("Password Changed Successfully")
        }

        binding.btnChangeMobile.setOnClickListener {
            val newMobile = binding.edtNewNumber.text.toString().trim() // Use the correct EditText here

            if (newMobile.isEmpty()) {
                showToast("Please Enter New Mobile Number")
                return@setOnClickListener
            }

            // Update mobile number in the database
            val sqlQuery = "UPDATE ADMIN SET MOBILE=${DatabaseUtils.sqlEscapeString(newMobile)} WHERE ID='1'" // Change '1' to the actual admin ID
            db?.fireQuery(sqlQuery)
            showToast("Mobile Number Changed Successfully")
            fillOldMobile() // Refresh the old mobile number to reflect changes
        }
    }

    private fun fillOldMobile() {
        try {
            val sqlQuery = "SELECT MOBILE FROM ADMIN WHERE ID='1'" // Change '1' to the actual admin ID
            db?.fireQuery(sqlQuery)?.use {
                val mobile = MyFunction.getValue(it, "MOBILE")
                if (mobile.isNotEmpty()) {
                    binding.edtOldNumber.setText(mobile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error retrieving mobile number")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}
