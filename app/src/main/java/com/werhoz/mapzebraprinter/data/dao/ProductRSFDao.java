package com.werhoz.mapzebraprinter.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.ProductRSFEntity;

import java.util.List;

@Dao
public interface ProductRSFDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductRSFEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductRSFEntity> entities);

    @Query("SELECT * FROM ProductRSF")
    List<ProductRSFEntity> getAll();

    @Query("SELECT * FROM ProductRSF WHERE itemNumber = :id")
    ProductRSFEntity getById(String id);

    @Query("DELETE FROM ProductRSF")
    void deleteAll();
}
