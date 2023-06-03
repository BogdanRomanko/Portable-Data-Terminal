package com.example.portableDataTerminal.Activitys

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseProductHandler
import com.example.portableDataTerminal.DatabaseHandlers.DatabaseUserHandler
import com.example.portableDataTerminal.Models.ProductDataModel
import com.example.portableDataTerminal.Models.UserDataModel
import com.example.portableDataTerminal.Utilies.Products
import com.example.portableDataTerminal.Utilies.ServerHelper
import com.example.portableDataTerminal.databinding.ActivitySyncBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.mindrot.jbcrypt.BCrypt
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

        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        val userDbHandler = DatabaseUserHandler(this)
        userDbHandler.onUpgrade(userDbHandler.writableDatabase, 3, 3)

        if (name != "" && ip != "") {
            val status = userDbHandler.addUser(UserDataModel(1, ip, name, passwordHash))
            if (status > -1) {
                Toast.makeText(applicationContext, "Данные записаны", Toast.LENGTH_LONG).show()
                /*binding.editTextIP.text.clear()
                binding.editTextName.text.clear()
                binding.editTextPassword.text.clear()*/
            } else
                Toast.makeText(applicationContext, "Неправильно заполнены данные! Проверьте ещё раз", Toast.LENGTH_LONG).show()
        } else
            Toast.makeText(applicationContext, "Неправильно заполнены данные! Проверьте ещё раз", Toast.LENGTH_LONG).show()

        getData(password)
    }

    /*
     * Метод, посылающий веб-серверу get-запрос на получение
     * данных о товарах
     */
    private fun getData(password: String) {
        val users: List<UserDataModel> = DatabaseUserHandler(this).viewUsers()

        if (BCrypt.checkpw(users[0].user_password, password)) {

            val result = ServerHelper(users[0].user_name, password, users[0].ip).getData(this)
            products = Gson().fromJson(result, object : TypeToken<Array<Products>>() {}.type)

            if (products.isNotEmpty())
                saveData(products)
        }
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