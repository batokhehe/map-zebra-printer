package com.werhoz.mapzebraprinter.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.werhoz.mapzebraprinter.data.dao.UserDao;
import com.werhoz.mapzebraprinter.data.entity.UserEntity;

@Database(entities = {UserEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "map_zebra")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
