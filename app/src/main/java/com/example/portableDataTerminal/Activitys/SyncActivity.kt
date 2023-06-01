package com.example.portableDataTerminal.Activitys

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseUserHandler
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.Models.UserDataModel
import com.example.portableDataTerminal.Utilies.Products
import com.example.portableDataTerminal.databinding.ActivitySyncBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.Credentials
import kotlin.math.roundToInt


/*
 * Класс, содержащий в себе обработку страницы с
 * синхронизацией с веб-сервером
 */
class SyncActivity : AppCompatActivity() {

    /*
     * Поля класса, отвечающие за доступ к xml-представлению
     * страницы и временный массив для данных с сервера
     */
    private lateinit var binding: ActivitySyncBinding
    private var products: Array<Products> = arrayOf()

    /*
     * Обработчик события создания страницы
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Синхронизация с сервером"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonAcceptance.setOnClickListener { sync() }
    }

    /*
     * Обработчик нажатия кнопки возврата
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /*
     * Метод, сохраняющий данные, введённые пользователем, в
     * базу данных и вызывающий получение данных с сервера
     */
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

    /*
     * Метод, посылающий веб-серверу get-запрос на получение
     * данных о товарах
     */
    private fun getData() {
        val users: List<UserDataModel> = DatabaseUserHandler(this).viewUsers()

        val url = "http://" + users[0].ip + "/barcodes/hs/products/get_all_products"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Method.GET,
            url,
            { result ->
                val dataArray = object : TypeToken<Array<Products>>() {}.type
                products = Gson().fromJson(result, dataArray)
            },
            { error ->
                Log.d("ERROR", "Error: $error")
            }) {
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
                //0JDQtNC80LjQvdC40YHRgtGA0LDRgtC+0YAgKNCk0LXQtNC+0YDQvtCy0JHQnCk6
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

        if (products.isNotEmpty())
            saveData(products)
    }

    /*
     * Метод, сохраняющий данные из временной переменной
     * products в базу данных
     */
    private fun saveData(products: Array<Products>){
        Toast.makeText(this, "Обновление базы данных", Toast.LENGTH_LONG).show()

        val productDbHandler = DatabaseProductHandler(this)
        productDbHandler.onUpgrade(productDbHandler.writableDatabase, 3, 3)

        products.forEachIndexed { index, it ->

            val count = it.count!!.toDouble()

            productDbHandler.addProduct(ProductDataModel(
                index.toString(),
                it.id.toString(),
                it.name.toString(),
                it.description.toString(),
                it.article.toString(),
                it.barcode.toString(),
                count.roundToInt()
            ))
        }

        Toast.makeText(this, "Обновление базы данных завершено", Toast.LENGTH_LONG).show()
    }
}