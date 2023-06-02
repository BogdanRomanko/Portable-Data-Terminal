package com.example.portableDataTerminal.Activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.portableDataTerminal.databinding.ActivityChoosingDocTypeBinding

/*
 * Класс с обработкой страницы выбора типа документа
 */
class ChoosingDocumentTypeActivity: AppCompatActivity() {

    /*
     * Поле с переменной для доступа к xml-представлению главной страницы
     */
    private lateinit var binding: ActivityChoosingDocTypeBinding

    /*
     * Обработчик создания страницы с выбором типа документа
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChoosingDocTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Выбор типа документа"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.acceptanceBtn.setOnClickListener { acceptance() }
        binding.shipmentBtn.setOnClickListener { shipment() }
        binding.movementBtn.setOnClickListener { movement() }
    }

    /*
     * Обработчик возврата обратно
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /*
     * Обработчик вызова страницы с приёмкой товара
     */
    private fun acceptance() {
        startActivity(Intent(this, AcceptanceActivity::class.java))
    }

    /*
     * Обработчик вызова страницы с отгрузкой товара
     */
    private fun shipment() {
        startActivity(Intent(this, ShipmentActivity::class.java))
    }

    /*
     * Обработчик вызова страницы с перемещением товара
     */
    private fun movement() {
        startActivity(Intent(this, MovementActivity::class.java))
    }

}