package com.example.portableDataTerminal.DatabaseHandlers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.Models.ProductDataModel

class DatabaseDocumentHandler(private val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /*
     * Перечисление констрант
     */
    companion object {
        private val DATABASE_VERSION = 6
        private val DATABASE_NAME = "DocumentDatabase"
        private val TABLE_DOCUMENTS = "Documents"
        private val KEY_ID = "id"
        private val KEY_NAME = "name"
        private val KEY_PRODUCTS = "products"
        private val KEY_TYPE = "type"
    }

    /*
     * Обработчик события создания базы данных, создающий
     * новую таблицу с документами
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_DOCUMENTS ($KEY_ID INTEGER PRIMARY KEY," +
                    " $KEY_NAME TEXT, $KEY_PRODUCTS TEXT, $KEY_TYPE TEXT);"
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
        var stringProducts = ""

        if (checkName(document.name)) {
            db!!.execSQL("DELETE FROM $TABLE_DOCUMENTS WHERE $KEY_NAME = '${document.name}'")
        }

        contentValues.put(KEY_ID, document.id)
        contentValues.put(KEY_NAME, document.name)

        document.product_list.forEach {
            stringProducts += "${it.product_barcode}, ${it.product_count} |"
        }

        stringProducts = stringProducts.substring(0, stringProducts.length-1)

        contentValues.put(KEY_PRODUCTS, stringProducts)
        contentValues.put(KEY_TYPE, document.document_type)

        val success = db.insert(TABLE_DOCUMENTS, null, contentValues)

        db.close()

        return success
    }

    /*
     * Метод, возвращающий список документов в базе данных
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

        var document_id: String
        var document_name: String
        var product_list: String
        var document_type: String

        if (cursor.moveToFirst()) {
            do {
                document_id = cursor.getString(cursor.getColumnIndex("id"))
                document_name = cursor.getString(cursor.getColumnIndex("name"))
                product_list = cursor.getString(cursor.getColumnIndex("products"))
                document_type = cursor.getString(cursor.getColumnIndex("type"))

                val databaseProductHandler = DatabaseProductHandler(context)
                val allProducts = databaseProductHandler.viewProducts()
                val products = arrayListOf<ProductDataModel>()

                val all = product_list.split("|")

                all.forEach { item ->
                    val product = item.split(",")

                    allProducts.forEach {
                        if (product[0].trim() == it.product_barcode) {
                            products.add(ProductDataModel(
                                it.id,
                                it.product_id,
                                it.product_name,
                                it.product_description,
                                it.product_article,
                                it.product_barcode,
                                product[1].trim().toInt()
                            ))
                        }
                    }
                }

                val document = DocumentDataModel(
                    id = document_id,
                    name = document_name,
                    product_list = products,
                    document_type = document_type
                )

                document_list.add(document)
            } while (cursor.moveToNext())
        }

        return document_list
    }

    fun getDocument(id: Int): DocumentDataModel {
       viewDocuments().forEach { document ->
           if (document.id == id.toString())
               return document
       }
        return DocumentDataModel("0", "!!null!!", arrayListOf(), "!!null!!")
    }

    private fun checkName(name: String?): Boolean {
        val list = viewDocuments()

        list.forEach {
            if (it.name == name)
                return true
        }

        return false
    }
}