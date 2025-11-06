package com.example.autovoucher

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Storage {
    private const val PREFS = "autovoucher_prefs"
    private const val KEY_VOUCHERS = "vouchers"
    private const val KEY_DELAY = "delay_seconds"
    private const val KEY_SERVICE_ENABLED = "service_enabled"

    fun saveVouchers(ctx: Context, list: List<GiftVoucher>) {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_VOUCHERS, Gson().toJson(list)).apply()
    }

    fun loadVouchers(ctx: Context): MutableList<GiftVoucher> {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = sp.getString(KEY_VOUCHERS, null) ?: return mutableListOf()
        val type = object : TypeToken<List<GiftVoucher>>() {}.type
        return Gson().fromJson<List<GiftVoucher>>(json, type).toMutableList()
    }

    fun saveDelay(ctx: Context, secs: Int) {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putInt(KEY_DELAY, secs).apply()
    }

    fun loadDelay(ctx: Context): Int {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getInt(KEY_DELAY, 2) // default 2s
    }

    fun saveServiceEnabled(ctx: Context, enabled: Boolean) {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun loadServiceEnabled(ctx: Context): Boolean {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_SERVICE_ENABLED, false)
    }
}
