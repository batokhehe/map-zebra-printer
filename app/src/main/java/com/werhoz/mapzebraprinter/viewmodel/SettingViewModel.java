package com.werhoz.mapzebraprinter.viewmodel;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SettingViewModel extends ViewModel {
    private final MutableLiveData<List<BluetoothDevice>> devicesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private Context appContext;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    devicesLiveData.setValue(new ArrayList<>(deviceList));
                }
            }
        }
    };

    public LiveData<List<BluetoothDevice>> getDevicesLiveData() {
        return devicesLiveData;
    }

    public void startDiscovery(Context context) {
        this.appContext = context.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) return;

        // Clear old data
        deviceList.clear();
        devicesLiveData.setValue(new ArrayList<>());

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        appContext.registerReceiver(receiver, filter);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    public void stopDiscovery() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        try {
            appContext.unregisterReceiver(receiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCleared() {
        stopDiscovery();
        super.onCleared();
    }
}