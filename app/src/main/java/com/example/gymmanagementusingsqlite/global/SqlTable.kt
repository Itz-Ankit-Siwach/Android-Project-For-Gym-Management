package com.example.gymmanagementusingsqlite.global

object SqlTable {
    const val admin="CREATE TABLE ADMIN(ID INTEGER PRIMARY KEY AUTOINCREMENT,USER_NAME TEXT DEFAULT ''," +
            "PASSWORD TEXT DEFAULT '',MOBILE TEXT DEFAULT '')"

    const val member="CREATE TABLE MEMBER(ID INTEGER PRIMARY KEY AUTOINCREMENT,FIRST_NAME TEXT DEFAULT ''," +
            "LAST_NAME TEXT DEFAULT '',GENDER TEXT DEFAULT '',AGE INTEGER DEFAULT '',WEIGHT TEXT DEFAULT ''," +
            "MOBILE TEXT DEFAULT '',ADDRESS TEXT DEFAULT '',DATE_OF_JOINING TEXT DEFAULT ''," +
            "MEMBERSHIP TEXT DEFAULT '',EXPIRE_ON TEXT DEFAULT ''," +
            "DISCOUNT TEXT DEFAULT '',TOTAL TEXT DEFAULT '',IMAGE_PATH TEXT DEFAULT '',STATUS TEXT DEFAULT 'A')"

    const val fee="CREATE TABLE FEE(ID INTEGER PRIMARY KEY AUTOINCREMENT,ONE_MONTH TEXT DEFAULT '',THREE_MONTH TEXT DEFAULT '',SIX_MONTH TEXT DEFAULT '',ONE_YEAR TEXT DEFAULT '',THREE_YEAR TEXT DEFAULT '')"
}