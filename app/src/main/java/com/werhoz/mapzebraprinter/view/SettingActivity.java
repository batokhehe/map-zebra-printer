package com.werhoz.mapzebraprinter.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.adapter.SettingAdapter;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;

import java.util.ArrayList;
import java.util.List;

import taimoor.sultani.sweetalert2.Sweetalert;

public class SettingActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    private RecyclerView rvItems;
    private Button btnSave;
    private EditText etIp;

    private SettingAdapter adapter;
    private List<BluetoothDevice> dataList = new ArrayList<>();
    private Sweetalert pDialog;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device != null ? device.getName() : "Unknown Device";
                String address = device != null ? device.getAddress() : "Unknown Address";
                System.out.println("Discovered Device: " + name + " - " + address);
                if (!dataList.contains(device)) {
                    dataList.add(device);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting); // Ensure this layout exists

        rvItems = findViewById(R.id.rv_devices);
        btnSave = findViewById(R.id.btn_save);
        etIp = findViewById(R.id.et_ip_address);
        etIp.setText(Hawk.get("ip_address", "http://10.3.25.166:7255"));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("MAP Zebra Printer - Setting");
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        discoverBluetoothDevices();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Button click to start discovery
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        adapter = new SettingAdapter(dataList, Hawk.get("macAddress", "null"));
        rvItems.setAdapter(adapter);

        adapter.setOnItemClickListener(device -> {
            // Handle item click here
            pDialog = new Sweetalert(SettingActivity.this, Sweetalert.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A0009"));
            pDialog.setTitleText("Connecting Bluetooth...");
            pDialog.setCancelable(false);
            Connection connection = null;
            try {
                // Set up Bluetooth connection to the printer
                connection = new BluetoothConnection(device.getAddress());
                connection.open();

                Toast.makeText(this, "Connected.", Toast.LENGTH_SHORT).show();
                Hawk.put("macAddress", device.getAddress());
                Hawk.put("deviceName", device.getName());
                adapter.setSelectedMacAddress(device.getAddress());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (connection != null && connection.isConnected()) {
                        connection.close(); // Close the connection after printing
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error closing connection: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
                pDialog.dismissWithAnimation();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = etIp.getText().toString();
                Hawk.put("ip_address", value);
                Toast.makeText(SettingActivity.this, "Data saved, please wait restarting application..", Toast.LENGTH_SHORT).show();
                restartApps();
            }
        });

    }

    private void restartApps() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // Kill the current process
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    @SuppressLint({"MissingPermission", "NotifyDataSetChanged"})
    private void discoverBluetoothDevices() {
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            String name = device.getName() != null ? device.getName() : "Unknown Device";
            String address = device.getAddress();
            System.out.println("Paired Device: " + name + " - " + address);
            if (!dataList.contains(device))
                dataList.add(device);
        }
        adapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
