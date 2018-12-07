package com.knx.mmi.hoarders;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class FireBaseUserModel {

    public String email;
    public Integer gold;
    public Integer stone;
    public String monuments;

    public FireBaseUserModel(String email, int gold, int stone, String monuments){
        this.email = email;
        this.gold = new Integer(gold);
        this.stone = new Integer(stone);
        this.monuments = monuments;
    }
}
