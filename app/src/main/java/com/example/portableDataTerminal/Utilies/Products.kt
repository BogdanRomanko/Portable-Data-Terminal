package com.example.portableDataTerminal.Utilies

/*
 * Класс для формирования временного массива
 * с данными от веб-сервера
 */
class Products {
    var barcode: String? = null
    var id: String? = null
    var name: String? = null
    var article: String? = null
    var description: String? = null
    var count: String? = null

    override fun toString(): String {
        return "Product(barcode=$barcode, id=$id, name=$name, article=$article, description=$description, count=$count)"
    }
}