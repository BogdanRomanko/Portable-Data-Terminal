package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.databinding.ActivityInfoBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions

/*
 * Класс, содержащий в себе обработку страницы для получения
 * информации о товаре
 */
class InfoActivity : AppCompatActivity() {

    /*
     * Поле, хранящее переменную для доступа к xml-представлению страницы
     */
    private lateinit var binding: ActivityInfoBinding

    /*
     * Обработчик события создания страницы
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
         * Устанавливаем переменную binding для доступа
         * к xml-представлению страницы
         */
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
         * Устанавливаем заголовок страницы
         */
        title = "Информация о товаре"

        /*
         * Приязываем обработчики событий для кнопок
         */
        binding.buttonAcceptance.setOnClickListener { getInfo() }
    }

    /*
     * Обработчик события запуска страницы на мобильном телефоне
     */
    override fun onStart() {
        super.onStart()
        lock_editText()
    }

    /*
     * Метод, блокирующий поля в форме с информацией о товаре
     * от изменений
     */
    private fun lock_editText() {
        val fragment = binding.fragmentContainerView.getFragment<InfoFragment>().view

        val editText_product_barcode = fragment?.findViewById<EditText>(R.id.editText_product_barcode)
        val editText_product_name = fragment?.findViewById<EditText>(R.id.editText_product_name)
        val editText_product_description = fragment?.findViewById<EditText>(R.id.editText_product_description)
        val editText_product_count = fragment?.findViewById<EditText>(R.id.editText_product_count)
        val editText_product_article = fragment?.findViewById<EditText>(R.id.editText_product_article)

        val ar = arrayOf(editText_product_name, editText_product_description, editText_product_barcode, editText_product_count, editText_product_article)
        ar.forEach {
            if (it != null) {
                it.isFocusable = false
                it.isClickable = false
                it.isLongClickable = false
            }
        }
    }

    /*
     * Метод, вызывающий сканер штрих-кодов для получения информации о товаре
     */
    private fun getInfo() {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES)
        scanner.initiateScan()
    }

    /*
     * Обработчик события срабатывания сканера
     */
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null) {
            if (result.contents == null)
                Toast.makeText(this, "Отменён", Toast.LENGTH_LONG).show()
            else
                getData(result.contents)
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    /*
     * Метод, ищущий данные в базе данных по отсканированному штрих-коду
     * и вписывающий данные в созданную форму просмотра информации о товаре
     */
    @SuppressLint("SetTextI18n")
    private fun getData(barcode: String) {
        try {
            val products = DatabaseProductHandler(this)

            products.viewProducts().forEach {
                if (it.product_barcode == barcode) {
                    val fragment = binding.fragmentContainerView.getFragment<InfoFragment>().view
                    fragment?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(it.product_barcode)
                    fragment?.findViewById<EditText>(R.id.editText_product_name)?.setText(it.product_name)
                    fragment?.findViewById<EditText>(R.id.editText_product_description)?.setText(it.product_description)
                    fragment?.findViewById<EditText>(R.id.editText_product_count)?.setText(it.product_count)
                    fragment?.findViewById<EditText>(R.id.editText_product_article)?.setText(it.product_article)
                    return
                }
            }
        } catch (_: Exception) {

        }
    }

}