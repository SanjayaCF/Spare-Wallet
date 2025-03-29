package com.example.sparewallet.ui.auth

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityChangePinBinding

class ChangePinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("SpareWalletPrefs", Context.MODE_PRIVATE)
        val savedPin = sharedPref.getString("user_pin", null)

        binding.savePinButton.setOnClickListener {
            val oldPin = binding.etOldPin.text.toString().trim()
            val newPin = binding.etNewPin.text.toString().trim()
            val confirmNewPin = binding.etConfirmNewPin.text.toString().trim()

            if (savedPin == null) {
                Toast.makeText(this, "No existing PIN found!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPin != savedPin) {
                Toast.makeText(this, "Old PIN is incorrect", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPin.length != 6) {
                Toast.makeText(this, "New PIN must be 6 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPin != confirmNewPin) {
                Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            with(sharedPref.edit()) {
                putString("user_pin", newPin)
                apply()
            }

            Toast.makeText(this, "PIN updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
