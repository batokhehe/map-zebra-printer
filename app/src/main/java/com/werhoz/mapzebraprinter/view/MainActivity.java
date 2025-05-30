package com.werhoz.mapzebraprinter.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.utils.DateTimeUtil;
import com.werhoz.mapzebraprinter.viewmodel.DataViewModel;

import taimoor.sultani.sweetalert2.Sweetalert;


public class MainActivity extends AppCompatActivity {

    private TextView connectedDevice;
    private Button btnDownload;
    private Sweetalert pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Hawk.init(this).build();
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
        btnAuto.setOnClickListener(view -> goToTemplateActivity("auto")); //printToZebra());

        Button btnManual = findViewById(R.id.btn_manual);
        btnManual.setOnClickListener(view -> goToTemplateActivity("manual")); //printToZebra());

        connectedDevice = findViewById(R.id.tv_printer);

        DataViewModel viewModel = new ViewModelProvider(this).get(DataViewModel.class);

        viewModel.getSyncStatus().observe(this, message -> {
            pDialog.setTitleText(message);
            pDialog.show();

            if (message.startsWith("✅") || message.startsWith("❌")) {
                pDialog.dismissWithAnimation();
            }
            if (message.startsWith("✅")) {
                Hawk.put("last_sync", DateTimeUtil.getCurrentDateTime());
            }
            updateLastSync();
        });


        btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog = new Sweetalert(MainActivity.this, Sweetalert.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A0009"));
                pDialog.setTitleText("Starting...");
                pDialog.setCancelable(false);

                viewModel.startSync();
            }
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
    }
}
