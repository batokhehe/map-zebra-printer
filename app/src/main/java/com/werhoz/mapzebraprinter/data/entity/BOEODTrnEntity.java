package com.werhoz.mapzebraprinter.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "BOEODTrn")
public class BOEODTrnEntity {
    @PrimaryKey (autoGenerate = true)
    public int id;

    public String warehouse;
    public String bODDate;
}