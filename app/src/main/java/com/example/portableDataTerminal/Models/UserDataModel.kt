package com.example.portableDataTerminal.Models

/*
 * Модель данных для базы данных пользователей
 */
class UserDataModel (
    var user_id: Int,
    var ip: String,
    var user_name: String,
    var user_password: String
)