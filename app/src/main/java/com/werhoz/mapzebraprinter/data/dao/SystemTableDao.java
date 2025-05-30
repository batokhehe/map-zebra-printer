package com.werhoz.mapzebraprinter.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.werhoz.mapzebraprinter.data.entity.SystemTableEntity;

import java.util.List;

@Dao
public interface SystemTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SystemTableEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SystemTableEntity> entities);

    @Query("SELECT * FROM SystemTable")
    List<SystemTableEntity> getAll();

    @Query("SELECT * FROM SystemTable WHERE keyValue = :id")
    SystemTableEntity getById(int id);

    @Query("SELECT * FROM SystemTable WHERE keyValue = :id1 OR keyValue = :id2 OR keyValue = :id3")
    SystemTableEntity getKeyValue(String id1, String id2, String id3);

    @Query("DELETE FROM SystemTable")
    void deleteAll();
}
