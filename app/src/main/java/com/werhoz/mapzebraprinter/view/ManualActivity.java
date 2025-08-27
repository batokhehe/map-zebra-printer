package com.werhoz.mapzebraprinter.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ManualActivity extends AppCompatActivity {

    private int image;
    private String fileName;
    private String name;
    private String macAddress;
    private TextView etTemplate;
    private EditText etQty;
    private EditText etPrice;
    private Button btnPrint;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manual);

        Intent intentExtra = getIntent();
        image = intentExtra.getIntExtra("image", 0);
        name = intentExtra.getStringExtra("name");
        fileName = intentExtra.getStringExtra("template");
        macAddress = Hawk.get("macAddress");

        etTemplate = findViewById(R.id.et_template);
        etQty = findViewById(R.id.et_qty);
        etPrice = findViewById(R.id.et_price);
        btnPrint = findViewById(R.id.btn_print);
        btnBack = findViewById(R.id.btn_back);

        etTemplate.setText(name);
        etQty.requestFocus();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("MAP Zebra Printer - Print");
        }

        btnPrint.setOnClickListener(v -> {
            if (!etPrice.getText().toString().isEmpty() || !etQty.getText().toString().isEmpty()) {
                btnPrint.setEnabled(false);
                Toast.makeText(this, "Printing, please wait...", Toast.LENGTH_LONG).show();
                new Handler(Looper.getMainLooper()).postDelayed(this::printToZebra, 1000);
            } else {
                Toast.makeText(this, "Please Input Qty and Price", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });

        etPrice.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etPrice.removeTextChangedListener(this);

                    // Hapus semua titik agar bisa parsing ulang
                    String cleanString = s.toString().replace(".", "");

                    try {
                        // Ubah ke long
                        long parsed = Long.parseLong(cleanString);

                        // Format dengan tanda titik
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                        symbols.setGroupingSeparator('.');
                        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
                        String formatted = formatter.format(parsed);

                        current = formatted;
                        etPrice.setText(formatted);
                        etPrice.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        // Handle jika input kosong atau bukan angka
                        current = "";
                        etPrice.setText("");
                    }

                    etPrice.addTextChangedListener(this);
                }
            }
        });
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
            String content = generateContent(qty, price);

            cpcl = cpcl.replace("{CONTENT}", content);
            cpcl = cpcl.replace("{height}", String.valueOf(170 * (int) Math.ceil(qty / 2.0)));
            printer.sendCommand(cpcl);  // Sending CPCL command

            Toast.makeText(this, "Print job sent.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            btnPrint.setEnabled(true);
            resetForm();
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

    private void resetForm() {
        etQty.setText("");
        etPrice.setText("");
    }


    public String generateContent(int qty, String price) {
        if (fileName.contains("sale")) return generatePriceSale(qty, price);
        return generatePriceRegular(qty, price);
    }

    public String generatePriceSale(int qty, String price) {
        StringBuilder content = new StringBuilder();

        // Configs
        int boxWidth = 264;
        int boxHeight = 120;
        int spacingY = 4;

        int[] startX = {7, 304}; // posisi kolom kiri & kanan
        int startY = 13; // posisi awal Y

        // Offsets global (bisa diubah kalau mau geser)
        int shiftX = 0;   // negatif = kiri, positif = kanan
        int shiftY = 0;   // negatif = atas, positif = bawah

        // Text price
        int priceTextOffsetY = 32;
        int fontWidthEstimate = price.length() * 20;

        // SALE label
        int[] saleX = {17, 314};
        int[] saleY = {104, 235, 369};

        // Vertical line
        int[] lineX = {43, 340};
        int[] lineYStart = {14, 147, 277};
        int[] lineYEnd = {132, 265, 395};

        for (int i = 0; i < qty; i++) {
            int col = i % 2;       // kolom kiri (0) / kanan (1)
            int row = i / 2;

            int x1 = startX[col] + shiftX;
            int y1 = startY + row * (boxHeight + spacingY) + shiftY;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            // BOX
            content.append(String.format("BOX %d %d %d %d 2\n", x1, y1, x2, y2));

            // PRICE text
            int priceX = x1 + ((boxWidth + 50 - fontWidthEstimate) / 2);
            int topY = y1 + priceTextOffsetY;
            content.append(String.format("T 4 0 %d %d %s\n", priceX, topY, price));

            // Vertical "SALE"
            content.append(String.format("T90 7 0 %d %d SALE\n",
                    saleX[col] + shiftX,
                    saleY[row] + shiftY));

            // Vertical line
            content.append(String.format("L %d %d %d %d 1\n",
                    lineX[col] + shiftX,
                    lineYStart[row] + shiftY,
                    lineX[col] + shiftX,
                    lineYEnd[row] + shiftY));
        }

        return content.toString();
    }

    public String generatePriceRegular(int qty, String price) {
        StringBuilder content = new StringBuilder();

        // box and text dimensions
        int boxWidth = 264;
        int boxHeight = 120;
        int startX1 = 15;
        int startX2 = 300;
        int fontWidthEstimate = price.length() * 20;
        int[] textOffset = {9, 32}; // x and y padding inside box

        for (int i = 0; i < qty; i++) {
            int col = i % 2; // 0 = left, 1 = right
            int row = i / 2;

            int x1 = col == 0 ? startX1 : startX2;
            int y1 = 13 + row * (boxHeight + 12); // 12 is spacing between boxes
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            int textX = x1 + ((boxWidth - fontWidthEstimate) / 2);
            int textY = y1 + textOffset[1];

            content.append(String.format("BOX %d %d %d %d 2\n", x1, y1, x2, y2));
            content.append(String.format("T 4 0 %d %d %s\n", textX, textY, price));
        }
        return content.toString();
    }
}