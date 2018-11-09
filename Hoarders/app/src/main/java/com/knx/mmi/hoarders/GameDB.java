package com.knx.mmi.hoarders;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.RoomDatabase;
import android.support.annotation.NonNull;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class GameDB extends RoomDatabase {
    public abstract GameDAO daoAccess();
}
