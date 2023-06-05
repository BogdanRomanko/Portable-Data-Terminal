package com.example.portableDataTerminal.Activitys

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.portableDataTerminal.BuildConfig
import com.example.portableDataTerminal.databinding.ActivityAboutBinding

/*
 * Класс, содержащий в себе обработку страницы для получения
 * информации о приложении
 */
class AboutActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    /*
     * Обработчик создания страницы
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "О приложении"

        binding.versionTextView.text = "${binding.versionTextView.text} ${BuildConfig.VERSION_NAME}"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /*
     * Обработчик для кнопки возврата
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}