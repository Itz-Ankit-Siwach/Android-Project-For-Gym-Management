package com.example.gymmanagementusingsqlite.global

import android.database.Cursor
import android.util.Log
import java.text.SimpleDateFormat

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

        fun returnSQLDataFormat(date:String):String{
            try {
                if (date.trim().isEmpty()){
                    val dateFormat=SimpleDateFormat("dd/MM/yyyy")
                    val firsDate=dateFormat.parse(date)
                    val dateFormat2=SimpleDateFormat("yyyy-MM-dd")
                    return dateFormat2.format(firsDate)
                }
            }catch (e:Exception){
                e.printStackTrace()

            }
            return ""
        }

        fun returnUserDataFormat(date:String):String{
            try {
                if (date.trim().isEmpty()){
                    val dateFormat=SimpleDateFormat("yyyy-MM-dd")
                    val firsDate=dateFormat.parse(date)
                    val dateFormat2=SimpleDateFormat("dd/MM/yyyy")
                    return dateFormat2.format(firsDate)
                }
            }catch (e:Exception){
                e.printStackTrace()

            }
            return ""
        }


    }
}