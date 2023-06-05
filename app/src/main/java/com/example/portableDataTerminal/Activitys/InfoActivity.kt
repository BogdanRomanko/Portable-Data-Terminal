package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.Models.ProductDataModel
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
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Информация о товаре"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonAcceptance.setOnClickListener { getInfo() }
    }

    /*
     * Обработчик для кнопки возврата
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /*
     * Обработчик события запуска страницы
     */
    override fun onStart() {
        super.onStart()
        lockEditText()
    }

    /*
     * Метод, блокирующий поля в форме с информацией о товаре
     * от изменений
     */
    private fun lockEditText() {
        val fragment = binding.fragmentContainerView.getFragment<InfoFragment>().view

        val editTextProductBarcode = fragment?.findViewById<EditText>(R.id.editText_product_barcode)
        val editTextProductName = fragment?.findViewById<EditText>(R.id.editText_product_name)
        val editTextProductDescription = fragment?.findViewById<EditText>(R.id.editText_product_description)
        val editTextProductCount = fragment?.findViewById<EditText>(R.id.editText_product_count)
        val editTextProductArticle = fragment?.findViewById<EditText>(R.id.editText_product_article)

        val editTexts = arrayOf(
            editTextProductName,
            editTextProductDescription,
            editTextProductBarcode,
            editTextProductCount,
            editTextProductArticle
        )

        editTexts.forEach {
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
    @Deprecated("Deprecated in Java")
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
        try{
            val product = DatabaseProductHandler(this).getProduct(barcode)

            if (product.product_barcode == "")
                addProductDialog(barcode)
            else {
                val fragment = binding.fragmentContainerView.getFragment<InfoFragment>().view
                fragment?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(product.product_barcode)
                fragment?.findViewById<EditText>(R.id.editText_product_name)?.setText(product.product_name)
                fragment?.findViewById<EditText>(R.id.editText_product_description)?.setText(product.product_description)
                fragment?.findViewById<EditText>(R.id.editText_product_count)?.setText(product.product_count)
                fragment?.findViewById<EditText>(R.id.editText_product_article)?.setText(product.product_article)
                return
            }
        } catch (_: Exception) {
        }
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
                    DatabaseProductHandler(this@InfoActivity).addProduct(
                        ProductDataModel(
                        (DatabaseProductHandler(this@InfoActivity).viewProducts().size + 1).toString(),
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

}