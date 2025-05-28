package com.werhoz.mapzebraprinter.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.model.ResultModel;

@Dao
public interface ProductDao {

    @Query("SELECT p.itemNumber, p.description, p.dimensionX, p.dimensionYOptionID, " +
            "a.aliasNumber AS EAN, p.freeField4, s.description AS SystemDesc " +
            "FROM ProductRSF p " +
            "JOIN AliasNumber a ON p.itemNumber = a.itemNumber " +
            "LEFT JOIN SystemTable s ON p.itemGroup = s.keyValue " +
            "WHERE a.aliasNumber = :barcode " +
            "LIMIT 1")
    LiveData<ResultModel> getFirstProduct(String barcode);
}

