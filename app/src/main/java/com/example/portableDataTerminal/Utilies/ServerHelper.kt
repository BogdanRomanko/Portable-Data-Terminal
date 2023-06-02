package com.example.portableDataTerminal.Utilies

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import okhttp3.Credentials

class ServerHelper(
    private var name: String,
    private var password: String,
    private var ip: String) {

    fun getData(context: Context): String {
        val url = "http://$ip/barcodes/hs/products/get_all_products"
        val queue = Volley.newRequestQueue(context)
        var result = ""

        val request = object : StringRequest(
            Method.GET,
            url,
            { res ->
                result = res
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
                val cred = Credentials.basic(name, password, Charsets.UTF_8)
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

        return result
    }

    fun sendData(json: String, context: Context): Int {
        val url = "http://$ip/barcodes/hs/products/send_data"
        val queue = Volley.newRequestQueue(context)
        var result = 0

        val request = object : StringRequest(
            Method.POST,
            url,
            { _ ->
            },
            { _ ->
                result = 1
            })
        {
            /*
             * Устанавливаем header запроса для авторизации
             * на стороне веб-сервера
             */
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getHeaders(): MutableMap<String, String> {
                val cred = Credentials.basic(name, password, Charsets.UTF_8)
                val headers = HashMap<String, String>()
                headers["Authorization"] = cred
                return headers
            }

            override fun getBody(): ByteArray {
                return json.toByteArray()
            }
        }

        queue.add(request)

        return result
    }
}