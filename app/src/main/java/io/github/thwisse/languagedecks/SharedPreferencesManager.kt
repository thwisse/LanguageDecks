package io.github.thwisse.languagedecks

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("DecksData", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "language_decks_prefs"
        private const val IMAGES_ASSIGNED_KEY = "images_assigned"
    }

    // Deck'leri kaydetme
    fun saveDecks(deckList: List<Deck>) {
//        Log.d("SharedPreferencesManager KEKOD", "saveDecks() called with deckList size: ${deckList.size}")
        val editor = sharedPreferences.edit()
        val json = gson.toJson(deckList)
        editor.putString("deck_list", json)
        editor.apply()
//        Log.d("SharedPreferencesManager KEKOD", "saveDecks() successfully saved: $json")
    }

    // Deck'leri geri alma
    fun getDecks(): MutableList<Deck> {
        val json = sharedPreferences.getString("deck_list", null)
        val type = object : TypeToken<List<Deck>>() {}.type
        return if (json != null) {
//            Log.d("SharedPreferencesManager KEKOD", "getDecks() returned: $json")
            gson.fromJson(json, type)
        } else {
//            Log.e("SharedPreferencesManager KEKOD", "getDecks() returned empty list")
            mutableListOf()
        }
    }

    // Boolean değeri kaydetme
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    // Boolean değeri okuma
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    // Resimlerin atanıp atanmadığını kontrol eden özel fonksiyon
    fun isImagesAssigned(): Boolean {
        return getBoolean(IMAGES_ASSIGNED_KEY)
    }

    // Resimlerin atandığını kaydeden fonksiyon
    fun setImagesAssigned(isAssigned: Boolean) {
        putBoolean(IMAGES_ASSIGNED_KEY, isAssigned)
    }
}

