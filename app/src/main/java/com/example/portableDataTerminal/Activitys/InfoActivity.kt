package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.Fragments.InfoFragment
import com.example.portableDataTerminal.R
import com.example.portableDataTerminal.databinding.ActivityInfoBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Информация о товаре"

        binding.buttonAcceptance.setOnClickListener { getInfo() }
    }

    private fun lock_editText() {
        val fragment_view = binding.fragmentContainerView.getFragment<InfoFragment>().get_view()
        val editText_product_barcode = fragment_view?.findViewById<EditText>(R.id.editText_product_barcode)
        val editText_product_name = fragment_view?.findViewById<EditText>(R.id.editText_product_name)
        val editText_product_description = fragment_view?.findViewById<EditText>(R.id.editText_product_description)
        val editText_product_count = fragment_view?.findViewById<EditText>(R.id.editText_product_count)
        val editText_product_article = fragment_view?.findViewById<EditText>(R.id.editText_product_article)

        val ar = arrayOf(editText_product_name, editText_product_description, editText_product_barcode, editText_product_count, editText_product_article)
        ar.forEach {
            if (it != null) {
                it.isFocusable = false
                it.isClickable = false
                it.isLongClickable = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lock_editText()
    }

    fun getInfo() {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES)
        scanner.initiateScan()
    }

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

    @SuppressLint("SetTextI18n")
    fun getData(barcode: String) {
        try{
            val products = DatabaseProductHandler(this)
            products.viewProducts().forEach {
                if (it.product_barcode == barcode){
                    val fragment_view = binding.fragmentContainerView.getFragment<InfoFragment>().get_view()
                    fragment_view?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(it.product_barcode)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_name)?.setText(it.product_name)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_description)?.setText(it.product_description)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_count)?.setText(it.product_count)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_article)?.setText(it.product_article)
                    return
                }
            }
        } catch (_: Exception) {

        }
    }

}