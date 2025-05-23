package com.luis.applogin

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ingenieriajhr.blujhr.BluJhr
import com.luis.applogin.databinding.ActivitySistemaBinding

class Sistema : AppCompatActivity() {

    private lateinit var binding: ActivitySistemaBinding
    private lateinit var blue: BluJhr
    private var devicesBluetooth = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySistemaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no tiene Bluetooth", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        blue = BluJhr(this)
        blue.onBluetooth()

        // Solicitar permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ), 1
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                ), 1
            )
        }

        // Conexión al seleccionar un dispositivo
        binding.listDeviceBluetooth.setOnItemClickListener { _, _, i, _ ->
            if (devicesBluetooth.isNotEmpty()) {
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object : BluJhr.ConnectedBluetooth {
                    override fun onConnectState(state: BluJhr.Connected) {
                        when (state) {
                            BluJhr.Connected.True -> {
                                Toast.makeText(applicationContext, "Conexión exitosa", Toast.LENGTH_SHORT).show()
                                binding.listDeviceBluetooth.visibility = View.GONE
                                binding.viewConn.visibility = View.VISIBLE
                                binding.descripcion.visibility = View.VISIBLE
                                rxReceived()
                            }

                            BluJhr.Connected.Pending -> {
                                Toast.makeText(applicationContext, "Conexión pendiente", Toast.LENGTH_SHORT).show()
                            }

                            BluJhr.Connected.False -> {
                                Toast.makeText(applicationContext, "Conexión fallida", Toast.LENGTH_SHORT).show()
                            }

                            BluJhr.Connected.Disconnect -> {
                                Toast.makeText(applicationContext, "Desconectado", Toast.LENGTH_SHORT).show()
                                binding.listDeviceBluetooth.visibility = View.VISIBLE
                                binding.viewConn.visibility = View.GONE
                                binding.descripcion.visibility = View.GONE
                            }
                        }
                    }
                })
            }
        }

        // Botones de control
        binding.button1.setOnClickListener { blue.bluTx("1") }
        binding.buttonA.setOnClickListener { blue.bluTx("a") }
        binding.button2.setOnClickListener { blue.bluTx("2") }
        binding.button0.setOnClickListener { blue.bluTx("0") }

        // Botón de desconexión
        binding.buttonD.setOnClickListener {
            blue.closeConnection()
            binding.listDeviceBluetooth.visibility = View.VISIBLE
            binding.viewConn.visibility = View.GONE
            binding.descripcion.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode, grantResults)) {
            Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                blue.initializeBluetooth()
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // ⚠️ ESTE MÉTODO ESCRUCIAL PARA MOSTRAR LOS DISPOSITIVOS
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100) {
            blue.initializeBluetooth()
        } else if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            devicesBluetooth = blue.deviceBluetooth()
            if (devicesBluetooth.isNotEmpty()) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesBluetooth)
                binding.listDeviceBluetooth.adapter = adapter
            } else {
                Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun rxReceived() {
        blue.loadDateRx(object : BluJhr.ReceivedData {
            override fun rxDate(rx: String) {
                Toast.makeText(applicationContext, "Respuesta: $rx", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
