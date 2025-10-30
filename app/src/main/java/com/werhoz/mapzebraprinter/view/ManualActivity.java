package com.werhoz.mapzebraprinter.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceHeader;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceSale;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceSaleVertical;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceVertical;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.io.InputStream;

import taimoor.sultani.sweetalert2.Sweetalert;

public class ManualActivity extends AppCompatActivity {

    private int image;
    private String fileName;
    private String name;
    private String macAddress;
    private TextView etTemplate;
    private EditText etQty;
    private EditText etPrice;
    private EditText etHeader;
    private Button btnPrint;
    private Button btnBack;
    private TextInputLayout tilHeader;
    private Sweetalert pDialog;

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
        tilHeader = findViewById(R.id.til_header);
        etQty = findViewById(R.id.et_qty);
        etPrice = findViewById(R.id.et_price);
        etHeader = findViewById(R.id.et_header);
        btnPrint = findViewById(R.id.btn_print);
        btnBack = findViewById(R.id.btn_back);

        etTemplate.setText(name);
        tilHeader.setVisibility(!fileName.contains("price_sale_even.zpl") ? VISIBLE : GONE);
        if (!fileName.contains("price_sale_even.zpl"))
            etHeader.requestFocus();
        else etQty.requestFocus();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("MAP Zebra Printer - Print");
        }

        btnPrint.setOnClickListener(v -> {
            if (!etPrice.getText().toString().isEmpty() || !etQty.getText().toString().isEmpty()) {
                btnPrint.setEnabled(false);
                Toast.makeText(this, "Printing, please wait...", Toast.LENGTH_LONG).show();
                pDialog = new Sweetalert(ManualActivity.this, Sweetalert.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A0009"));
                pDialog.setTitleText("Printing...");
                pDialog.setCancelable(false);
                pDialog.show();

                new Thread(() -> {
                    try {
                        printToZebra();
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            pDialog.dismiss();
                            btnPrint.setEnabled(true);
                            Toast.makeText(this, "Print gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }).start();
            } else {
                Toast.makeText(this, "Please Input Qty and Price", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });

//        etPrice.addTextChangedListener(new TextWatcher() {
//            private String current = "";
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (!s.toString().equals(current)) {
//                    etPrice.removeTextChangedListener(this);
//
//                    // Hapus semua titik agar bisa parsing ulang
//                    String cleanString = s.toString().replace(".", "");
//
//                    try {
//                        // Ubah ke long
//                        long parsed = Long.parseLong(cleanString);
//
//                        // Format dengan tanda titik
//                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
//                        symbols.setGroupingSeparator('.');
//                        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
//                        String formatted = formatter.format(parsed);
//
//                        current = formatted;
//                        etPrice.setText(formatted);
//                        etPrice.setSelection(formatted.length());
//                    } catch (NumberFormatException e) {
//                        // Handle jika input kosong atau bukan angka
//                        current = "";
//                        etPrice.setText("");
//                    }
//
//                    etPrice.addTextChangedListener(this);
//                }
//            }
//        });
    }

    // Load the CPCL template from assets
    public String loadZpl(Context context, String fileName) {
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

            // Create a ZebraPrinter instance
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

            // Send the CPCL data directly to the printer without setting language
            int row = qty / 2;
            if (row > 0) {
                String cpcl = loadZpl(this, fileName);

                cpcl = cpcl.replace("{qty}", String.valueOf(row));
                String content = generateContent(cpcl, qty, price);
                printer.sendCommand(content);
            }

            if (qty % 2 > 0) {
                String cpcl = loadZpl(this, fileName.replace("even", "odd"));
                String content = generateContent(cpcl, qty, price);
                printer.sendCommand(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } finally {
            runOnUiThread(() -> {
                btnPrint.setEnabled(true);
                pDialog.dismiss();
                btnPrint.setEnabled(true);
                Toast.makeText(this, "Print job sent.", Toast.LENGTH_SHORT).show();
                resetForm();
            });
            try {
                if (connection != null && connection.isConnected()) {
                    connection.close(); // Close the connection after printing
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error closing connection: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void resetForm() {
        etQty.setText("");
        etPrice.setText("");
    }

    public String generateContent(String content, int qty, String price) {
        String header = etHeader.getText().toString();
        if (fileName.contains("price_v")) return generatePriceVertical(content, price, header);
        if (fileName.contains("price_sale_v"))
            return generatePriceSaleVertical(content, price, header);
        if (fileName.contains("sale")) return generatePriceSale(content, price);
        return generatePriceHeader(content, price, header);
    }

}