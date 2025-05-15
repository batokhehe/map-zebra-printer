package com.werhoz.mapzebraprinter.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;

public class MainActivity extends AppCompatActivity {

    private TextView connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Hawk.init(this).build();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectedDevice.setText(Hawk.get("deviceName", "No Connected Device."));
    }

    private void goToTemplateActivity(String params) {
        Intent intent = new Intent(this, TemplateActivity.class);
        intent.putExtra("type", params);
        startActivity(intent);
    }
}
