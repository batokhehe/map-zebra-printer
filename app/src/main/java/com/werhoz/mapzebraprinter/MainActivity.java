package com.werhoz.mapzebraprinter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.view.SettingActivity;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

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

        Button btnPrint = findViewById(R.id.btn_auto);
        String macAddress = "04:7F:0E:71:80:5E"; // Your printer's Bluetooth MAC address
        btnPrint.setOnClickListener(view -> printToZebra(macAddress));
    }

    // Load the CPCL template from assets
    public static String loadZpl(Context context) {
        String cpclTemplate = "";

        try (InputStream is = context.getAssets().open("label_template.zpl")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            cpclTemplate = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cpclTemplate;
    }

    // Main method to handle printing to Zebra printer
    public void printToZebra(String macAddress) {
        Connection connection = null;

        try {
            // Set up Bluetooth connection to the printer
            connection = new BluetoothConnection(macAddress);
            connection.open();

            // Create a ZebraPrinter instance
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

            // Send the CPCL data directly to the printer without setting language
//            connection.write("! U1 do \"device.reset\"\r\n".getBytes());
            String cpcl = loadZpl(this);
            cpcl = cpcl.replace("{TEXT1}", "susu uht");
            printer.sendCommand(cpcl);  // Sending CPCL command

            Toast.makeText(this, "Print job sent.", Toast.LENGTH_SHORT).show();

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
        }
    }
}
