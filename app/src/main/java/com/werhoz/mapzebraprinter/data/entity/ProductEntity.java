package com.werhoz.mapzebraprinter.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Product")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String ItemNumber;
    public String Description;
    public String Name;
    public String AliasNumber;
    public String StyleNo;
    public String ConfigurationCode;
    public String QRCode;
    public String Currency;
    public String ItemGroup;
    public String SalesPrice;
    public String Size;
    public String Color;
}