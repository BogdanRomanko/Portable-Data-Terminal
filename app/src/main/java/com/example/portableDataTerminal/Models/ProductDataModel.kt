package com.example.portableDataTerminal.Models

/*
 * Модель данных для базы данных продуктов
 */
class ProductDataModel(
    var id: String?,
    var product_id: String,
    var product_name: String,
    var product_description: String,
    var product_article: String,
    var product_barcode: String,
    var product_count: Int
)