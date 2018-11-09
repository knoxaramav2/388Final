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

    public String getUserId(){
        return userId;
    }

    public String getUserName(){
        return userName;
    }
}
