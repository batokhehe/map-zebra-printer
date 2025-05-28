package com.werhoz.mapzebraprinter.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.SalesPriceListEntity;

import java.util.List;

@Dao
public interface SalesPriceListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SalesPriceListEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SalesPriceListEntity> entities);

    @Query("SELECT * FROM SalesPriceList")
    List<SalesPriceListEntity> getAll();

    @Query("SELECT * FROM SalesPriceList WHERE itemNumber = :id")
    List<SalesPriceListEntity> getById(String id);

    @Query("DELETE FROM SalesPriceList")
    void deleteAll();
}
