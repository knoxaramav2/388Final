package com.knx.mmi.hoarders;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {UserEntity.class, WorldEntity.class}, version = 3, exportSchema = false)
public abstract class GameDB extends RoomDatabase {

    private static GameDB INSTANCE;

    public abstract GameDAO daoAccess();

    public static GameDB getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, GameDB.class, Room.MASTER_TABLE_NAME)
                    .allowMainThreadQueries()//Yeah, yeah, I know
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return INSTANCE;
    }
}
