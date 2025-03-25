package com.example.sparewallet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sparewallet.databinding.ActivityComingSoonBinding

class ComingSoonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComingSoonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComingSoonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val menuName = intent.getStringExtra("menuName") ?: "This feature"
        binding.textComingSoon.text = "$menuName will be available in the next patch"
    }
}
