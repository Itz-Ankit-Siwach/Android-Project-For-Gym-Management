package com.example.gymmanagementusingsqlite.manager

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Log

class SessionManager(private var context:Context) {
    private var pref:SharedPreferences=context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
    private var editor:Editor=pref.edit()

    internal var PRIVATE_MODE=0;
    val isLoggedIn:Boolean
        get() = pref.getBoolean(KEY_IS_LOGGED_IN,false)

    fun setLogin(isLoggedIn:Boolean){
        editor.putBoolean(KEY_IS_LOGGED_IN,isLoggedIn)
        //commiting the change
        editor.commit()

        Log.d(TAG,"User login session modified")
    }


    companion object{
        private val TAG=SessionManager::class.java.simpleName

        //Shared Preference File Name
        private val PREF_NAME="Login"
        var KEY_USER_ID="user_id"
        private val KEY_IS_LOGGED_IN="isLoggedIn"
    }
}