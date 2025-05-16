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

public class AutoActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_auto);

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
//            connection = new BluetoothConnection(macAddress);
//            connection.open();

//            String cpclCommand = "! U1 setvar \"media.clear\" \"\"\n";
//            connection.write(cpclCommand.getBytes());

            Log.d("Zebra", "Buffer cleared.");

            // Create a ZebraPrinter instance
//            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

            // Send the CPCL data directly to the printer without setting language
            String cpcl = loadZpl(this);
            String content = generateContent(qty, price);

            cpcl = cpcl.replace("{CONTENT}", content);
//            printer.sendCommand(cpcl);  // Sending CPCL command

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


    public String generateContent(int qty, String price) {
        if (fileName.contains("active")) return generateActive(qty, price);
        return generatePriceRegular(qty, price);
    }

    public String generateActive(int qty, String price) {
        int startX = 19;
        int startY = 21;
        int boxWidth = 264;
        int boxHeight = 440;
        int columnSpacing = 275; // roughly the horizontal shift between columns
        int rowSpacing = 457;    // vertical shift per row

        StringBuilder content = new StringBuilder();

        for (int i = 0; i < qty; i++) {
            int col = i % 2;
            int row = i / 2;

            int offsetX = col * columnSpacing;
            int offsetY = row * rowSpacing;

            // BOX
            content.append("BOX ")
                    .append(startX + offsetX).append(" ")
                    .append(startY + offsetY).append(" ")
                    .append(startX + offsetX + boxWidth).append(" ")
                    .append(startY + offsetY + boxHeight).append(" 2\n");

            // Variant/Single Article
            content.append("T 5 0 ").append(startX + 15 + offsetX).append(" ")
                    .append(startY + offsetY + 6).append(" Variant/Single Article\n");

            // Article Description
            content.append("T 5 0 ").append(startX + 33 + offsetX).append(" ")
                    .append(startY + offsetY + 55).append(" Article Description\n");

            // Product Category
            content.append("T 5 0 ").append(startX + 39 + offsetX).append(" ")
                    .append(startY + offsetY + 156).append(" Product Category\n");

            // WAS : Original Price
            content.append("T 5 0 ").append(startX + 19 + offsetX).append(" ")
                    .append(startY + offsetY + 351).append(" WAS : Original Price\n");

            // NOW : Current Price
            content.append("T 5 0 ").append(startX + 19 + offsetX).append(" ")
                    .append(startY + offsetY + 381).append(" NOW : Current Price\n");

            // Lines
            content.append("L ").append(startX + 15 + offsetX).append(" ")
                    .append(startY + offsetY + 46).append(" ")
                    .append(startX + 248 + offsetX).append(" ")
                    .append(startY + offsetY + 46).append(" 1\n");

            content.append("L ").append(startX + 33 + offsetX).append(" ")
                    .append(startY + offsetY + 146).append(" ")
                    .append(startX + 236 + offsetX).append(" ")
                    .append(startY + offsetY + 146).append(" 1\n");

            content.append("L ").append(startX + 15 + offsetX).append(" ")
                    .append(startY + offsetY + 190).append(" ")
                    .append(startX + 248 + offsetX).append(" ")
                    .append(startY + offsetY + 190).append(" 1\n");

            content.append("L ").append(startX + 19 + offsetX).append(" ")
                    .append(startY + offsetY + 333).append(" ")
                    .append(startX + 245 + offsetX).append(" ")
                    .append(startY + offsetY + 333).append(" 1\n");

            content.append("L ").append(startX + 15 + offsetX).append(" ")
                    .append(startY + offsetY + 428).append(" ")
                    .append(startX + 245 + offsetX).append(" ")
                    .append(startY + offsetY + 428).append(" 1\n");

            // Barcode (now inside every iteration)
            content.append("T 4 0 ")
                    .append(startX + 20 + offsetX).append(" ")
                    .append(startY + offsetY + 226).append(" BARCODE\n");
        }

        return content.toString();
    }

    public String generatePriceRegular(int qty, String price) {
        StringBuilder content = new StringBuilder();

        // box and text dimensions
        int boxWidth = 264;
        int boxHeight = 120;
        int startX1 = 20;
        int startX2 = 289;
        int[] textOffset = {9, 32}; // x and y padding inside box

        for (int i = 0; i < qty; i++) {
            int col = i % 2; // 0 = left, 1 = right
            int row = i / 2;

            int x1 = col == 0 ? startX1 : startX2;
            int y1 = 13 + row * (boxHeight + 12); // 12 is spacing between boxes
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            int textX = x1 + textOffset[0];
            int textY = y1 + textOffset[1];

            content.append(String.format("BOX %d %d %d %d 2\n", x1, y1, x2, y2));
            content.append(String.format("T 5 2 %d %d %s\n", textX, textY, price));
        }
        return content.toString();
    }
}