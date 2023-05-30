package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.children
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseDocumentHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.Utilies.DocumentLoader
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

    private lateinit var removeView: View

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
        binding.addButton.setOnClickListener { addProduct() }
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
                saveDialog(EditText(this))
            }
            R.id.load_menu  ->
            {
                loadDialog(DatabaseDocumentHandler(this).viewDocuments())
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveDialog(editText: EditText) {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Сохранение документа")
            setMessage("Введите название документа")
            setView(editText)
            setPositiveButton("Сохранить") { dialog: DialogInterface, which: Int ->
                val documentLoader = DocumentLoader()
                val result = documentLoader.saveDocument(
                    binding.linearLayout.children,
                    editText.text.toString(),
                    context,
                    "shipment"
                )

                if (result == documentLoader.SUCCESS)
                    Toast.makeText(context, "Сохранение успешно", Toast.LENGTH_LONG).show()
                else if (result == documentLoader.ERROR)
                    Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_LONG).show()
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
                Toast.makeText(context, "Сохранение отменено", Toast.LENGTH_LONG).show()
            }
            show()
        }
    }

    private fun loadDialog(documents: List<DocumentDataModel>) {
        val builder = AlertDialog.Builder(this)
        val items: ArrayList<String> = arrayListOf()


        documents.forEachIndexed { index, document ->
            if (document.document_type == "shipment")
                items.add(document.name.toString())
        }

        val docs: Array<String> = Array(items.size) { "" }
        items.forEachIndexed { index, item ->
            docs[index] = item
        }

        with(builder)
        {
            setTitle("Загрузка документа")
            setItems(docs) { dialog, which ->
                var id = 0

                documents.forEach {
                    if (it.name == docs[which])
                        id = it.id.toString().toInt()
                }

                val documentLoader = DocumentLoader()
                val fragments = documentLoader.loadDocument(id, context)

                fragments.forEach { fragment ->
                    supportFragmentManager.beginTransaction().add(R.id.linearLayout, fragment)
                        .commitNow()

                    fragment.view?.setOnLongClickListener {
                        popupMenu(it)
                        true
                    }
                }

                documentLoader.loadData(DatabaseDocumentHandler(context).getDocument(id), fragments)
                binding.textView.text = ""
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
                Toast.makeText(context, "Загрузка отменена", Toast.LENGTH_LONG).show()
            }
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val lv: ListView = dialog.listView
            lv.setOnItemLongClickListener { parent, view, position, id ->
                popupListViewItems(view, docs[position], dialog)
                true
            }
        }

        dialog.show()
    }

    /*
     * Обработчик событий для кнопки сканирования нового
     * штрих-кода
     */
    @SuppressLint("CommitTransaction")
    private fun addProduct() {
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
    fun removeEmpty() {
        val childs: Sequence<View> = binding.linearLayout.children
        childs.forEach() {child ->
            if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == "")
                binding.linearLayout.removeView(child)
        }
    }

    private fun popupMenu(view: View) {
        removeView = view

        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.remove_info_menu)

        popup.setOnMenuItemClickListener {
                item: MenuItem? ->

            when (item!!.itemId) {
                R.id.remove_item -> {
                    removeFragmentDialog()
                }
            }

            true
        }

        popup.show()
    }

    private fun popupListViewItems(view: View, name: String, dialog: AlertDialog) {
        val popup = PopupMenu(this, view)

        popup.inflate(R.menu.remove_document_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.remove_document -> {
                    var id = 0

                    DatabaseDocumentHandler(this).viewDocuments().forEach {
                        if (it.name == name && it.document_type == "shipment") {
                            id = it.id?.toInt()!!
                            return@forEach
                        }
                    }

                    removeDocumentDialog(id, dialog)
                }
            }

            true
        }

        popup.show()
    }

    private fun removeFragmentDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Удаление фрагмента")
            setMessage("Удалить фрагмент?")
            setPositiveButton("Да") { dialog: DialogInterface, which: Int ->
                binding.linearLayout.removeView(removeView)
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
            }
            show()
        }
    }

    private fun removeDocumentDialog(id: Int, alertDialog: AlertDialog) {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Удаление документа")
            setMessage("Удалить документ?")
            setPositiveButton("Да") { dialog: DialogInterface, which: Int ->
                DatabaseDocumentHandler(context).deleteDocument(id)
                alertDialog.cancel()
                loadDialog(DatabaseDocumentHandler(context).viewDocuments())
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
            }
            show()
        }
    }
}