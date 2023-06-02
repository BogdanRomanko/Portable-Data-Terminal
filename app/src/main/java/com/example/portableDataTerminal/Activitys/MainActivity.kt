package com.example.portableDataTerminal.Activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.portableDataTerminal.databinding.ActivityMainBinding

/*
 * Класс, содержащий в себе обработку главной страницы мобильного приложения
 */
class MainActivity : AppCompatActivity() {

    /*
     * Поле с переменной для доступа к xml-представлению главной страницы
     */
    private lateinit var binding: ActivityMainBinding

    /*
     * Обработчик создания главной страницы мобильного приложения
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.syncBtn.setOnClickListener { sync() }
        binding.infoBtn.setOnClickListener { info() }
        binding.choosingDocTypeBtn.setOnClickListener { chooseDocumentType() }

        title = ""
    }

    /*
     * Обработчик вызова страницы с синхронизацией с веб-сервером
     */
    private fun sync() {
        startActivity(Intent(this, SyncActivity::class.java))
    }

    /*
     * Обработчик вызова страницы с получением информации о товаре
     */
    private fun info() {
        startActivity(Intent(this, InfoActivity::class.java))
    }

    /*
     * Обработчик вызова страницы с выбором типа документа
     */
    private fun chooseDocumentType() {
        startActivity(Intent(this, ChoosingDocumentTypeActivity::class.java))
    }

    /*
     * Обработчик вызова страницы о программе
     */
    private fun about() {

    }
}