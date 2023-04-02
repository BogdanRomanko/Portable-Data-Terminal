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

class ShipmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShipmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Отгрузка товара"

        binding.addButton.setOnClickListener { add_product() }
    }

    @SuppressLint("CommitTransaction")
    private fun add_product() {
        binding.textView.text = ""

        val infoFragment = InfoFragment()
        supportFragmentManager.beginTransaction().add(R.id.linearLayout, infoFragment).commitNow()
        getInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.textView.text = R.string.tmp_description.toString()
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

        removeEmpty()
    }

    @SuppressLint("SetTextI18n")
    fun getData(barcode: String) {
        try{
            val products = DatabaseProductHandler(this)
            products.viewProducts().forEach {
                if (it.product_barcode == barcode){

                    val childs: Sequence<View> = binding.linearLayout.children
                    childs.forEach() {child ->
                        if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == it.product_barcode){
                            val count =  child.findViewById<EditText>(R.id.editText_product_count).text.toString().toInt()
                            child.findViewById<EditText>(R.id.editText_product_count).setText((count + (it.product_count + 1)).toString())
                            return
                        }
                    }

                    val fragment_view = binding.linearLayout.getChildAt(binding.linearLayout.childCount-1)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_barcode)?.setText(it.product_barcode)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_name)?.setText(it.product_name)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_description)?.setText(it.product_description)
                    fragment_view?.findViewById<EditText>(R.id.editText_product_count)?.setText((it.product_count + 1).toString())
                    fragment_view?.findViewById<EditText>(R.id.editText_product_article)?.setText(it.product_article)
                    return
                }
            }
        } catch (_: Exception) {
        }
    }

    fun removeEmpty() {
        val childs: Sequence<View> = binding.linearLayout.children
        childs.forEach() {child ->
            if (child.findViewById<EditText>(R.id.editText_product_barcode).text.toString() == "")
                binding.linearLayout.removeView(child)
        }
    }
}