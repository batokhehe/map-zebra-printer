package com.werhoz.mapzebraprinter.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class AutoActivity extends AppCompatActivity {

    private int image;
    private String fileName;
    private String macAddress;
    private ImageView ivTemplate;
    private EditText etQty;
    private EditText etBarcode;
    private Button btnPrint;
    private View clContent;
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
        macAddress = Hawk.get("macAddress");

        ivTemplate = findViewById(R.id.iv_template);
        etQty = findViewById(R.id.et_qty);
        btnPrint = findViewById(R.id.btn_print);

        ivTemplate.setImageResource(image);
        btnPrint.setOnClickListener(v -> printToZebra());

        etVariant = findViewById(R.id.et_variant);
        etDescription = findViewById(R.id.et_desc);
        etCategory = findViewById(R.id.et_category);
        etEan = findViewById(R.id.et_ean);
        etWasPrice = findViewById(R.id.et_was_price);
        etCurrentPrice = findViewById(R.id.et_current_price);
        etBarcode = findViewById(R.id.et_barcode);
        clContent = findViewById(R.id.cl_content);

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        clContent.setVisibility(GONE);
        etBarcode.requestFocus();
        viewModel.getItemResponseLiveData().observe(this, item -> {
            itemResponse = item;
            if (item != null) {
                etVariant.setText(item.getVariant());
                etDescription.setText(item.getDescription());
                etCategory.setText(item.getProductCategory());
                etEan.setText(item.getEanNumber());
                etWasPrice.setText("Rp " + item.getWasPrice());
                etCurrentPrice.setText("Rp " + item.getCurrentPrice());
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

                viewModel.fetchItem(barcodeValue);

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
                    .append(startY + offsetY + 351).append(" WAS :  ").append(itemResponse.getWasPrice()).append("\n");

            // NOW : Current Price
            content.append("T 5 0 ").append(startX + 19 + offsetX).append(" ")
                    .append(startY + offsetY + 381).append(" NOW :  ").append(itemResponse.getCurrentPrice()).append("\n");

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
            cpcl.append(String.format("T 5 0 %d %d Rp. %s\n", priceX, startYRow + 276, itemResponse.getCurrentPrice()));
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

            cpcl.append(String.format("T 5 0 %d %d Rp. %s\n", xField + 86, yBase + 379, itemResponse.getCurrentPrice()));

            // BARCODE text
            int barcodeY = yBase + (isLeft ? 85 : 85); // adjust if needed
            int barcodeX = isLeft ? 51 : 335;
            cpcl.append(String.format("BARCODE 128 1 1 100  %d %d %s\n", barcodeX, barcodeY, itemResponse.getEanNumber()));
        }

        return cpcl.toString();
    }
}