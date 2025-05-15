package com.werhoz.mapzebraprinter.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.io.InputStream;

public class ManualActivity extends AppCompatActivity {

    private int image;
    private String fileName;
    private String macAddress;
    private ImageView ivTemplate;
    private EditText etQty;
    private EditText etPrice;
    private Button btnPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manual);

        Intent intentExtra = getIntent();
        image = intentExtra.getIntExtra("image", 0);
        fileName = intentExtra.getStringExtra("template");
        macAddress = Hawk.get("macAddress");

        ivTemplate = findViewById(R.id.iv_template);
        etQty = findViewById(R.id.et_qty);
        etPrice = findViewById(R.id.et_price);
        btnPrint = findViewById(R.id.btn_print);

        ivTemplate.setImageResource(image);
        btnPrint.setOnClickListener(v -> printToZebra());
    }

    // Load the CPCL template from assets
    public String loadZpl(Context context) {
        String cpclTemplate = "";

        try (InputStream is = context.getAssets().open(fileName)) {
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
    public void printToZebra() {
        int qty = Integer.parseInt(etQty.getText().toString());
        String price = etPrice.getText().toString();
        Connection connection = null;

        try {
            // Set up Bluetooth connection to the printer
            connection = new BluetoothConnection(macAddress);
            connection.open();

            String cpclCommand = "! U1 setvar \"media.clear\" \"\"\n";
            connection.write(cpclCommand.getBytes());

            Log.d("Zebra", "Buffer cleared.");

            // Create a ZebraPrinter instance
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

            // Send the CPCL data directly to the printer without setting language
            String cpcl = loadZpl(this);

            StringBuilder content = new StringBuilder();
            // box and text dimensions
            int boxWidth = 264;
            int boxHeight = 120;
            int startX1 = 20;
            int startX2 = 289;
            int[] textOffset = {9, 32}; // x and y padding inside box

            for (int i = 0; i < qty; i++) {
                int col = i / 3; // 0 = left, 1 = right
                int row = i % 3;

                int x1 = col == 0 ? startX1 : startX2;
                int y1 = 13 + row * (boxHeight + 12); // 12 is spacing between boxes
                int x2 = x1 + boxWidth;
                int y2 = y1 + boxHeight;

                int textX = x1 + textOffset[0];
                int textY = y1 + textOffset[1];

                content.append(String.format("BOX %d %d %d %d 2\n", x1, y1, x2, y2));
                content.append(String.format("T 5 2 %d %d %s\n", textX, textY, price));
            }

            cpcl = cpcl.replace("{CONTENT}", content.toString());
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