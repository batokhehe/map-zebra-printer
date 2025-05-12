package com.werhoz.mapzebraprinter.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.adapter.SettingAdapter;
import com.werhoz.mapzebraprinter.viewmodel.SettingViewModel;

public class SettingActivity extends AppCompatActivity {

    private SettingViewModel viewModel;
    private SettingAdapter adapter;
    private static final int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        RecyclerView recyclerView = findViewById(R.id.rv_devices);
        adapter = new SettingAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);

        viewModel.getDevicesLiveData().observe(this, devices -> adapter.submitList(devices));

        checkPermissionsAndDiscover();
    }

    private void checkPermissionsAndDiscover() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
                return;
            }
        }

        // Ensure Location is enabled
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean locationOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!locationOn) {
            Toast.makeText(this, "Please enable Location Services", Toast.LENGTH_LONG).show();
            return;
        }

        viewModel.startDiscovery(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.stopDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            checkPermissionsAndDiscover();
        }
    }
}