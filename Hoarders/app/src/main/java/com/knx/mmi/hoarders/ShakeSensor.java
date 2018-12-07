package com.knx.mmi.hoarders;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeSensor implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor acc;
    private IShakeSensor iShakeSensor;
    private final int ActivateShake = 5;

    private Float baseAcc;
    private Float baseX, baseY;


    private int shakeCounterHorz;
    private int shakeCounterVert;
    private boolean shakePositive;
    private final float shakeThresh = 2f;
    public final static int reqShakes = 5;
    public int shakeDir = 0;

    public static int HORZ = 1;
    public static int VERT = 2;

    private boolean isActive;

    private void resetShake(){
        baseAcc = null;
        shakeCounterHorz = 0;
        shakeCounterVert = 0;
        shakePositive = false;
        baseX = null;
        baseY = null;
    }

    public ShakeSensor(Context context, IShakeSensor iShakeSensor){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.iShakeSensor = iShakeSensor;

        isActive = false;
    }

    public void start(){
        if (isActive){
            return;
        }

        resetShake();

        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        isActive = true;
    }

    public void stop(){

        if (!isActive){
            return;
        }

        resetShake();

        sensorManager.unregisterListener(this);
        isActive = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float accX = event.values[0];
        float accY = event.values[1];
        float accZ = event.values[2];

        float accT = (float) Math.sqrt((accX*accX)+(accY*accY)+(accZ*accZ));

        if (baseAcc == null){
            baseAcc = new Float(accT);
            baseX = new Float(accX);
            baseY = new Float(accY);
            return;
        }

        accX -= baseX;
        accY -= baseY;

        Log.i("DEBUG", accX+"   :   "+accY+"   :   "+accZ);

        shakeDir = accX > accY ?
                HORZ : VERT;

        if (shakeDir == HORZ){
            if (accX > 3 && !shakePositive){
                shakePositive = true;
                ++shakeCounterHorz;
            } else {
                if (accX < -3 && shakePositive){
                    shakePositive = false;
                    ++shakeCounterHorz;
                }
            }
        } else {
            if (accY > 2 && !shakePositive){
                shakePositive = true;
                ++shakeCounterVert;
            } else {
                if (accY < -2 && shakePositive){
                    shakePositive = false;
                    ++shakeCounterVert;
                }
            }
        }

        iShakeSensor.shakeSensorUpdate(shakeCounterVert, shakeCounterHorz);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    interface IShakeSensor{
        void shakeSensorUpdate(int vertShakes, int horzShakes);
    }

    public void clearHorz(){
        shakeCounterHorz = 0;
    }

    public void clearVert(){
        shakeCounterVert = 0;
    }
}
