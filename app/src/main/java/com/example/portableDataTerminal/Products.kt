package com.example.portableDataTerminal

class Products {
    var barcode: String? = null
    var id: String? = null
    var name: String? = null
    var article: String? = null
    var description: String? = null

    override fun toString(): String {
        return "Product(barcode=$barcode, id=$id, name=$name, article=$article, description=$description)"
    }
}