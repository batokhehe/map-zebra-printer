package com.werhoz.mapzebraprinter.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.BOEODTrnEntity;

import java.util.List;

@Dao
public interface BOEODTrnDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BOEODTrnEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BOEODTrnEntity> entities);

    @Query("SELECT * FROM BOEODTrn")
    List<BOEODTrnEntity> getAll();

    @Query("SELECT * FROM BOEODTrn WHERE warehouse = :id")
    BOEODTrnEntity getById(int id);

    @Query("DELETE FROM BOEODTrn")
    void deleteAll();
}
