package com.werhoz.mapzebraprinter.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.werhoz.mapzebraprinter.data.dao.ProductDao;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;

@Database(entities = {ProductEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract ProductDao productDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "zmapfash")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
