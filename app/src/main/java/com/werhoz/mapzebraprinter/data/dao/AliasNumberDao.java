package com.werhoz.mapzebraprinter.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.AliasNumberEntity;

import java.util.List;

@Dao
public interface AliasNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AliasNumberEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AliasNumberEntity> entities);

    @Query("SELECT * FROM AliasNumber")
    List<AliasNumberEntity> getAll();

    @Query("SELECT * FROM AliasNumber WHERE itemNumber = :id")
    AliasNumberEntity getById(int id);

    @Query("DELETE FROM AliasNumber")
    void deleteAll();
}
