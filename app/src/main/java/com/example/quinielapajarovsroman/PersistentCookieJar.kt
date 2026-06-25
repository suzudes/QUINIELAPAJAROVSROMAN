package com.example.quinielapajarovsroman

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs = context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
    private val cookies = mutableSetOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies.addAll(cookies)
        cookies.forEach { cookie ->
            prefs.edit().putString(cookie.name, cookie.toString()).apply()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val loadedCookies = mutableListOf<Cookie>()
        prefs.all.forEach { (_, value) ->
            Cookie.parse(url, value as String)?.let { loadedCookies.add(it) }
        }
        return loadedCookies
    }

    fun clear() {
        cookies.clear()
        prefs.edit().clear().apply()
    }
}
