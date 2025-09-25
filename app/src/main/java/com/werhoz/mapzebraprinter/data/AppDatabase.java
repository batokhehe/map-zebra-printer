package com.werhoz.mapzebraprinter.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.werhoz.mapzebraprinter.data.dao.PriceDao;
import com.werhoz.mapzebraprinter.data.dao.ProductDao;
import com.werhoz.mapzebraprinter.data.entity.PriceEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;

@Database(entities = {ProductEntity.class, PriceEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract ProductDao productDao();

    public abstract PriceDao priceDao();

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
