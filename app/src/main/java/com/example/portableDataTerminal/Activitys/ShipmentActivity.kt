package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.children
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseDocumentHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseUserHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.Models.DocumentDataModel
import com.example.portableDataTerminal.Models.UserDataModel
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.Utilies.DocumentLoader
import com.example.portableDataTerminal.databinding.ActivityShipmentBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions
import okhttp3.Credentials
import org.json.JSONArray

/*
 * Класс, содержащий в себе обработку страницы с формированием
 * документе о отгрузке товара
 */
class ShipmentActivity : AppCompatActivity() {

    /*
     * Поле с переменной для доступа к xml-представлению страницы с
     * формированием документа о приёмке товара
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
        binding.sendButton.setOnClickListener { sendData() }
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
     * Метод, добавляющий запись в текущий документ
     */
    @SuppressLint("CommitTransaction")
    private fun addProduct() {
        binding.textView.text = ""

        /*
         * Создаём фрагмент с формой данных для новой записи в документе и
         * добавляем в документ
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
     * Метод, ищущий данные в базе данных по отсканированному штрих-коду
     * и вписывающий данные в созданную форму в документе
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
                     * Получаем уже созданные записи в документе и сверяем их для
                     * проверки совпадения штрих-кода в уже созданных записях в
                     * документе
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
     * Метод, отправляющий документ на веб-сервер
     */
    private fun sendData() {
        val json = "{\"type\" : \"shipment\", \"products\": " + dataToJson().toString() + "}"
        val users: List<UserDataModel> = DatabaseUserHandler(this).viewUsers()
        val url = "http://" + users[0].ip + "/barcodes/hs/products/send_data"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Method.POST,
            url,
            { _ ->
            },
            { _ ->
                errorDialog()
            })
        {
            /*
             * Устанавливаем header запроса для авторизации
             * на стороне веб-сервера
             */
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getHeaders(): MutableMap<String, String> {
                val cred = Credentials.basic(users[0].user_name, users[0].user_password, Charsets.UTF_8)
                val headers = HashMap<String, String>()
                headers["Authorization"] = cred
                return headers
            }

            override fun getBody(): ByteArray {
                return json.toByteArray()
            }
        }

        request.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 30000
            }

            override fun getCurrentRetryCount(): Int {
                return 30000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }

        queue.add(request)
    }

    private fun dataToJson(): JSONArray {
        val productHandler = DatabaseProductHandler(this)
        var result = "["

        binding.linearLayout.children.forEachIndexed { index, it ->

            result += "{" +
                    "\"name\":" + "\"" + it.findViewById<EditText>(R.id.editText_product_name)?.text.toString() + "\"," +
                    "\"description\":" + "\"" + it.findViewById<EditText>(R.id.editText_product_description)?.text.toString() + "\"," +
                    "\"article\":" + "\"" + it.findViewById<EditText>(R.id.editText_product_article)?.text.toString() + "\"," +
                    "\"count\":" + "\"" + it.findViewById<EditText>(R.id.editText_product_count)?.text.toString() + "\"," +
                    "\"barcode\":" + "\"" + it.findViewById<EditText>(R.id.editText_product_barcode)?.text.toString() + "\"," +
                    "\"id\":" + "\"" + productHandler.getProduct(it.findViewById<EditText>(R.id.editText_product_barcode)?.text.toString()).id.toString() + "\""

            result += if (binding.linearLayout.childCount == index+1)
                "}"
            else
                "},"
        }

        result += "]"

        return JSONArray(result)
    }

    /*
     * Метод, очищающий пустые записи в документ
     */
    private fun removeEmpty() {
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

    private fun errorDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Ошибка отправления")
            setMessage("Повторите отправку документа")
            setPositiveButton("Хорошо") { dialog: DialogInterface, which: Int ->
            }
            show()
        }
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

                if (result == DocumentLoader.SUCCESS)
                    Toast.makeText(context, "Сохранение успешно", Toast.LENGTH_LONG).show()
                else if (result == DocumentLoader.ERROR)
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

}