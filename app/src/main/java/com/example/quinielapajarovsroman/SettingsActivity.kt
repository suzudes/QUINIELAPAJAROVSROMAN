package com.example.quinielapajarovsroman

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        tokenManager = TokenManager(this)
        val inputToken = findViewById<EditText>(R.id.tokenInput)
        val btnSave = findViewById<Button>(R.id.btnSaveToken)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentToken = tokenManager.getToken()
        if (currentToken != null) {
            inputToken.setText(currentToken)
        }

        btnSave.setOnClickListener {
            val token = inputToken.text.toString().trim()
            if (token.isNotEmpty()) {
                tokenManager.saveToken(token)
                Toast.makeText(this, "TOKEN GUARDADO. REINICIANDO...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        btnLogout.setOnClickListener {
            tokenManager.clear()
            (ApiClient.getCookieJar(this) as? PersistentCookieJar)?.clear()
            Toast.makeText(this, "SESIÓN CERRADA", Toast.LENGTH_SHORT).show()
            inputToken.setText("")
        }
    }
}
