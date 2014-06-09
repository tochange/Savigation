package com.tochange.yang.sector.screenobserver;

import java.lang.reflect.Method;

import com.tochange.yang.lib.log;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

/*
 * dynamic listen，screen on、off and system dialog close
 * do something in FloatWindowService.java
 * system dialog close cann't catch when screen was locked
 */
public class ScreenObserver
{
    private Context mContext;

    private ScreenBroadcastReceiver mScreenReceiver;

    private ScreenStateListener mScreenStateListener;

    private Method mReflectScreenState;

    public ScreenObserver(Context context)
    {
        mContext = context;
        mScreenReceiver = new ScreenBroadcastReceiver();
        try
        {
            mReflectScreenState = PowerManager.class.getMethod("isScreenOn",
                    new Class[] {});
        }
        catch (NoSuchMethodException nsme)
        {
            log.d("API < 7," + nsme);
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action))
            {
                mScreenStateListener.onScreenOn();
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action))
            {
                mScreenStateListener.onScreenOff();
            }
            else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action))
            {
                mScreenStateListener.onDialogClose(intent);
            }
        }
    }

    public void requestScreenStateUpdate(ScreenStateListener listener)
    {
        mScreenStateListener = listener;
        startScreenBroadcastReceiver();

        firstGetScreenState();
    }

    private void firstGetScreenState()
    {
        PowerManager manager = (PowerManager) mContext
                .getSystemService(Activity.POWER_SERVICE);
        if (mScreenStateListener != null)
        {
            if (isScreenOn(manager))
                mScreenStateListener.onScreenOn();
            else
                mScreenStateListener.onScreenOff();
        }
    }

    public void stopScreenObserver()
    {
        mContext.unregisterReceiver(mScreenReceiver);
    }

    private void startScreenBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);// yangxj@20140609
        // register in menifest file
        // filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenReceiver, filter);
    }

    private boolean isScreenOn(PowerManager pm)
    {
        boolean screenState;
        try
        {
            screenState = (Boolean) mReflectScreenState.invoke(pm);
        }
        catch (Exception e)
        {
            screenState = false;
        }
        return screenState;
    }

    public interface ScreenStateListener
    {
        public void onScreenOn();

        public void onScreenOff();

        public void onDialogClose(Intent intent);
        // public void onUserPresent();
    }
}