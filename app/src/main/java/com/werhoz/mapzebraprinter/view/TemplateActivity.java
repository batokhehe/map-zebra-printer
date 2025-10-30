package com.werhoz.mapzebraprinter.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.werhoz.mapzebraprinter.R;
import com.werhoz.mapzebraprinter.adapter.TemplateAdapter;
import com.werhoz.mapzebraprinter.data.model.TemplateModel;

import java.util.ArrayList;

public class TemplateActivity extends AppCompatActivity {

    private RecyclerView rvItems;
    private ArrayList<TemplateModel> dataList = new ArrayList<>();
    private TemplateAdapter adapter;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_template);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("MAP Zebra Printer - Choose Template");
        }

        Intent intentExtra = getIntent();
        type = intentExtra.getStringExtra("type");

        rvItems = findViewById(R.id.rv_template);
        rvItems.setLayoutManager(new GridLayoutManager(this, 3)); // 2 columns

        adapter = new TemplateAdapter(this, dataList, (model, position) -> {
            // Handle click
//            Toast.makeText(TemplateActivity.this, "Clicked position: " + model.fileName, Toast.LENGTH_SHORT).show();

            // Example: Open new activity and pass image resource
            Intent intent = null;
            if (type.equals("manual"))
                intent = new Intent(TemplateActivity.this, ManualActivity.class);
            else
                intent = new Intent(TemplateActivity.this, AutoActivity.class);
            intent.putExtra("template", model.fileName);
            intent.putExtra("image", model.imageResId);
            intent.putExtra("name", model.name);
            startActivity(intent);
        });
        rvItems.setAdapter(adapter);
        loadImages();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadImages() {
        // Add drawable images here
        dataList.clear();
        dataList.add(new TemplateModel(R.drawable.price_sale, "price_sale_even.zpl", "Price"));
        dataList.add(new TemplateModel(R.drawable.price_regular, "price_regular_even.zpl", "Header"));
        dataList.add(new TemplateModel(R.drawable.price_regular, "price_v_even.zpl", "Vertical"));
        dataList.add(new TemplateModel(R.drawable.price_sale, "price_sale_v_even.zpl", "Vertical Sale"));
        if (type.equals("auto")) {
            dataList.add(new TemplateModel(R.drawable.active, "active.zpl", "Fashion"));
            dataList.add(new TemplateModel(R.drawable.mango, "mango.zpl", "Mango"));
            dataList.add(new TemplateModel(R.drawable.alo, "alo.zpl", "Alo"));
        }
        adapter.notifyDataSetChanged();
    }
}