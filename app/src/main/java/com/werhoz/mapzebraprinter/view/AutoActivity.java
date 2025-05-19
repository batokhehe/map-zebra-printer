package com.werhoz.mapzebraprinter.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.data.model.ItemResponse;
import com.werhoz.mapzebraprinter.viewmodel.ItemViewModel;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class AutoActivity extends AppCompatActivity {

    private int image;
    private String fileName;
    private String name;
    private String macAddress;
    private TextView etTemplate;
    private EditText etQty;
    private EditText etBarcode;
    private Button btnPrint;
    private View clContent;
    private View progressBar;
    private TextView etVariant;
    private TextView etDescription;
    private TextView etCategory;
    private TextView etEan;
    private TextView etWasPrice;
    private TextView etCurrentPrice;
    private ItemViewModel viewModel;
    private ItemResponse itemResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auto);

        Intent intentExtra = getIntent();
        image = intentExtra.getIntExtra("image", 0);
        fileName = intentExtra.getStringExtra("template");
        name = intentExtra.getStringExtra("name");
        macAddress = Hawk.get("macAddress");

        etTemplate = findViewById(R.id.et_template);
        etQty = findViewById(R.id.et_qty);
        btnPrint = findViewById(R.id.btn_print);

        etTemplate.setText(name);
        btnPrint.setOnClickListener(v -> {
            Toast.makeText(this, "Printing, please wait...", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(this::printToZebra, 1000);
        });


        etVariant = findViewById(R.id.et_variant);
        etDescription = findViewById(R.id.et_desc);
        etCategory = findViewById(R.id.et_category);
        etEan = findViewById(R.id.et_ean);
        etWasPrice = findViewById(R.id.et_was_price);
        etCurrentPrice = findViewById(R.id.et_current_price);
        etBarcode = findViewById(R.id.et_barcode);
        clContent = findViewById(R.id.cl_content);
        progressBar = findViewById(R.id.progress_bar);

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        clContent.setVisibility(GONE);
        etBarcode.requestFocus();
        viewModel.getItemResponseLiveData().observe(this, item -> {
            progressBar.setVisibility(GONE);
            itemResponse = item;
            if (item != null) {
                etVariant.setText(item.getVariant());
                etDescription.setText(item.getDescription());
                etCategory.setText(item.getProductCategory());
                etEan.setText(item.getEanNumber());
                etWasPrice.setText("Rp " + formatNumber(item.getWasPrice()));
                etCurrentPrice.setText("Rp " + formatNumber(item.getCurrentPrice()));
                clContent.setVisibility(VISIBLE);
                etQty.requestFocus();
            } else {
                clContent.setVisibility(GONE);
                Toast.makeText(this, "Failed to load item", Toast.LENGTH_SHORT).show();
            }
        });

        etBarcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String barcodeValue = etBarcode.getText().toString().trim();

                progressBar.setVisibility(VISIBLE);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    viewModel.fetchItem(barcodeValue);
                }, 1000);

                return true; // consume event
            }
            return false;
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
            String content = generateContent(qty);

            cpcl = cpcl.replace("{CONTENT}", content);
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


    public String generateContent(int qty) {
        if (fileName.contains("active")) return generateActive(qty);
        if (fileName.contains("alo")) return generateAlo(qty);
        if (fileName.contains("sale")) return generatePriceSale(qty);
        if (fileName.contains("regular")) return generatePriceRegular(qty);
        return generateMango(qty);
    }

    public String generateActive(int qty) {
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
                    .append(startY + offsetY + 6).append(" ").append(itemResponse.getVariant()).append("\n");

            // Article Description
            content.append("T 5 0 ").append(startX + 33 + offsetX).append(" ")
                    .append(startY + offsetY + 55).append(" ").append(itemResponse.getDescription()).append("\n");

            // Product Category
            content.append("T 5 0 ").append(startX + 39 + offsetX).append(" ")
                    .append(startY + offsetY + 156).append(" ").append(itemResponse.getProductCategory()).append("\n");

            // WAS : Original Price
            content.append("T 5 0 ").append(startX + 19 + offsetX).append(" ")
                    .append(startY + offsetY + 351).append(" WAS :  ").append(formatNumber(itemResponse.getWasPrice())).append("\n");

            // NOW : Current Price
            content.append("T 5 0 ").append(startX + 19 + offsetX).append(" ")
                    .append(startY + offsetY + 381).append(" NOW :  ").append(formatNumber(itemResponse.getCurrentPrice())).append("\n");

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
                    .append(startY + offsetY + 340).append(" ")
                    .append(startX + 245 + offsetX).append(" ")
                    .append(startY + offsetY + 340).append(" 1\n");

            content.append("L ").append(startX + 15 + offsetX).append(" ")
                    .append(startY + offsetY + 435).append(" ")
                    .append(startX + 245 + offsetX).append(" ")
                    .append(startY + offsetY + 435).append(" 1\n");

            // Barcode (now inside every iteration)
            content.append("BARCODE 128 1 1 100 ")
                    .append(startX + 20 + offsetX).append(" ")
                    .append(startY + offsetY + 226).append(" ").append(itemResponse.getEanNumber()).append("\n");
        }

        return content.toString();
    }

    public String generateMango(int qty) {
        StringBuilder cpcl = new StringBuilder();

        // Page config
        int boxHeight = 440;
        int boxWidth = 264;
        int gapY = 44;

        int startY = 28;
        int labelHeight = boxHeight + gapY;

        for (int i = 0; i < qty; i++) {

            // Calculate X,Y based on left/right column and row
            boolean isLeft = (i % 2 == 0);
            int row = i / 2;

            int startX = isLeft ? 16 : 295;
            int boxEndX = startX + boxWidth;
            int startYRow = startY + row * labelHeight;
            int boxEndY = startYRow + boxHeight;

            int textX = startX + (isLeft ? 18 : 33); // adjusted based on original
            int descX = textX;
            int priceX = textX + 44;
            int barcodeX = textX + 5;

            // Add box
            cpcl.append(String.format("BOX %d %d %d %d 2\n", startX, startYRow, boxEndX, boxEndY));

            // Add texts
            cpcl.append(String.format("T 5 0 %d %d %s\n", textX, startYRow + 36, itemResponse.getVariant()));
            cpcl.append(String.format("T 5 0 %d %d %s\n", descX, startYRow + 71, itemResponse.getDescription()));
            cpcl.append(String.format("T 5 0 %d %d Rp. %s\n", priceX, startYRow + 276, formatNumber(itemResponse.getCurrentPrice())));
            cpcl.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, startYRow + 130, itemResponse.getEanNumber())); // placeholder
        }

        return cpcl.toString();
    }

    public String generateAlo(int qty) {
        StringBuilder cpcl = new StringBuilder();

        int labelWidth = 264;
        int labelHeight = 440;
        int rowGap = 47;

        for (int i = 0; i < qty; i++) {

            boolean isLeft = i % 2 == 0;
            int row = (i / 2) % 2;
            int page = i / 4;

            int xStart = isLeft ? 16 : 296;
            int xText = isLeft ? 74 : 354;
            int xField = isLeft ? 21 : 301;
            int xColon = isLeft ? 119 : 400;
            int xBoxEnd = xStart + labelWidth;

            int yBase = (page * 1200) + (row * (labelHeight + rowGap)) + 28;
            int yBoxEnd = yBase + labelHeight;

            // Print Title
            cpcl.append(String.format("T 5 2 %d %d alo\n", xStart + 105, yBase + 25));

            // Draw box
            cpcl.append(String.format("BOX %d %d %d %d 2\n", xStart, yBase, xBoxEnd, yBoxEnd));

            // Print fields
            cpcl.append(String.format("T 5 0 %d %d %s\n", xText, yBase + 226, itemResponse.getEanNumber()));
            cpcl.append(String.format("T 0 0 %d %d No. ARTIKEL\n", xField, yBase + 264));
            cpcl.append(String.format("T 0 0 %d %d : %s\n", xColon, yBase + 264, itemResponse.getVariant()));

            cpcl.append(String.format("T 0 0 %d %d UKURAN\n", xField, yBase + 285));
            cpcl.append(String.format("T 0 0 %d %d : %s\n", xColon, yBase + 285, itemResponse.getSize()));

            cpcl.append(String.format("T 0 0 %d %d WARNA\n", xField, yBase + 304));
            cpcl.append(String.format("T 0 0 %d %d : %s\n", xColon, yBase + 304, itemResponse.getColor()));

            cpcl.append(String.format("T 0 0 %d %d KATEGORI\n", xField, yBase + 324));
            cpcl.append(String.format("T 0 0 %d %d : %s\n", xColon, yBase + 324, itemResponse.getProductCategory()));

            cpcl.append(String.format("T 5 0 %d %d Rp. %s\n", xField + 86, yBase + 379, formatNumber(itemResponse.getCurrentPrice())));

            // BARCODE text
            int barcodeY = yBase + (isLeft ? 85 : 85); // adjust if needed
            int barcodeX = isLeft ? 51 : 335;
            cpcl.append(String.format("BARCODE 128 1 1 100  %d %d %s\n", barcodeX, barcodeY, itemResponse.getEanNumber()));
        }

        return cpcl.toString();
    }

    public String generatePriceSale(int qty) {
        StringBuilder content = new StringBuilder();

        // Configs
        int boxWidth = 264;
        int boxHeight = 120;
        int spacingY = 12;

        int startX1 = 20;
        int startX2 = 294;

        int startY = 13;

        // Text price padding
        int priceTextOffsetX = 70;
        int priceTextOffsetY1 = 32; // For top price
        int priceTextOffsetY2 = 167; // For bottom price

        // Vertical SALE label
        int saleTextXLeft = 22;
        int saleTextXRight = 296;
        int[] saleTextY = {104, 235, 369};

        // Vertical line
        int lineXLeft = 51;
        int lineXRight = 325;
        int[] lineYStart = {14, 147, 277};
        int[] lineYEnd = {132, 265, 395};

        for (int i = 0; i < qty; i++) {
            int col = i % 2; // 0 = left, 1 = right
            int row = i / 2;

            int x1 = (col == 0) ? startX1 : startX2;
            int y1 = startY + row * (boxHeight + spacingY);
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            // Draw box
            content.append(String.format("BOX %d %d %d %d 2\n", x1, y1, x2, y2));

            // Draw horizontal price text (top & bottom within box)
            int priceX = x1 + priceTextOffsetX;
            int topY = y1 + priceTextOffsetY1;
//            int bottomY = y1 + priceTextOffsetY2;
            content.append(String.format("T 5 1 %d %d %s\n", priceX, topY, formatNumber(itemResponse.getCurrentPrice())));
//            content.append(String.format("T 5 1 %d %d %s\n", priceX, bottomY, price));

            // Draw vertical "SALE" text
            int saleX = (col == 0) ? saleTextXLeft : saleTextXRight;
            int saleY = saleTextY[row];
            content.append(String.format("T90 5 0 %d %d SALE\n", saleX, saleY));

            // Draw vertical line
            int lineX = (col == 0) ? lineXLeft : lineXRight;
            int lineY1 = lineYStart[row];
            int lineY2 = lineYEnd[row];
            content.append(String.format("L %d %d %d %d 1\n", lineX, lineY1, lineX, lineY2));
        }

        return content.toString();
    }

    public String generatePriceRegular(int qty) {
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
            content.append(String.format("T 5 2 %d %d %s\n", textX, textY, formatNumber(itemResponse.getCurrentPrice())));
        }
        return content.toString();
    }

    public String formatNumber(String number) {
        double value = Double.parseDouble(number);  // Ubah string ke double
        // Atur simbol pemisah ribuan
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');

        // Buat format angka dengan pemisah ribuan
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        String formattedNumber = formatter.format(value);

        return formattedNumber; // Output: 1.234.567
    }
}