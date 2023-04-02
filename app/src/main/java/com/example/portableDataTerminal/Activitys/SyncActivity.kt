package com.example.portableDataTerminal.Activitys

import okhttp3.Credentials
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseUserHandler
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.Models.UserDataModel
import com.example.portableDataTerminal.Products
import com.example.portableDataTerminal.databinding.ActivitySyncBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SyncActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySyncBinding
    private var products: Array<Products> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Синхронизация с сервером"

        binding.buttonAcceptance.setOnClickListener { sync() }
    }

    private fun sync() {
        val name = binding.editTextName.text.toString()
        val password = binding.editTextPassword.text.toString()
        val ip = binding.editTextIP.text.toString()

        val userDbHandler = DatabaseUserHandler(this)
        userDbHandler.onUpgrade(userDbHandler.writableDatabase, 3, 3)

        if (name != "" && ip != "") {
            val status = userDbHandler.addUser(UserDataModel(1, ip, name, password))
            if (status > -1) {
                Toast.makeText(
                    applicationContext,
                    "Данные записаны",
                    Toast.LENGTH_LONG).show()
                /*binding.editTextIP.text.clear()
                binding.editTextName.text.clear()
                binding.editTextPassword.text.clear()*/
            } else
                Toast.makeText(
                    applicationContext,
                    "Неправильно заполнены данные! Проверьте ещё раз",
                    Toast.LENGTH_LONG).show()
        } else
            Toast.makeText(
                applicationContext,
                "Неправильно заполнены данные! Проверьте ещё раз",
                Toast.LENGTH_LONG).show()

        getData()
    }

    private fun getData(){
        val userDbHandler = DatabaseUserHandler(this)
        val users: List<UserDataModel> = userDbHandler.viewUsers()

        val url = "http://"+ users[0].ip + "/barcodes/hs/products/get_all_products"
        val queue = Volley.newRequestQueue(this)

        val request = object: StringRequest(
            Method.GET,
            url,
            { result ->
                val dataArray = object : TypeToken<Array<Products>>() {}.type
                products = Gson().fromJson(result, dataArray)
            },
            {
                    error -> Log.d("ERROR", "Error: $error")
            })
        {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getHeaders(): MutableMap<String, String> {
                val cred = Credentials.basic(users[0].user_name, users[0].user_password, Charsets.UTF_8)
                val headers = HashMap<String, String>()
                headers.put("Authorization", cred)
                return headers
                //0JDQtNC80LjQvdC40YHRgtGA0LDRgtC+0YAgKNCk0LXQtNC+0YDQvtCy0JHQnCk6
            }
        }

        queue.add(request)
        if (products.isNotEmpty())
            saveData(products)
    }

    private fun saveData(products: Array<Products>){
        Toast.makeText(this, "Обновление базы данных", Toast.LENGTH_LONG).show()
        val productDbHandler = DatabaseProductHandler(this)
        productDbHandler.onUpgrade(productDbHandler.writableDatabase, 3, 3)
        products.forEachIndexed { index, it ->
            productDbHandler.addProduct(ProductDataModel(
                index.toString(),
                it.id.toString(),
                it.name.toString(),
                it.description.toString(),
                it.article.toString(),
                it.barcode.toString(),
                1))
        }
        Toast.makeText(this, "Обновление базы данных завершено", Toast.LENGTH_LONG).show()
    }

}