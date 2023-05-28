package com.example.portableDataTerminal.DatabaseHandlers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.Utilies.Products
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class DatabaseDocumentHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /*
     * Перечисление констрант
     */
    companion object {
        private val DATABASE_VERSION = 3
        private val DATABASE_NAME = "DocumentDatabase"
        private val TABLE_DOCUMENTS = "Documents"
        private val KEY_ID = "id"
        private val KEY_NAME = "name"
        private val KEY_PRODUCTS = "products"
    }

    /*
     * Обработчик события создания базы данных, создающий
     * новую таблицу с продуктами
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_DOCUMENTS ($KEY_ID INTEGER PRIMARY KEY," +
                    " $KEY_NAME TEXT, $KEY_PRODUCTS TEXT);"
        )
    }

    /*
     * Обработчик события обновления базы данных
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_DOCUMENTS;")
        onCreate(db)
    }

    /*
     * Метод, добавляющий запись в таблицу с переданными значениями
     */
    fun addDocument(document: DocumentDataModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_ID, document.id)
        contentValues.put(KEY_NAME, document.name)
        Log.d("product_list", JSONArray(document.product_list).toString())
        contentValues.put(KEY_PRODUCTS, JSONArray(document.product_list).toString())

        val success = db.insert(TABLE_DOCUMENTS, null, contentValues)

        db.close()

        return success
    }

    /*
     * Метод, возвращающий список продуктов в базе данных
     */
    @SuppressLint("Range")
    fun viewDocuments(): List<DocumentDataModel> {
        val document_list: ArrayList<DocumentDataModel> = ArrayList()
        val selectQuery = "SELECT  * FROM $TABLE_DOCUMENTS"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: String
        var document_id: String
        var document_name: String
        var product_list: String

        if (cursor.moveToFirst()) {
            do {
                document_id = cursor.getString(cursor.getColumnIndex("id"))
                document_name = cursor.getString(cursor.getColumnIndex("name"))
                product_list = cursor.getString(cursor.getColumnIndex("products"))

                val gson = GsonBuilder().create()
                val products = gson.fromJson<ArrayList<Products>>(
                    product_list,
                    object : TypeToken<Array<Products>>() {}.type
                )

                products.forEach {
                    Log.d("products", it.toString())
                }

                val document = DocumentDataModel(
                    id = document_id,
                    name = document_name,
                    product_list = products,
                )

                document_list.add(document)
            } while (cursor.moveToNext())
        }

        return document_list
    }
}