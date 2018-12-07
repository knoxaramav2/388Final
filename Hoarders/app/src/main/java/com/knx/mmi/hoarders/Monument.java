package com.knx.mmi.hoarders;

import java.util.List;

public class Monument{
    double lat, lng;
    int id;
    String title;

    Monument(String title, double lat, double lng, int id){
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.id = id;
    }

    public static Monument monumentFromString(String str){
        String[] list = str.split(",");

        if (list.length < 4){
            return null;
        }

        String title = list[0];
        double lat = Double.parseDouble(list[1]);
        double lng = Double.parseDouble(list[2]);
        int id = Integer.parseInt(list[3]);

        return new Monument(title, lat, lng, id);
    }

    @Override
    public String toString(){
        return title+','+lat+','+lng+','+id;
    }

}