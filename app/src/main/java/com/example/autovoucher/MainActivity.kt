package com.example.autovoucher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.autovoucher.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var vouchers: MutableList<GiftVoucher>
    private var running = false

    private val importJsonLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader().use { it?.readText() }
                    val voucherType = object : TypeToken<List<GiftVoucher>>() {}.type
                    val importedVouchers: List<GiftVoucher> = Gson().fromJson(json, voucherType)

                    vouchers.addAll(importedVouchers)
                    Storage.saveVouchers(this, vouchers)
                    adapter.clear()
                    adapter.addAll(vouchers.map { "${it.id} | ${it.pin}" })
                    Toast.makeText(this, "Successfully imported ${importedVouchers.size} vouchers", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error importing vouchers: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vouchers = Storage.loadVouchers(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, vouchers.map { "${it.id} | ${it.pin}" }.toMutableList())
        binding.listVouchers.adapter = adapter

        binding.btnAdd.setOnClickListener {
            val id = binding.etCard.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()
            if (id.isNotEmpty() && pin.isNotEmpty()) {
                vouchers.add(GiftVoucher(id, pin))
                Storage.saveVouchers(this, vouchers)
                adapter.add("$id | $pin")
                binding.etCard.text.clear()
                binding.etPin.text.clear()
            }
        }

        binding.btnImportJson.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/json"
            importJsonLauncher.launch(intent)
        }

        binding.listVouchers.setOnItemClickListener { _, _, pos, _ ->
            vouchers.removeAt(pos)
            Storage.saveVouchers(this, vouchers)
            adapter.remove(adapter.getItem(pos))
        }

        val delay = Storage.loadDelay(this)
        binding.seekDelay.progress = delay
        binding.tvDelay.text = "${delay}s"
        binding.seekDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val p = if (progress < 1) 1 else progress
                binding.tvDelay.text = "${p}s"
                Storage.saveDelay(this@MainActivity, p)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // Restore running state from storage
        running = Storage.loadServiceEnabled(this)
        binding.btnStartStop.text = if (running) "Stop" else "Start"

        binding.btnStartStop.setOnClickListener {
            running = !running
            binding.btnStartStop.text = if (running) "Stop" else "Start"
            Storage.saveServiceEnabled(this, running)
        }
    }
}
