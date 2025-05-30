package com.werhoz.mapzebraprinter.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AliasNumber")
public class AliasNumberEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemNumber;
    public String aliasCode;
    public String season;
    public String company;
}