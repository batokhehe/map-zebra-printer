package com.werhoz.mapzebraprinter.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.network.ApiClient;
import com.werhoz.mapzebraprinter.utils.DateTimeUtil;
import com.werhoz.mapzebraprinter.viewmodel.DataViewModel;

import taimoor.sultani.sweetalert2.Sweetalert;


public class MainActivity extends AppCompatActivity {

    private TextView connectedDevice;
    private Button btnDownload;
    private Sweetalert pDialog;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Hawk.init(this).build();
        ApiClient.init(getApplicationContext());
        updateLastSync();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSetting = findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });

        Button btnAuto = findViewById(R.id.btn_auto);
        btnAuto.setOnClickListener(view -> {
//            if (!isBluetoothSet() || !isLastSyncSet()) {
//                return;
//            }
            goToTemplateActivity("auto");
        }); //printToZebra());

        Button btnManual = findViewById(R.id.btn_manual);
        btnManual.setOnClickListener(view -> {
//            if (!isBluetoothSet()) {
//                return;
//            }
            goToTemplateActivity("manual"); //printToZebra());
        });

        connectedDevice = findViewById(R.id.tv_printer);

        DataViewModel viewModel = new ViewModelProvider(this).get(DataViewModel.class);

        viewModel.getSyncStatus().observe(this, message -> {
            pDialog.setTitleText(message);
            pDialog.show();

            if (message.startsWith("✅") || message.startsWith("❌")) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.dismissWithAnimation();
                    }
                }, 2000); // 2000 ms = 2 detik
            }
            if (message.startsWith("✅")) {
                Hawk.put("last_sync", DateTimeUtil.getCurrentDateTime());
                String[] parts = message.split(":");
                String sizePart = parts[1].trim(); // "1234 data."
                String sizeOnly = sizePart.split(" ")[0]; // "1234"
                counter = Integer.parseInt(sizeOnly); // 1234
            }
            updateLastSync();
        });


        btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(v -> {
            if (!isIpSet()) {
                return;
            }
            pDialog = new Sweetalert(MainActivity.this, Sweetalert.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A0009"));
            pDialog.setTitleText("Starting...");
            pDialog.setCancelable(false);
            counter = 0;

            viewModel.startSync();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String text = Hawk.get("deviceName", "");
        if (text.isEmpty())
            text = "No Connected Device.";
        else text = "Connected Devices: " + text;
        connectedDevice.setText(text);
    }

    private void goToTemplateActivity(String params) {
        Intent intent = new Intent(this, TemplateActivity.class);
        intent.putExtra("type", params);
        startActivity(intent);
    }

    private void updateLastSync() {
        TextView tvLastSync = findViewById(R.id.tv_last_download);
        tvLastSync.setText(String.format("Last Sync: %s", Hawk.get("last_sync", "-")));

        TextView tvCounter = findViewById(R.id.tv_counter_download);
        tvCounter.setText(String.format("%s Data", counter));
    }

    private boolean isIpSet() {
        String value = Hawk.get("ip_address", "");
        boolean result = value.isEmpty();
        if (result) {
            Toast.makeText(this, "Please set IP Address on Setting menu.", Toast.LENGTH_SHORT).show();
        }
        return !result;
    }

    private boolean isBluetoothSet() {
        String value = Hawk.get("macAddress", "");
        boolean result = value.isEmpty();
        if (result) {
            Toast.makeText(this, "Please set Bluetooth on Setting menu.", Toast.LENGTH_SHORT).show();
        }
        return !result;
    }

    private boolean isLastSyncSet() {
        String value = Hawk.get("last_sync", "");
        boolean result = value.isEmpty();
        if (result) {
            Toast.makeText(this, "Please download data first.", Toast.LENGTH_SHORT).show();
        }
        return !result;
    }
}
