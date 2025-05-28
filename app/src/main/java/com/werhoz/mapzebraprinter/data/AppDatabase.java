package com.werhoz.mapzebraprinter.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.werhoz.mapzebraprinter.data.dao.AliasNumberDao;
import com.werhoz.mapzebraprinter.data.dao.BOEODTrnDao;
import com.werhoz.mapzebraprinter.data.dao.ProductDao;
import com.werhoz.mapzebraprinter.data.dao.ProductRSFDao;
import com.werhoz.mapzebraprinter.data.dao.SalesPriceListDao;
import com.werhoz.mapzebraprinter.data.dao.SystemTableDao;
import com.werhoz.mapzebraprinter.data.entity.AliasNumberEntity;
import com.werhoz.mapzebraprinter.data.entity.BOEODTrnEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductRSFEntity;
import com.werhoz.mapzebraprinter.data.entity.SalesPriceListEntity;
import com.werhoz.mapzebraprinter.data.entity.SystemTableEntity;

@Database(entities = {AliasNumberEntity.class, BOEODTrnEntity.class, ProductRSFEntity.class, SalesPriceListEntity.class, SystemTableEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract AliasNumberDao aliasNumberDao();

    public abstract BOEODTrnDao bOEODTrnDao();

    public abstract ProductRSFDao productRSFDao();

    public abstract SalesPriceListDao salesPriceListDao();

    public abstract SystemTableDao systemTableDao();

    public abstract ProductDao productDao();

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
