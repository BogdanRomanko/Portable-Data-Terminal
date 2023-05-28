package com.example.portableDataTerminal.Utilies

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.portableDataTerminal.Models.UserDataModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Credentials

class ServerSync {

    fun getData(context: Context, users: List<UserDataModel>, products: Array<Products>): Array<Products>{
        val url = "http://" + users[0].ip + "/barcodes/hs/products/get_all_products"
        val queue = Volley.newRequestQueue(context)
        var prod: Array<Products> = arrayOf()

        val request = object : StringRequest(
            Method.GET,
            url,
            { result ->
                val dataArray = object : TypeToken<Array<Products>>() {}.type
                prod = Gson().fromJson(result, dataArray)
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
                val cred =
                    Credentials.basic(users[0].user_name, users[0].user_password, Charsets.UTF_8)
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

        return if (prod.isNotEmpty())
            prod
        else
            products
    }

}
