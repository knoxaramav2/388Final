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

    private int shakeCounter;
    private boolean shakePositive;
    private final float shakeThresh = 2f;
    public final static int reqShakes = 5;

    private boolean isActive;

    private void resetShake(){
        baseAcc = null;
        shakeCounter = 0;
        shakePositive = false;
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

        //Log.i("DEBUG", "ACC"+accT);

        if (baseAcc == null){
            baseAcc = new Float(accT);
            return;
        }

        if (accT > (baseAcc + shakeThresh) && !shakePositive){
            shakePositive = true;
            shakeCounter++;
        } else if (accT < (baseAcc - shakeThresh) && shakePositive){
            shakePositive = false;
            shakeCounter++;
        }

        iShakeSensor.shakeSensorUpdate(shakeCounter);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    interface IShakeSensor{
        void shakeSensorUpdate(int shakes);
    }
}
