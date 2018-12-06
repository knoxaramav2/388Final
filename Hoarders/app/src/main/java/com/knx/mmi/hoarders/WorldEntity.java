package com.knx.mmi.hoarders;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

@Entity(tableName = "world_entity")
public class WorldEntity {
    @NonNull
    @PrimaryKey
    private int resourceId;
    @ColumnInfo(name = "rsc_type")
    private String resourceType;
    @ColumnInfo(name = "lat")
    private Double latitude;
    @ColumnInfo(name = "lng")
    private Double longitude;
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

    public void setLatitude(Double latitude){
        this.latitude = latitude;
    }

    public Double getLatitude(){
        return latitude;
    }

    public void setLongitude(Double longitude){
        this.longitude = longitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    public void setSpawnTime(Long time){
        spawnTime = time;
    }

    public Long getSpawnTime(){
        return spawnTime;
    }
}
