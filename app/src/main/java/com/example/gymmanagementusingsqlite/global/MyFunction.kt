package com.example.gymmanagementusingsqlite.global

import android.database.Cursor
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

class MyFunction {

    companion object{

        fun getValue(cursor: Cursor,columnName:String):String{
            var value:String=""

            try {
                val col=cursor.getColumnIndex(columnName)
                value=cursor.getString(col)
            }catch (e:Exception){
                e.printStackTrace()
                Log.d("MyFunction","getValue ${e.printStackTrace()}")
                value=""
            }
            return value
        }

        fun returnSQLDataFormat(date: String): String {
            return try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val parsedDate = dateFormat.parse(date) // Parse the input date
                val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US) // SQL date format
                sqlDateFormat.format(parsedDate) // Return formatted date
            } catch (e: Exception) {
                e.printStackTrace() // Log any exceptions
                "" // Return empty string on error
            }
        }


        fun returnUserDataFormat(dateString: String): String {
            return try {
                val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val date = originalFormat.parse(dateString)
                targetFormat.format(date!!)
            } catch (e: Exception) {
                dateString
            }
        }


    }
}