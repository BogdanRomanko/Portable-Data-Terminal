package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseDocumentHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.Utilies.DocumentLoader
import com.example.portableDataTerminal.databinding.ActivityAcceptanceBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions

/*
 * Класс, содержащий в себе обработку страницы с формированием
 * отчёта о приёмке товара
 */
class AcceptanceActivity : AppCompatActivity() {

    /*
     * Поле с переменной для доступа к xml-представлению страницы с
     * формированием отчёта о приёмке товара
     */
    private lateinit var binding: ActivityAcceptanceBinding


    private lateinit var remove_view: View
    private lateinit var editText_SaveName: EditText

    /*
     * Обработчик события создания страницы
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
         * Устанавливаем переменную binding для доступа к
         * xml-представлению страницы
         */
        super.onCreate(savedInstanceState)
        binding = ActivityAcceptanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
         * Устанавливаем заголовок страницы
         */
        title = "Приёмка товара"

        /*
         * Привязываем обработчики событий к кнопкам
         */
        binding.addButton.setOnClickListener { add_product() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.save_load_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId)
        {
            R.id.save_menu  -> run {
                editText_SaveName = EditText(this)
                saveDialog(editText_SaveName)
            }
            R.id.load_menu  ->
            {

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveDialog(editText: EditText) {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Сохранение документа")
            setMessage("Сохранить документ?")
            setView(editText)
            setPositiveButton("Да", saveDocument)
            setNeutralButton("Отмена", cancelSaveDocument)
            show()
        }
    }

    private val saveDocument = { dialog: DialogInterface, which: Int ->
        val documentLoader = DocumentLoader()
        val result = documentLoader.saveDocument(binding.linearLayout.children, editText_SaveName.text.toString(), this)

        if (result == documentLoader.SUCCESS)
            Toast.makeText(this, "Сохранение успешно", Toast.LENGTH_LONG).show()
        else if (result == documentLoader.ERROR)
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_LONG).show()
    }

    private val cancelSaveDocument = { dialog: DialogInterface, which: Int ->
        Toast.makeText(this, "Сохранение отменено", Toast.LENGTH_LONG).show()
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
    private fun getInfo() {
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
            if (result.contents == null) {
                Toast.makeText(this, "Отменён", Toast.LENGTH_LONG).show()
            }
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
    private fun getData(barcode: String) {
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
                            child.findViewById<EditText>(R.id.editText_product_count).setText((count +  1).toString())
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

                    fragment?.setOnLongClickListener {
                        popupMenu(fragment)
                        true
                    }
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
    private fun removeEmpty() {
        val childs: Sequence<View> = binding.linearLayout.children
        childs.forEach() { child ->
            if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == "")
                binding.linearLayout.removeView(child)
        }
    }

    private fun popupMenu(view: View) {
        remove_view = view

        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.remove_info)

        popup.setOnMenuItemClickListener {
                item: MenuItem? ->

            when (item!!.itemId) {
                R.id.remove_item -> {
                    removeDialog()
                }
            }

            true
        }

        popup.show()
    }

    private fun removeDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Удаление фрагмента")
            setMessage("Удалить фрагмент?")
            setPositiveButton("Да", removeFragment)
            setNeutralButton("Отмена", cancelRemoveFragment)
            show()
        }
    }

    private val removeFragment = { dialog: DialogInterface, which: Int ->
        binding.linearLayout.removeView(remove_view)
    }

    private val cancelRemoveFragment = { dialog: DialogInterface, which: Int ->
    }

}