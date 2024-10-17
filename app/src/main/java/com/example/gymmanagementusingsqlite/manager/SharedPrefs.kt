package com.example.gymmanagementusingsqlite.manager

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs {
    private val TAG=SharedPrefs::class.java.simpleName

    companion object{
        var user_no="user_no"
        var user_name="user_name"
        var user_mobile="user_mobile"
        var user_type="user_type"

        private val SHARED_PREFS_FILE_NAME="MyData"
        private fun getPrefs(context:Context):SharedPreferences{
            return context.getSharedPreferences(SHARED_PREFS_FILE_NAME,Context.MODE_PRIVATE)
        }
        fun save(context: Context,key:String,value:String){
            getPrefs(context).edit().putString(key,value).apply()
        }
        fun getString(context: Context,key:String):String?{
            return getPrefs(context).getString(key,"")
        }
        fun clearSharedPref(context: Context){
            getPrefs(context).edit().clear().apply()
        }
    }
}