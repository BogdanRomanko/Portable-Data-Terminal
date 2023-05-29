package com.example.portableDataTerminal.Utilies

import android.content.Context
import android.view.View
import android.widget.EditText
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseDocumentHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.R

class DocumentLoader {

    val SUCCESS = 1
    val ERROR = 0

    fun saveDocument(fragments: Sequence<View>, name: String, context: Context): Int {
        val databaseDocumentHandler = DatabaseDocumentHandler(context)
        val products: ArrayList<ProductDataModel> = arrayListOf()
        val id = databaseDocumentHandler.viewDocuments().size + 1

        try {
            fragments.forEach {
                val product = ProductDataModel(
                    "0",
                    "0",
                    it.findViewById<EditText>(R.id.editText_product_name)?.text.toString(),
                    it.findViewById<EditText>(R.id.editText_product_description)?.text.toString(),
                    it.findViewById<EditText>(R.id.editText_product_article)?.text.toString(),
                    it.findViewById<EditText>(R.id.editText_product_barcode)?.text.toString(),
                    it.findViewById<EditText>(R.id.editText_product_count).text.toString().toInt()
                )

                products.add(product)
            }

            databaseDocumentHandler.addDocument(DocumentDataModel(id.toString(), name, products))

            return SUCCESS
        } catch (_: Exception) {
            return ERROR
        }

    }

    fun loadDocument() {

    }

}