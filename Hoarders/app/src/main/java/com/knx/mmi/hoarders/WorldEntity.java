package com.knx.mmi.hoarders;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

@Entity(tableName = "world_entity")
public class WorldEntity {
    @NonNull
    private int resourceId;
    @ColumnInfo(name = "rsc_type")
    private String resourceType;
    @ColumnInfo(name = "lat")
    private String latitude;
    @ColumnInfo(name = "lng")
    private String longitude;
    @ColumnInfo (name = "spawn_time")
    private Long spawnTime;

    public WorldEntity(){}

    public void setResourceId(int resourceId){
        this.resourceId = resourceId;
    }

    public int getResourceId(){
        return resourceId;
    }

    public void setResourceType(String resourceType){
        this.resourceType = resourceType;
    }

    public String getResourceType(){
        return resourceType;
    }

    public void setLatitude(String latitude){
        this.latitude = latitude;
    }

    public String getLatitude(){
        return latitude;
    }

    public void setLongitude(String longitude){
        this.longitude = longitude;
    }

    public String getLongitude(){
        return longitude;
    }

    public void setSpawnTime(Long time){
        spawnTime = time;
    }

    public Long getSpawnTime(){
        return spawnTime;
    }
}
