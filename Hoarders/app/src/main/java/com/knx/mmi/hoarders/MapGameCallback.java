package com.knx.mmi.hoarders;

public interface MapGameCallback {
    void onMapReady();
    void onMarkerClick(int id);
    void onSpeech(String str);
}
