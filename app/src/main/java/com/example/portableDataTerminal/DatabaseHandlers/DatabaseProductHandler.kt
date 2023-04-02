package com.example.portableDataTerminal.DatabaseHandlers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.portableDataTerminal.Models.ProductDataModel

class DatabaseProductHandler(context: Context):  SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 3
        private val DATABASE_NAME = "ProductDatabase"
        private val TABLE_PRODUCTS = "Products"
        private val KEY_ID = "id"
        private val KEY_PRODUCT_ID = "product_id"
        private val KEY_NAME = "name"
        private val KEY_DESCRIPTION = "description"
        private val KEY_ARTICLE = "article"
        private val KEY_BARCODE = "barcode"
        private val KEY_COUNT = "count"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_PRODUCTS ($KEY_ID INTEGER PRIMARY KEY," +
                " $KEY_PRODUCT_ID TEXT, $KEY_NAME TEXT, $KEY_DESCRIPTION TEXT," +
                " $KEY_ARTICLE TEXT, $KEY_BARCODE TEXT, $KEY_COUNT INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS;")
        onCreate(db)
    }

    fun addProduct(product: ProductDataModel):Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_ID, product.id)
        contentValues.put(KEY_PRODUCT_ID, product.product_id)
        contentValues.put(KEY_NAME, product.product_name)
        contentValues.put(KEY_DESCRIPTION, product.product_description)
        contentValues.put(KEY_ARTICLE, product.product_article)
        contentValues.put(KEY_BARCODE, product.product_barcode)
        contentValues.put(KEY_COUNT, product.product_count)

        val success = db.insert(TABLE_PRODUCTS, null, contentValues)

        db.close()

        return success
    }

    @SuppressLint("Range")
    fun viewProducts(): List<ProductDataModel> {
        val product_list: ArrayList<ProductDataModel> = ArrayList()
        val selectQuery = "SELECT  * FROM $TABLE_PRODUCTS"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try{
            cursor = db.rawQuery(selectQuery, null)
        } catch(e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: String
        var product_id: String
        var product_name: String
        var product_description: String
        var product_article: String
        var product_barcode: String
        var product_count: Int

        if(cursor.moveToFirst()) {
            do {
                id = cursor.getString(cursor.getColumnIndex("id"))
                product_id = cursor.getString(cursor.getColumnIndex("product_id"))
                product_name = cursor.getString(cursor.getColumnIndex("name"))
                product_description = cursor.getString(cursor.getColumnIndex("description"))
                product_article = cursor.getString(cursor.getColumnIndex("article"))
                product_barcode = cursor.getString(cursor.getColumnIndex("barcode"))
                product_count = cursor.getInt(cursor.getColumnIndex("count"))

                val product = ProductDataModel(
                    id = id,
                    product_id = product_id,
                    product_name = product_name,
                    product_description = product_description,
                    product_article = product_article,
                    product_barcode = product_barcode,
                    product_count = product_count
                )

                product_list.add(product)
            } while(cursor.moveToNext())
        }

        return product_list
    }
}