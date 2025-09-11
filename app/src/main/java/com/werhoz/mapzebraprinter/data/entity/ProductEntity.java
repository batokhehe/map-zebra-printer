package com.werhoz.mapzebraprinter.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Product")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemNumber;
    public String description;
    public String name;
    public String aliasNumber;
    public String styleNo;
    public String configurationCode;
    public String qrCode;
    public String currency;
    public String itemGroup;
    public String salesPrice;
    public String wasPrice;
    public String size;
    public String color;
}