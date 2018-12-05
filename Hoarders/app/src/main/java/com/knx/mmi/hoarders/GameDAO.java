package com.knx.mmi.hoarders;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface GameDAO  {

    @Insert
    void insertNewProfile (UserEntity user);
    @Query("SELECT * FROM users where userMail =:email")
    UserEntity getUserByEmail(String email);
    @Query ("SELECT * FROM users")
    List<UserEntity> getAllUsers();
    @Update
    void updateUser(UserEntity user);
    @Delete
    void deleteLocalUserProfile(UserEntity user);

    @Insert
    void inserWorldEntity (WorldEntity worldEntity);
    @Query("SELECT * FROM world_entity")
    List<WorldEntity> getAllWorldEntities();
    @Query("SELECT * FROM world_entity where resourceId =:resourceId")
    WorldEntity getWorldEntityById(int resourceId);
    @Query("SELECT * FROM world_entity where spawn_time < :expiration")
    List<WorldEntity> getWorldEntitiesByExpired(long expiration);
    @Delete
    void deleteWorldEntityById(int resourceId);
    @Delete
    void deleteWorldEntities(WorldEntity... entities);

}
