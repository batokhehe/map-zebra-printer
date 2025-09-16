package com.werhoz.mapzebraprinter.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.viewmodel.DataViewModel;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.PrinterLanguage;
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
    private Button btnBack;
    private View clContent;
    private View progressBar;
    private TextView etVariant;
    private TextView etDescription;
    private TextView etCategory;
    private TextView etEan;
    private TextView etWasPrice;
    private TextView etCurrentPrice;
    private DataViewModel viewModel;
    private ResultModel itemResponse;
    private int MAX_CHARS_PER_LINE = 21;

    private ScrollView mainLayout;

    private EditText etHeader;
    private TextInputLayout tilHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auto);

        mainLayout = findViewById(R.id.main);

        Intent intentExtra = getIntent();
        image = intentExtra.getIntExtra("image", 0);
        fileName = intentExtra.getStringExtra("template");
        name = intentExtra.getStringExtra("name");
        macAddress = Hawk.get("macAddress");

        etTemplate = findViewById(R.id.et_template);
        etQty = findViewById(R.id.et_qty);
        btnPrint = findViewById(R.id.btn_print);
        btnBack = findViewById(R.id.btn_back);

        tilHeader = findViewById(R.id.til_header);
        etHeader = findViewById(R.id.et_header);
        tilHeader.setVisibility(!fileName.contains("regular") && !fileName.contains("active") ? GONE : VISIBLE);

        etTemplate.setText(name);
        btnPrint.setOnClickListener(v -> {
            btnPrint.setEnabled(false);
            Toast.makeText(this, "Printing, please wait...", Toast.LENGTH_SHORT).show();
            printToZebra();
//            new Handler(Looper.getMainLooper()).postDelayed(this::printToZebra, 1000);
        });

        btnBack.setOnClickListener(v -> {
            finish();
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("MAP Zebra Printer - Print");
        }

        viewModel = new ViewModelProvider(this).get(DataViewModel.class);

        clContent.setVisibility(GONE);
        etBarcode.requestFocus();
//        viewModel.getItemResponseLiveData().observe(this, item -> {
//            progressBar.setVisibility(GONE);
//            itemResponse = item;
//            if (item != null) {
//                etVariant.setText(item.ItemNumber);
//                etDescription.setText(item.Description);
//                etCategory.setText(item.ProductCategory);
//                etEan.setText(item.EANNumber);
//                etWasPrice.setText("Rp " + formatNumber(item.WasPrice));
//                etCurrentPrice.setText("Rp " + formatNumber(item.CurrentPrice));
//                clContent.setVisibility(VISIBLE);
//                etQty.requestFocus();
//            } else {
//                clContent.setVisibility(GONE);
//                Toast.makeText(this, "Failed to load item", Toast.LENGTH_SHORT).show();
//            }
//        });

        viewModel.getItemResponseLiveData().observe(this, item -> {
            progressBar.setVisibility(GONE);
            itemResponse = item;
            if (item != null) {
                showResult(true);
                etVariant.setText(item.itemNumber);
                etDescription.setText(item.description);
                etCategory.setText(item.productCategory);
                etEan.setText(item.eANNumber);
                etWasPrice.setText(item.currency + " " + formatNumber(item.wasPrice));
                etCurrentPrice.setText(item.currency + " " + formatNumber(item.currentPrice));
                clContent.setVisibility(VISIBLE);
                if (!fileName.contains("regular"))
                    etQty.requestFocus();
                else etHeader.requestFocus();
            } else {
                showResult(false);
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
                    viewModel.getProduct(barcodeValue);
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

//            String cpclCommand = "! U1 setvar \"media.type\" \"gap\"\n" +   // set media type
//                    "! U1 setvar \"media.clear\" \"\"\n" +     // clear buffer
//                    "! U1 setvar \"media.calibrate\" \"\"\n" +     // clear buffer
//                    "! U1 do \"feed\"\n";                      // feed one label

//            connection.write(cpclCommand.getBytes());

//            Log.d("Zebra", "Buffer cleared.");

            // Create a ZebraPrinter instance
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

//            InputStream is = getAssets().open("logo_alo.pcx");
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//
//            String header = "! 0 200 200 400 1\n" +
//                    "PCX 100 50\n";
//
//            String footer = "PRINT\n";
//
//            connection.write(header.getBytes("US-ASCII"));
//            connection.write(buffer);
//            connection.write(footer.getBytes("US-ASCII"));
//            return;

//            if (fileName.contains("alo")) {
//                InputStream is = getAssets().open("logo_alo.png");
//                Bitmap bitmap = BitmapFactory.decodeStream(is);
//
//                bitmap = Bitmap.createScaledBitmap(bitmap, 300, 150, false);
//
//                int paperWidth = 576;
//                int xPos = (paperWidth - bitmap.getWidth()) / 2;
//                int yPos = 20;
//
//                ZebraImageAndroid zebraImage = new ZebraImageAndroid(bitmap);
//                printer.printImage(zebraImage, xPos, yPos, bitmap.getWidth(), bitmap.getHeight(), false);
//            }
//
            // Send the CPCL data directly to the printer without setting language
            String cpcl = loadZpl(this);
            String content = generateContent(qty);

            cpcl = cpcl.replace("{CONTENT}", content);
            if (fileName.contains("sale") || fileName.contains("regular"))
                cpcl = cpcl.replace("{height}", String.valueOf(150 * (int) Math.ceil(qty / 2.0)));
            else
                cpcl = cpcl.replace("{height}", String.valueOf(480 * (int) Math.ceil(qty / 2.0)));
//            cpcl = cpcl.replace("{qty}", String.valueOf((int) Math.ceil(qty / 2.0)));

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
        etBarcode.setText("");
        etBarcode.requestFocus();
        clContent.setVisibility(GONE);
    }


    public String generateContent(int qty) throws IOException {
        if (fileName.contains("active")) return generateActive(qty);
        if (fileName.contains("alo")) return generateAlo(qty);
        if (fileName.contains("sale")) return generatePrice(qty);
        if (fileName.contains("regular")) return generatePriceHeader(qty);
        return generateMango(qty);
    }

    public String generateActive(int qty) {
        int startX = 7;
        int startY = 10;
        int boxWidth = 264;
        int boxHeight = 440;
        int columnSpacing = 32; // jarak antar kolom
        int rowSpacing = 30;    // jarak antar baris

        StringBuilder content = new StringBuilder();

        String header = etHeader.getText().toString();

        double wasPrice = parseDouble(itemResponse.wasPrice);
        double nowPrice = parseDouble(itemResponse.currentPrice);

        for (int i = 0; i < qty; i++) {
            int col = i % 2;        // kolom kiri/kanan
            int row = i / 2;        // baris ke-
            int offsetX = col * (columnSpacing + boxWidth);
            int offsetY = row * (boxHeight + rowSpacing);

            int x1 = startX + offsetX;
            int y1 = startY + offsetY;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            // Garis dalam box
            content.append(String.format("L %d %d %d %d 1\n", x1 + 15, y1 + 46, x1 + 248, y1 + 46));
            content.append(String.format("L %d %d %d %d 1\n", x1 + 33, y1 + 146, x1 + 236, y1 + 146));
            content.append(String.format("L %d %d %d %d 1\n", x1 + 15, y1 + 190, x1 + 248, y1 + 190));
            content.append(String.format("L %d %d %d %d 1\n", x1 + 19, y1 + 340, x1 + 245, y1 + 340));
            content.append(String.format("L %d %d %d %d 1\n", x1 + 15, y1 + 435, x1 + 245, y1 + 435));

            // Variant/Single Article
            int variantX = x1 + ((boxWidth - (itemResponse.itemNumber.length() * 12)) / 2);
            content.append(String.format("T 7 0 %d %d %s\n", variantX, y1 + 6, itemResponse.itemNumber));

            // Description (wrap text)
            content.append(wrapText(itemResponse.description, 55, 15, startX, y1, offsetX, 15)).append("\n");

            // Category
            String category = itemResponse.productCategory + " - " + header;
            int categoryX = x1 + ((boxWidth - (category.length() * 12)) / 2);
            content.append(String.format("T 7 0 %d %d %s\n", categoryX, y1 + 156, itemResponse.productCategory));

            // WAS price
            if (wasPrice > nowPrice) {
                String priceWas = itemResponse.currency + " " + formatNumber(itemResponse.wasPrice);
                content.append(String.format("T 0 2 %d %d WAS :  %s\n", x1 + 19, y1 + 351, priceWas));
                content.append(String.format("LINE %d %d %d %d 1\n", x1 + 50, y1 + 361, x1 + (priceWas.length() * 16), y1 + 361));
            }
            // NOW price
            content.append(String.format("T 0 2 %d %d NOW :  %s %s\n", x1 + 19, y1 + 381, itemResponse.currency, formatNumber(itemResponse.currentPrice)));

            // Barcode
            int barcodeX = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 18)) / 2);
            content.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, y1 + 200, itemResponse.eANNumber));

            // Barcode Text
            int xText = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 12)) / 2);
            content.append(String.format("T 5 0 %d %d %s\n", xText, y1 + 310, itemResponse.eANNumber));
        }

        return content.toString();
    }


    public String generateMango(int qty) {
        int startX = 7;
        int startY = 10;
        int boxWidth = 264;
        int boxHeight = 440;
        int columnSpacing = 32; // jarak antar kolom
        int rowSpacing = 30;    // jarak antar baris

        StringBuilder content = new StringBuilder();

        double wasPrice = parseDouble(itemResponse.wasPrice);
        double nowPrice = parseDouble(itemResponse.currentPrice);

        for (int i = 0; i < qty; i++) {
            int col = i % 2;        // kolom kiri/kanan
            int row = i / 2;        // baris ke-
            int offsetX = col * (columnSpacing + boxWidth);
            int offsetY = row * (boxHeight + rowSpacing);

            int x1 = startX + offsetX;
            int y1 = startY + offsetY;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            // Variant/Article (item number)
            int variantX = x1 + ((boxWidth - (itemResponse.itemNumber.length() * 12)) / 2);
            content.append(String.format("T 7 0 %d %d %s\n", variantX, y1 + 36, itemResponse.itemNumber));

            // Description (wrap text)
            int descX = x1 + 7;
            int index = 0;
            String text = itemResponse.description;
            int yText = y1 + 75;
            while (index < text.length()) {
                int end = Math.min(index + MAX_CHARS_PER_LINE, text.length());
                String line = text.substring(index, end);
                content.append(String.format("T 7 0 %d %d %s\n", descX, yText, line));
                yText += 30;
                index += MAX_CHARS_PER_LINE;
            }

            // Price
            if (wasPrice > nowPrice) {
                String priceWas = "WAS: " + itemResponse.currency + " " + formatNumber(itemResponse.wasPrice);
                content.append(String.format("T 0 2 %d %d %s\n", x1 + 10, y1 + 310, priceWas));
                content.append(String.format("LINE %d %d %d %d 1\n", x1 + 10 + 50, y1 + 320, x1 + (priceWas.length() * 16), y1 + 320));
            }
            String priceNow = "NOW: " + itemResponse.currency + " " + formatNumber(itemResponse.currentPrice);
            content.append(String.format("T 0 2 %d %d %s\n", x1 + 10, y1 + 330, priceNow));

            // Barcode
            int barcodeX = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 18)) / 2);
            content.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, y1 + 150, itemResponse.eANNumber));

            // Barcode Text
            int xText = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 12)) / 2);
            content.append(String.format("T 5 0 %d %d %s\n", xText, y1 + 260, itemResponse.eANNumber));
        }

        return content.toString();
    }

    public String generateAlo(int qty) throws IOException {
        int startX = 7;
        int startY = 8;
        int boxWidth = 264;
        int boxHeight = 440;
        int columnSpacing = 32; // jarak antar kolom
        int rowSpacing = 30;    // jarak antar baris


        StringBuilder cpcl = new StringBuilder();

        for (int i = 0; i < qty; i++) {
            int col = i % 2;        // kiri/kanan
            int row = (i / 2) % 2;  // baris ke-0/1 dalam 1 halaman

            int offsetX = col * (boxWidth + columnSpacing);
            int offsetY = row * (boxHeight + rowSpacing);

            int x1 = startX + offsetX;
            int y1 = startY + offsetY;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            // Titik acuan teks
            int xField = (col == 0) ? 10 : 305;
            int xColon = (col == 0) ? 100 : 395;

            // Print Title
//            cpcl.append(String.format("T 5 2 %d %d alo\n", x1 + 105, y1 + 25));

//            cpcl.append("BITMAP ").append(x1 + 105).append(" ").append(y1 + 25).append(" ")
//                    .append(widthBytes).append(" ").append(height).append(" ")
//                    .append(dataLength).append("\n");

            // Print fields
            int xText = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 10)) / 2);
            cpcl.append(String.format("T 5 0 %d %d %s\n", xText, y1 + 205, itemResponse.eANNumber));
            cpcl.append(String.format("T 0 0 %d %d No. ARTIKEL\n", xField, y1 + 264));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 264, itemResponse.itemNumber));

            cpcl.append(String.format("T 0 0 %d %d UKURAN\n", xField, y1 + 285));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 285, itemResponse.size));

            cpcl.append(String.format("T 0 0 %d %d WARNA\n", xField, y1 + 304));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 304, itemResponse.color));

            cpcl.append(String.format("T 0 0 %d %d KATEGORI\n", xField, y1 + 324));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 324, itemResponse.productCategory));

            // Harga
            String price = formatNumber(itemResponse.currentPrice);
            int priceX = xField + ((boxWidth - (price.length() * 20)) / 2);
            cpcl.append(String.format("T 5 0 %d %d %s %s\n", priceX, y1 + 379, itemResponse.currency, price));

            // BARCODE
            int barcodeX = x1 + ((boxWidth - 150) / 2);
            int barcodeY = y1 + 85;
            cpcl.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, barcodeY, itemResponse.eANNumber));
        }

        return cpcl.toString();
    }

    public String generatePrice(int qty) {
        StringBuilder content = new StringBuilder();

        int boxWidth = 264;
        int boxHeight = 120;
        int gapY = 24;
        int[] startX = {9, 305}; // kiri & kanan
        String price = itemResponse.currency + " " + formatNumber(itemResponse.currentPrice);

        int fontWidthEstimate = price.length() * 12;

        for (int i = 0; i < qty; i++) {
            int col = i % 2;       // kolom
            int row = i / 2;       // baris
            int x1 = startX[col];
            int x2 = x1 + boxWidth;
            int y = 14 + row * (boxHeight + gapY);   // ✅ fix perhitungan y

            // Text price offset
            int priceTextOffsetY = y + 25;

            // PRICE text
            int priceX = x1 + ((boxWidth + 50 - fontWidthEstimate) / 2);
            content.append(String.format("T 5 1 %d %d %s\n", priceX, priceTextOffsetY, price));

            // Vertical "SALE"
//            content.append(String.format("T90 7 0 %d %d SALE\n", x1 + 8, y + (boxHeight - 30)));
//
//            // Vertical line
//            content.append(String.format("L %d %d %d %d 1\n", x1 + 35, y, x1 + 35, y + boxHeight));
        }

        return content.toString();
    }

    public String generatePriceHeader(int qty) {
        StringBuilder content = new StringBuilder();

        int boxWidth = 264;
        int boxHeight = 120;
        int gapY = 24;
        int[] startX = {9, 305}; // kiri & kanan
        String price = itemResponse.currency + " " + formatNumber(itemResponse.currentPrice);

        int fontWidthEstimate = price.length() * 12;

        String header = etHeader.getText().toString();

        for (int i = 0; i < qty; i++) {
            int col = i % 2;       // kolom
            int row = i / 2;       // baris
            int x1 = startX[col];
            int x2 = x1 + boxWidth;
            int y = 14 + row * (boxHeight + gapY);   // ✅ perhitungan Y atas
            int y2 = y + boxHeight;                  // ✅ Y bawah

            // Text price offset
            int priceTextOffsetY = y + 20;

            // PRICE text
            int priceX = x1 + ((boxWidth - fontWidthEstimate) / 2);
            content.append(String.format("T 5 1 %d %d %s\n", priceX, priceTextOffsetY, header));
            content.append(String.format("T 5 1 %d %d %s\n", priceX, priceTextOffsetY + 40, price));
        }
        return content.toString();
    }

    public String formatNumber(String number) {
        double value = Double.parseDouble(number.replace(",", "."));  // Ubah string ke double
        // Atur simbol pemisah ribuan
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');

        // Buat format angka dengan pemisah ribuan
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        String formattedNumber = formatter.format(value);

        return formattedNumber; // Output: 1.234.567
    }

    public Double parseDouble(String number) {
        return Double.parseDouble(number.replace(",", "."));
    }

    public String wrapText(String text, int startYPos, int startXPos, int startX, int startY, int offsetX, int offsetY) {
        StringBuilder wrapped = new StringBuilder();
        int y = startY + offsetY + startYPos; // starting Y position for the first line
        int index = 0;

        while (index < text.length()) {
            int end = Math.min(index + MAX_CHARS_PER_LINE, text.length());
            String line = text.substring(index, end);
            wrapped.append("T 7 0 ")
                    .append(startX + startXPos + offsetX).append(" ")
                    .append(y).append(" ")
                    .append(line).append("\n");
            y += 30;
            index += MAX_CHARS_PER_LINE;
        }

        return wrapped.toString();
    }

    private void animateBackground(int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, toColor, Color.WHITE);
        colorAnimation.setDuration(1000);
        colorAnimation.addUpdateListener(animator ->
                mainLayout.setBackgroundColor((int) animator.getAnimatedValue())
        );
        colorAnimation.start();
    }


    private void showResult(boolean success) {
        if (success) {
            animateBackground(Color.parseColor("#00FF00"));
//            soundPool.play(soundSuccess, 1, 1, 0, 0, 1);
        } else {
            animateBackground(Color.parseColor("#FF0000"));
//            soundPool.play(soundFailed, 1, 1, 0, 0, 1);
        }
    }
}