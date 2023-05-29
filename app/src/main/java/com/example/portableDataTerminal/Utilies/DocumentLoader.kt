package com.example.portableDataTerminal.Utilies

import android.content.Context
import android.view.View
import android.widget.EditText
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseDocumentHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.R

class DocumentLoader {

    val SUCCESS = 1
    val ERROR = 0

    fun saveDocument(fragments: Sequence<View>, name: String, context: Context, type: String): Int {
        val databaseDocumentHandler = DatabaseDocumentHandler(context)
        val products: ArrayList<ProductDataModel> = arrayListOf()
        val id = databaseDocumentHandler.viewDocuments().lastIndex + 1

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

            databaseDocumentHandler.addDocument(DocumentDataModel(id.toString(), name, products, type))

            return SUCCESS
        } catch (_: Exception) {
            return ERROR
        }

    }

    fun loadDocument(id: Int, context: Context): ArrayList<InfoFragment> {
        val fragments: ArrayList<InfoFragment> = arrayListOf()
        val documents = DatabaseDocumentHandler(context)
        val document = documents.getDocument(id)

        if (document.id == "0" && document.name == "!!null!!" && document.product_list.isEmpty() && document.document_type == "!!null!!")
            return fragments

        document.product_list.forEach { _ ->
            fragments.add(InfoFragment())
        }

        return fragments
    }

    fun loadData(document: DocumentDataModel, fragments: ArrayList<InfoFragment>) {
        document.product_list.forEachIndexed { index, product ->
            val fragment = fragments[index]

            fragment.view?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(product.product_barcode)
            fragment.view?.findViewById<EditText>(R.id.editText_product_name)?.setText(product.product_name)
            fragment.view?.findViewById<EditText>(R.id.editText_product_description)?.setText(product.product_description)
            fragment.view?.findViewById<EditText>(R.id.editText_product_count)?.setText(product.product_count.toString())
            fragment.view?.findViewById<EditText>(R.id.editText_product_article)?.setText(product.product_article)
        }
    }

}