package com.knx.mmi.hoarders;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {
    @NonNull
    @PrimaryKey
    private String userMail;
    @ColumnInfo(name = "user_name")
    private String userName;
    @ColumnInfo (name = "gold")
    private Integer gold;
    @ColumnInfo (name = "stone")
    private Integer stone;

    public UserEntity(){

    }

    public void setUserMail(String email){
        userMail = email;
    }

    public void setUserName(String name){
        userName = name;
    }

    public void setGold(Integer gold) {this.gold = gold;}

    public void setStone(Integer stone) {this.stone = stone;}

    public String getUserMail(){
        return userMail;
    }

    public String getUserName(){
        return userName;
    }

    public Integer getGold() {return gold;}

    public Integer getStone() {return stone;}
}
