package com.tochange.yang.sector.background;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorEventListener
{
    private static final int SPEED_SHRESHOLD = 3000;

    private static final int UPTATE_INTERVAL_TIME = 70;

    private SensorManager sensorManager;

    private Sensor sensor;

    private ShakeInterface onShakeListener;

    private Context mContext;

    private float lastX;

    private float lastY;

    private float lastZ;

    private long lastUpdateTime;

    public ShakeListener(Context c)
    {
        mContext = c;
        start();
    }

    public void start()
    {
        if (sensorManager == null)
            sensorManager = (SensorManager) mContext
                    .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null)
        {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (sensor != null)
        {
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
            // log.e("register");
        }

    }

    public void stop()
    {
        sensorManager.unregisterListener(this);
        // log.e("unregister");
    }

    public void setOnShakeListener(ShakeInterface listener)
    {
        onShakeListener = listener;
    }

    public void onSensorChanged(SensorEvent event)
    {
        long currentUpdateTime = System.currentTimeMillis();
        long timeInterval = currentUpdateTime - lastUpdateTime;
        if (timeInterval < UPTATE_INTERVAL_TIME)
            return;
        lastUpdateTime = currentUpdateTime;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;

        lastX = x;
        lastY = y;
        lastZ = z;

        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeInterval * 10000;
        if (speed >= SPEED_SHRESHOLD)
        {
            try
            {
                onShakeListener.onShake();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

}