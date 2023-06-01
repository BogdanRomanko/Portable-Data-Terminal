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

        binding.buttonSync.setOnClickListener { sync() }
        binding.buttonInfo.setOnClickListener { info() }
        binding.buttonAcceptance.setOnClickListener { acceptance() }
        binding.buttonShipment.setOnClickListener { shipment() }

        title = ""
    }

    /*
     * Обработчик кнопки для вызова страницы с синхронизацией
     * с веб-сервером
     */
    private fun sync() {
        startActivity(Intent(this, SyncActivity::class.java))
    }

    /*
     * Обработчик кнопки для вызова страницы с получением информации
     * о товаре
     */
    private fun info() {
        startActivity(Intent(this, InfoActivity::class.java))
    }

    /*
     * Обработчик кнопки для вызова страницы с созданием отчёта
     * приёмки товара
     */
    private fun acceptance() {
        startActivity(Intent(this, AcceptanceActivity::class.java))
    }

    /*
     * Обработчик кнопки для вызова страницы с созданием отчёта
     * отгрузки товара
     */
    private fun shipment() {
        startActivity(Intent(this, ShipmentActivity::class.java))
    }
}