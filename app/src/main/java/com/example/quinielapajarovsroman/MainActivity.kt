package com.example.quinielapajarovsroman

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: MatchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(this)

        val token = tokenManager.getToken()
        if (token == null) {
            goToSettings()
            return
        }

        setContentView(R.layout.activity_main)
        setupUI()
    }

    private fun setupUI() {
        // ... (Lógica de Fragments/RecyclerView que ya teníamos)
    }

    private fun goToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}
