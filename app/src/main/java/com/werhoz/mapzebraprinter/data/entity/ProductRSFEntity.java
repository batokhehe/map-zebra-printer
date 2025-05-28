package com.werhoz.mapzebraprinter.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ProductRSF")
public class ProductRSFEntity {
    @PrimaryKey
    @NonNull
    public String itemNumber;

    public String description;
    public String name;
    public String styleNo;
    public String configurationCode;
    public String dimensionX;
    public String dimensionYOptionID;
    public String itemGroup;
    public String freeField4;
    public String company;
}