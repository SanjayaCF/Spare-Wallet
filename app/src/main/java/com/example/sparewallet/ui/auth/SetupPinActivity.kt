package com.example.sparewallet.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivitySetupPinBinding
import com.example.sparewallet.ui.main.MainActivity

class SetupPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupPinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setupPinButton.setOnClickListener {
            val pin = binding.pinEditText.text.toString().trim()
            val confirmPin = binding.confirmPinEditText.text.toString().trim()

            if (pin.length != 6 || confirmPin.length != 6) {
                Toast.makeText(this, "Please enter a 6-digit PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirmPin) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("SpareWalletPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("user_pin", pin)
                apply()
            }
            Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
