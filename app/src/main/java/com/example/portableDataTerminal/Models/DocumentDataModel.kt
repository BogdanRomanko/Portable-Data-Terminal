package com.example.portableDataTerminal.Models

/*
 * Модель данных для базы данных документов
 */
class DocumentDataModel (
    var id: String?,
    var name: String?,
    var product_list: ArrayList<ProductDataModel>,
    var document_type: String?
)