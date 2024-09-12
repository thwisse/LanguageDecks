package io.github.thwisse.languagedecks

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("DecksData", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Deck'leri kaydetme
    fun saveDecks(deckList: List<Deck>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(deckList)
        editor.putString("deck_list", json)
        editor.apply()
    }

    // Deck'leri geri alma
    fun getDecks(): MutableList<Deck> {
        val json = sharedPreferences.getString("deck_list", null)
        val type = object : TypeToken<List<Deck>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}

