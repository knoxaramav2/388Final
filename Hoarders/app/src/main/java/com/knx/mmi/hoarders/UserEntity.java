package com.knx.mmi.hoarders;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class UserEntity {
    @NonNull
    @PrimaryKey
    private String userId;
    private String userName;

    public UserEntity(){

    }

    public void setUserId(String id){
        userId = id;
    }

    public void setUserName(String name){
        userName = name;
    }

    public String getUserId(){
        return userId;
    }

    public String getUserName(){
        return userName;
    }
}
