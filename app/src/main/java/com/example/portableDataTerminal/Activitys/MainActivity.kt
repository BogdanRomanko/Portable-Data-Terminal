package com.example.portableDataTerminal.Activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.portableDataTerminal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSync.setOnClickListener { sync() }
        binding.buttonInfo.setOnClickListener { info() }
        binding.buttonAcceptance.setOnClickListener { acceptance() }
        binding.buttonShipment.setOnClickListener { shipment() }

        title = ""
    }

    private fun sync() {
        startActivity(Intent(this, SyncActivity::class.java))
    }

    private fun info() {
        startActivity(Intent(this, InfoActivity::class.java))
    }

    private fun acceptance() {
        startActivity(Intent(this, AcceptanceActivity::class.java))
    }

    private fun shipment() {
        startActivity(Intent(this, ShipmentActivity::class.java))
    }
}