package com.werhoz.mapzebraprinter.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.formatNumber;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generateActive;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generateAlo;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generateMango;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceSale;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceHeader;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceSaleVertical;
import static com.werhoz.mapzebraprinter.utils.TemplateGenerator.generatePriceVertical;

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
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.io.InputStream;

import taimoor.sultani.sweetalert2.Sweetalert;

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

    private ScrollView mainLayout;

    private EditText etHeader;
    private TextInputLayout tilHeader;
    private Sweetalert pDialog;


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
        tilHeader.setVisibility(!fileName.contains("price_sale_even.zpl") && !fileName.contains("mango") &&
                !fileName.contains("alo") ? VISIBLE : GONE);

        etTemplate.setText(name);
        btnPrint.setOnClickListener(v -> {
            btnPrint.setEnabled(false);
            pDialog = new Sweetalert(AutoActivity.this, Sweetalert.PROGRESS_TYPE);
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
                if (!fileName.contains("price_sale_even.zpl"))
                    etHeader.requestFocus();
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
            String content = generateContent(cpcl, qty);

            cpcl = cpcl.replace("{CONTENT}", content);
            if (fileName.contains("sale") || fileName.contains("regular") || fileName.contains("_v"))
                cpcl = cpcl.replace("{height}", String.valueOf(150 * (int) Math.ceil(qty / 2.0)));
            else
                cpcl = cpcl.replace("{height}", String.valueOf(480 * (int) Math.ceil(qty / 2.0)));
//            cpcl = cpcl.replace("{qty}", String.valueOf((int) Math.ceil(qty / 2.0)));

            printer.sendCommand(cpcl);  // Sending CPCL command

//            Toast.makeText(this, "Print job sent.", Toast.LENGTH_SHORT).show();


            // Setelah selesai, update UI di main thread
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } finally {
            runOnUiThread(() -> {
                btnPrint.setEnabled(true);
                resetForm();
                pDialog.dismiss();
                btnPrint.setEnabled(true);
                Toast.makeText(this, "Print job sent.", Toast.LENGTH_SHORT).show();
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
        etBarcode.setText("");
        etBarcode.requestFocus();
        clContent.setVisibility(GONE);
    }


    public String generateContent(String content, int qty) throws IOException {
        String price = itemResponse.currency + " " + formatNumber(itemResponse.currentPrice);
        String header = etHeader.getText().toString();

        if (fileName.contains("price_v")) return generatePriceVertical(content, price, header);
        if (fileName.contains("price_sale_v")) return generatePriceSaleVertical(content, price, header);
        if (fileName.contains("active")) return generateActive(qty, itemResponse, header);
        if (fileName.contains("alo")) return generateAlo(qty, itemResponse);
        if (fileName.contains("sale")) return generatePriceSale(content, price);
        if (fileName.contains("regular")) return generatePriceHeader(content, price, header);
        return generateMango(qty, itemResponse);
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