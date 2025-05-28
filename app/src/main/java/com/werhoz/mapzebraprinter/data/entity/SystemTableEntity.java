package com.werhoz.mapzebraprinter.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "SystemTable")
public class SystemTableEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String keyValue;
    public String description;
}
