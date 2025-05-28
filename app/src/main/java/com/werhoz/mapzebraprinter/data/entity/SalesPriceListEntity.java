package com.werhoz.mapzebraprinter.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "SalesPriceList")
public class SalesPriceListEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemNumber;
    public String salesPrice;
    public String validFrom;
    public String company;
    public String entityCode1;
    public String salesCampaign;
}
