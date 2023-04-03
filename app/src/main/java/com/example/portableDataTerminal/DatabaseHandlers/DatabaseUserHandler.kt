package com.example.portableDataTerminal.DatabaseHandlers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.portableDataTerminal.Models.UserDataModel

/*
 * Класс, содержащий в себе методы для работы с базой данной пользователей
 */
class DatabaseUserHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /*
     * Перечисление констрант
     */
    companion object {
        private val DATABASE_VERSION = 3
        private val DATABASE_NAME = "ProductDatabase"
        private val TABLE_USERS = "Users"
        private val KEY_ID = "id"
        private val KEY_IP = "ip"
        private val KEY_NAME = "name"
        private val KEY_PASSWORD = "password"
    }

    /*
     * Обработчик события создания базы данных, создающий
     * новую таблицу с пользователями
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USERS ($KEY_ID INTEGER PRIMARY KEY, $KEY_IP TEXT, $KEY_NAME TEXT, $KEY_PASSWORD TEXT);")
    }

    /*
     * Обработчик события обновления базы данных
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_USERS;")
        onCreate(db)
    }

    /*
     * Метод, добавляющий запись в таблицу с переданными значениями
     */
    fun addUser(user: UserDataModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_ID, user.user_id)
        contentValues.put(KEY_IP, user.ip)
        contentValues.put(KEY_NAME, user.user_name)
        contentValues.put(KEY_PASSWORD, user.user_password)

        val success = db.insert(TABLE_USERS, null, contentValues)

        db.close()

        return success
    }

    /*
     * Метод, возвращающий список пользователей в базе данных
     */
    @SuppressLint("Range")
    fun viewUsers(): List<UserDataModel> {
        val user_list: ArrayList<UserDataModel> = ArrayList()
        val selectQuery = "SELECT  * FROM $TABLE_USERS"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try{
            cursor = db.rawQuery(selectQuery, null)
        } catch(e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var user_id: Int
        var ip: String
        var user_name: String
        var user_password: String

        if(cursor.moveToFirst()) {
            do {
                user_id = cursor.getInt(cursor.getColumnIndex("id"))
                ip = cursor.getString(cursor.getColumnIndex("ip"))
                user_name = cursor.getString(cursor.getColumnIndex("name"))
                user_password = cursor.getString(cursor.getColumnIndex("password"))

                val user = UserDataModel(
                    user_id = user_id,
                    ip = ip,
                    user_name = user_name,
                    user_password = user_password
                )

                user_list.add(user)
            } while(cursor.moveToNext())
        }

        return user_list
    }

    /*
     * Метод, возвращающий ip для подключения к веб-серверу
     */
    @SuppressLint("Recycle", "Range")
    fun getIp(user_name: String): String {
        var cursor: Cursor? = null
        val selectQuery = "SELECT ip FROM $TABLE_USERS WHERE name=$user_name"

        try{
            cursor = this.readableDatabase.rawQuery(selectQuery, null)
        } catch(e: SQLiteException) {
            this.readableDatabase.execSQL(selectQuery)
            return "Error"
        }

        return cursor.getString(cursor.getColumnIndex("ip"))
    }

}