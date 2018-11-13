package com.knx.mmi.hoarders;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface GameDAO  {

    @Insert
    void insertNewProfile (UserEntity user);
    @Query("SELECT * FROM UserEntity where userId =:userId")
    UserEntity getUserById(int userId);
    @Update
    void updateUser(UserEntity user);
    @Delete
    void deleteLocalUserProfile(UserEntity user);

}
