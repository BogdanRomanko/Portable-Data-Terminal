package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseDocumentHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseUserHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.Models.UserDataModel
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.Utilies.DocumentStream
import com.example.portableDataTerminal.Utilies.ServerHelper
import com.example.portableDataTerminal.databinding.ActivityMovementBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONArray
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/*
 * Класс, содержащий в себе обработку страницы с формированием
 * документа о перемещении товаров
 */
class MovementActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMovementBinding
    private lateinit var removeView: View
    private var type = "movement"

    private lateinit var name: String
    private lateinit var firstStore: String
    private lateinit var secondStore: String

    /*
     * Обработчик события создания страницы
     */
    @SuppressLint("AppCompatMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Перемещение товара"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.addButton.setOnClickListener { addProduct() }
        binding.sendButton.setOnClickListener { sendDialog() }
    }

    /*
     * Обработчик возврата обратно
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /*
     * Обработчик события создания меню сохранения/загрузки документа
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.save_load_menu, menu)
        return true
    }

    /*
     * Обработчик нажатия элемента меню в меню сохранения/загрузки
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.save_menu -> saveDialog(EditText(this))
            R.id.load_menu -> loadDialog(DatabaseDocumentHandler(this).viewDocuments())
        }

        return super.onOptionsItemSelected(item)
    }

    /*
     * Обработчик события для сканера штрих-кодов, который в случае удачного
     * сканирования передаст отсканированный штрих-код в метод обработки данных
     */
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null)
                Toast.makeText(this, "Отменён", Toast.LENGTH_LONG).show()
            else
                getData(result.contents)
        } else
            super.onActivityResult(requestCode, resultCode, data)

        removeEmpty()
    }

    /*
     * Метод, добавляющий запись в текущий документ
     */
    @SuppressLint("CommitTransaction")
    private fun addProduct() {
        binding.textView.text = ""

        supportFragmentManager.beginTransaction().add(R.id.linearLayout, InfoFragment()).commitNow()
        getInfo()
    }

    /*
     * Метод, создающий объект сканера штрих-кодов и вызывающий его работу
     */
    private fun getInfo() {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES)
        scanner.initiateScan()
    }

    /*
     * Метод, ищущий данные в базе данных по отсканированному штрих-коду
     * и вписывающий данные в созданную форму в документе
     */
    @SuppressLint("SetTextI18n")
    private fun getData(barcode: String) {
        try {
            val product = DatabaseProductHandler(this).getProduct(barcode)
            if (product.product_barcode == "")
                addProductDialog(barcode)
            else {
                val fragment = binding.linearLayout.getChildAt(binding.linearLayout.childCount - 1)
                fragment?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(product.product_barcode)
                fragment?.findViewById<EditText>(R.id.editText_product_name)?.setText(product.product_name)
                fragment?.findViewById<EditText>(R.id.editText_product_description)?.setText(product.product_description)
                fragment?.findViewById<EditText>(R.id.editText_product_count)?.setText(1.toString())
                fragment?.findViewById<EditText>(R.id.editText_product_article)?.setText(product.product_article)

                fragment?.setOnLongClickListener {
                    popupMenu(fragment)
                    true
                }
            }
        } catch (_: Exception) {
        }
    }

    /*
     * Метод, отправляющий документ на веб-сервер
     */
    @SuppressLint("SimpleDateFormat")
    private fun sendData(password: String) {
        val json = "{\"type\" : \"$type\", " +
                "\"first_store\" : \"$firstStore\", " +
                "\"second_store\" : \"$secondStore\", " +
                "\"name\" : \"$name\", " +
                "\"date\" : \"${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}\", " +
                "\"products\": ${dataToJson()} }"
        val users: List<UserDataModel> = DatabaseUserHandler(this).viewUsers()

        var result = 0

        if (BCrypt.checkpw(users[0].user_password, password))
            result = ServerHelper(users[0].user_name, password, users[0].ip).sendData(json, this)

        if (result == 1)
            errorDialog("Ошибка отправки", "Повторите ошибку отправки документа")
    }

    /*
     * Метод, конвертирующий данные из документа в JSON-формат
     */
    private fun dataToJson(): JSONArray {
        val productHandler = DatabaseProductHandler(this)
        var result = "["

        binding.linearLayout.children.forEachIndexed { index, it ->

            result += "{" +
                    "\"name\" : \"${it.findViewById<EditText>(R.id.editText_product_name)?.text.toString()}\"," +
                    "\"description\" : \"${it.findViewById<EditText>(R.id.editText_product_description)?.text.toString()}\"," +
                    "\"article\" : \"${it.findViewById<EditText>(R.id.editText_product_article)?.text.toString()}\"," +
                    "\"count\" : \"${it.findViewById<EditText>(R.id.editText_product_count)?.text.toString()}\"," +
                    "\"barcode\" : \"${it.findViewById<EditText>(R.id.editText_product_barcode)?.text.toString()}\"," +
                    "\"id\" : \"${productHandler.getProduct(it.findViewById<EditText>(R.id.editText_product_barcode)?.text.toString()).id.toString()}\""

            result += if (binding.linearLayout.childCount == index+1)
                "}"
            else
                "},"
        }

        result += "]"

        return JSONArray(result)
    }

    /*
     * Метод, очищающий пустые записи в документе
     */
    private fun removeEmpty() {
        binding.linearLayout.children.forEach() { child ->
            if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == "")
                binding.linearLayout.removeView(child)
        }
    }

    /*
     * Контекстное меню для записей в документе для их удаления
     */
    private fun popupMenu(view: View) {
        removeView = view

        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.remove_info_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.remove_item -> removeFragmentDialog()
            }

            true
        }

        popup.show()
    }

    /*
     * Контекстное меню для удаления документа из списка доступных
     */
    private fun popupListViewItems(view: View, name: String, dialog: AlertDialog) {
        val popup = PopupMenu(this, view)

        popup.inflate(R.menu.remove_document_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.remove_document -> {
                    var id = 0

                    DatabaseDocumentHandler(this).viewDocuments().forEach {
                        if (it.name == name && it.document_type == type) {
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

    /*
     * Диалоговое окно добавления товара
     */
    private fun addProductDialog(barcode: String) {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Добавление товара")
            setMessage("Совпадений в базе данных штрих-кода \"$barcode\" не обнаружено. Желаете добавить товар вручную?")
            setPositiveButton("Да") { dialog: DialogInterface, which: Int ->
                addProductDBDialog(barcode)
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
     * Диалоговое окно добавления товара в базу данных
     */
    @SuppressLint("InflateParams", "CutPasteId", "ResourceType")
    private fun addProductDBDialog(barcode: String) {
        val view = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val builder = AlertDialog.Builder(this)
        view.findViewById<EditText>(R.id.barcode_editText).setText(barcode)

        with(builder)
        {
            setTitle("Добавление товара")
            setView(view)
            setPositiveButton("Добавить") { dialog: DialogInterface, which: Int ->
                if (view.findViewById<EditText>(R.id.barcode_editText).text.toString().trim().isEmpty() &&
                    view.findViewById<EditText>(R.id.count_editText).text.toString().trim().isEmpty())
                    errorDialog("Ошибка добавления товара", "Проверьте заполненность поля со штрих-кодом или количеством")
                else {
                    DatabaseProductHandler(this@MovementActivity).addProduct(
                        ProductDataModel(
                            (DatabaseProductHandler(this@MovementActivity).viewProducts().size + 1).toString(),
                            view.findViewById<EditText>(R.id.product_id_EditText).text.toString(),
                            view.findViewById<EditText>(R.id.name_editText).text.toString(),
                            view.findViewById<EditText>(R.id.description_editText).text.toString(),
                            view.findViewById<EditText>(R.id.article_editText).text.toString(),
                            view.findViewById<EditText>(R.id.barcode_editText).text.toString(),
                            view.findViewById<EditText>(R.id.count_editText).text.toString().toInt()
                        )
                    )
                }
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
     * Диалоговое окно для удаления записи в документе
     */
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
        }

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
     * Диалоговое окно для удаления документа из списка доступных
     */
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
        }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
     * Диалоговое окно с ошибкой при отправке документа на сервер
     */
    private fun errorDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Хорошо") { dialog: DialogInterface, which: Int ->
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
     * Диалоговое окно для сохранения документа
     */
    private fun saveDialog(editText: EditText) {
        val builder = AlertDialog.Builder(this)

        editText.hint = "Введите название документа"

        with(builder)
        {
            setTitle("Сохранение документа")
            setMessage("Введите название документа")
            setView(editText)
            setPositiveButton("Сохранить") { dialog: DialogInterface, which: Int ->
                val documentStream = DocumentStream()
                val result = documentStream.saveDocument(binding.linearLayout.children, editText.text.toString(), context, type)

                if (result == DocumentStream.SUCCESS)
                    Toast.makeText(context, "Сохранение успешно", Toast.LENGTH_LONG).show()
                else if (result == DocumentStream.ERROR)
                    errorDialog("Ошибка сохранения", "Повторите попытку сохранения")
            }
            setNeutralButton("Отмена") { dialog: DialogInterface, which: Int ->
                Toast.makeText(context, "Сохранение отменено", Toast.LENGTH_LONG).show()
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
     * Диалоговое окно для загрузки документа из списка доступных
     */
    private fun loadDialog(documents: List<DocumentDataModel>) {
        val builder = AlertDialog.Builder(this)
        val items: java.util.ArrayList<String> = arrayListOf()

        documents.forEachIndexed { index, document ->
            if (document.document_type == type)
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

                val documentStream = DocumentStream()
                val fragments = documentStream.loadDocument(id, context)

                fragments.forEach { fragment ->
                    supportFragmentManager.beginTransaction().add(R.id.linearLayout, fragment).commitNow()

                    fragment.view?.setOnLongClickListener {
                        popupMenu(it)
                        true
                    }
                }

                documentStream.loadData(DatabaseDocumentHandler(context).getDocument(id), fragments)
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

        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }

    /*
    * Диалоговое окно для отправки документа
    */
    @SuppressLint("InflateParams", "MissingInflatedId", "CutPasteId")
    private fun sendDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_send_movement_dialog, null)

        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Отправление документа")
            setView(view)
            setPositiveButton("Отправить") { dialog: DialogInterface, which: Int ->
                if (view.findViewById<EditText>(R.id.name_editText).text.toString().trim().isEmpty() ||
                    view.findViewById<EditText>(R.id.first_store_editText).text.toString().trim().isEmpty() ||
                    view.findViewById<EditText>(R.id.first_store_editText).text.toString().trim().isEmpty())
                    errorDialog("Ошибка отправки", "Заполните поля пред отправкой документа")
                else {
                    name = view.findViewById<EditText>(R.id.name_editText).text.toString().trim()
                    firstStore = view.findViewById<EditText>(R.id.first_store_editText).text.toString().trim()
                    secondStore = view.findViewById<EditText>(R.id.first_store_editText).text.toString().trim()
                    sendData(view.findViewById<EditText>(R.id.password_editText).text.toString().trim())
                }
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(R.style.Theme_PortableDataTerminal)
    }
}