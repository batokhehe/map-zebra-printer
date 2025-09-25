package com.werhoz.mapzebraprinter.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.ProductEntity;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductEntity> entities);

    @Query("SELECT * FROM products")
    List<ProductEntity> getAll();

    @Query("SELECT COUNT(*) FROM products")
    int getAllCount();

    @Query("SELECT * FROM products WHERE itemNumber = :id")
    ProductEntity getById(String id);

    @Query("SELECT * FROM products WHERE aliasNumber = :alias")
    ProductEntity getByAlias(String alias);

    @Query("DELETE FROM products")
    void deleteAll();
}

