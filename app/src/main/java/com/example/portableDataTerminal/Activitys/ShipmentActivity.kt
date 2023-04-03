package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.databinding.ActivityShipmentBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions

/*
 * Класс, содержащий в себе обработку страницы с формированием
 * отчёта о отгрузке товара
 */
class ShipmentActivity : AppCompatActivity() {

    /*
     * Поле с переменной для доступа к xml-представлению страницы с
     * формированием отчёта о приёмке товара
     */
    private lateinit var binding: ActivityShipmentBinding

    /*
     * Обработчик события создания страницы
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
         * Устанавливаем переменную binding для доступа к
         * xml-представлению страницы
         */
        super.onCreate(savedInstanceState)
        binding = ActivityShipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
         * Устанавливаем заголовок страницы
         */
        title = "Отгрузка товара"

        /*
         * Привязываем обработчики событий к кнопкам
         */
        binding.addButton.setOnClickListener { add_product() }
    }

    /*
    * Обработчик события уничтожения страницы
    */
    override fun onDestroy() {
        super.onDestroy()
        binding.textView.text = R.string.tmp_description.toString()
    }

    /*
     * Обработчик событий для кнопки сканирования нового
     * штрих-кода
     */
    @SuppressLint("CommitTransaction")
    private fun add_product() {
        binding.textView.text = ""

        /*
         * Создаём фрагмент с формой данных для новой записи в отчёте и
         * добавляем в отчёт
         */
        val infoFragment = InfoFragment()
        supportFragmentManager.beginTransaction().add(R.id.linearLayout, infoFragment).commitNow()
        getInfo()
    }

    /*
     * Метод, всоздающий объект сканера штрих-кодов и вызывающий его работу
     */
    fun getInfo() {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES)
        scanner.initiateScan()
    }

    /*
     * Обработчик события для сканера штрих-кодов, который в случае удачного
     * сканирования передаст отсканированный штрих-код в метод обработки данных
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

        removeEmpty()
    }

    /*
     * Метод, ищущий данные в базе данных по отсканированному штрих-коду
     * и вписывающий данные в созданную форму в отчёте
     */
    @SuppressLint("SetTextI18n")
    fun getData(barcode: String) {
        try{
            /*
             * Создаём объект обработчика базы данных Products и ищем совпадение
             * отсканированного штрих-кода с базой данных
             */
            val products = DatabaseProductHandler(this)
            products.viewProducts().forEach {
                if (it.product_barcode == barcode){

                    /*
                     * Получаем уже созданные записи в отчёте и сверяем их для
                     * проверки совпадения штрих-кода в уже созданных записях в
                     * отчёте
                     */
                    val childs: Sequence<View> = binding.linearLayout.children
                    childs.forEach() { child ->
                        /*
                         * Если находим совпадение - просто прибавляем 1 к уже
                         * записанному количеству и выходим из метода
                         */
                        if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == it.product_barcode){
                            val count =  child.findViewById<EditText>(R.id.editText_product_count).text.toString().toInt()
                            child.findViewById<EditText>(R.id.editText_product_count).setText((count + 1).toString())
                            return
                        }
                    }

                    /*
                     * Заполняем форму данными из таблицы
                     */
                    val fragment = binding.linearLayout.getChildAt(binding.linearLayout.childCount-1)
                    fragment?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(it.product_barcode)
                    fragment?.findViewById<EditText>(R.id.editText_product_name)?.setText(it.product_name)
                    fragment?.findViewById<EditText>(R.id.editText_product_description)?.setText(it.product_description)
                    fragment?.findViewById<EditText>(R.id.editText_product_count)?.setText(1.toString())
                    fragment?.findViewById<EditText>(R.id.editText_product_article)?.setText(it.product_article)
                    return
                }
            }
        } catch (_: Exception) {

        }
    }

    /*
     * Метод, отправляющий отчёт на веб-сервер
     */
    private fun sendData() {

    }

    /*
     * Метод, очищающий пустые записи в отчёте
     */
    fun removeEmpty() {
        val childs: Sequence<View> = binding.linearLayout.children
        childs.forEach() {child ->
            if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == "")
                binding.linearLayout.removeView(child)
        }
    }
}