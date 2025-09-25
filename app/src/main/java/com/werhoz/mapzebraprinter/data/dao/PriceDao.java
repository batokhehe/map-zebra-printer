package com.werhoz.mapzebraprinter.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.PriceEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;

import java.util.List;

@Dao
public interface PriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PriceEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PriceEntity> entities);

    @Query("SELECT * FROM prices")
    List<PriceEntity> getAll();

    @Query("SELECT COUNT(*) FROM prices")
    int getAllCount();

    @Query("SELECT * FROM prices WHERE itemNumber = :id")
    PriceEntity getById(String id);

    @Query("SELECT * FROM prices WHERE itemNumber = :itemNumber ORDER BY rowNum ASC LIMIT 1")
    PriceEntity getFirstByItemNumber(String itemNumber);

    @Query("SELECT * FROM prices WHERE itemNumber = :itemNumber ORDER BY rowNum DESC LIMIT 1")
    PriceEntity getLastByItemNumber(String itemNumber);

    @Query("DELETE FROM Prices")
    void deleteAll();
}

