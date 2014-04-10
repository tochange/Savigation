package com.tochange.yang.sector.tools;

import java.lang.reflect.Method;
import java.util.List;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.tochange.yang.log;
import com.tochange.yang.sector.tools.screenshot.ScreenShotActivity;

public class BackPanelBin
{
    private static Context mContext;

    private static BluetoothAdapter adapter;

    private static WifiManager wifiManager;

    private static int currentBrightness;

    private static AudioManager audioManager;

    private static ConnectivityManager mConnectivityManager;

    private LocationManager locationManager;

    private Camera mCamera;

    public void detach()
    {
        if (null != mCamera)
            mCamera.release();
    }

    public BackPanelBin(Context c)
    {
        mContext = c;
        wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        adapter = BluetoothAdapter.getDefaultAdapter();
        locationManager = (LocationManager) c
                .getSystemService(c.LOCATION_SERVICE);
        audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        mCamera = Camera.open();
    }

    public void doit(int type) throws SettingNotFoundException
    {
        // log.e("type =" + type);
        // index = 128;
        switch (type)
        {
            case AppUtils.SECONDPANELKEY_WIFI:
                wifiManager.setWifiEnabled(!wiFiIsAbled());
                break;
            case AppUtils.SECONDPANELKEY_BLUETOOTH:
                if (blueToothIsAbled())
                    adapter.disable();
                else
                    adapter.enable();
                break;
            case AppUtils.SECONDPANELKEY_GPS:
                boolean b = gpsIsAbled();
                // if (b)
                // turnGPSOff();//no work
                // else
                // turnGPSOn();
                // toggleGps(!b);//no work
                openGPSSetting();
                break;
            case AppUtils.SECONDPANELKEY_BRIGHTNESS:
                brightnessIsAbled();
                toggleBrightness();
                break;
            case AppUtils.SECONDPANELKEY_RING:
                if (ringIsAbled())
                    audioManager
                            .setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                else
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;

            case AppUtils.SECONDPANELKEY_AIRPLANMODE:
                setAirplaneMode(!airplanModelAble());
                break;
            case AppUtils.SECONDPANELKEY_GPRS:
                toggleGPRS("setMobileDataEnabled",
                        gprsIsAbled("getMobileDataEnabled"));
                break;
            case AppUtils.SECONDPANELKEY_FLASHLIGHT:
                toggleFlashLight(mCamera);
                break;
            case AppUtils.SECONDPANELKEY_SCREENORIENTATION:
                int i = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, -1);
                if (0 == i)
                    i += 1;
                else if (1 == i)
                    i -= 1;
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, i);
                break;
            case AppUtils.SECONDPANELKEY_SCREENSHOT:
                Intent it = new Intent(mContext, ScreenShotActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
                break;
        }
    }

    public static boolean isAble(int value)
    {
        switch (value)
        {
            // several type just suppose to be true
            case AppUtils.SECONDPANELKEY_WIFI:
                return wiFiIsAbled();
            case AppUtils.SECONDPANELKEY_BLUETOOTH:
                return blueToothIsAbled();
            case AppUtils.SECONDPANELKEY_GPS:
                return gpsIsAbled();
            case AppUtils.SECONDPANELKEY_BRIGHTNESS:
                return brightnessIsAbled();
            case AppUtils.SECONDPANELKEY_RING:
                return ringIsAbled();
            case AppUtils.SECONDPANELKEY_AIRPLANMODE:
                return airplanModelAble();
            case AppUtils.SECONDPANELKEY_GPRS:
                return gprsIsAbled("getMobileDataEnabled");
            case AppUtils.SECONDPANELKEY_SCREENORIENTATION:
                return Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, -1) == 1;
            default:
                return true;
        }

    }

    public static boolean wiFiIsAbled()
    {
        return wifiManager.isWifiEnabled();
    }

    public static boolean blueToothIsAbled()
    {

        if (adapter.getState() == BluetoothAdapter.STATE_OFF)
            return false;
        else if (adapter.getState() == BluetoothAdapter.STATE_ON)
            return true;
        return false;

    }

    public static boolean ringIsAbled()
    {
        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_NORMAL)
            return true;
        else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
            return false;
        return false;
    }

    public static boolean airplanModelAble()
    {
        try
        {
            if (Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON) == 1)
                return true;
            else
                return false;
        }
        catch (SettingNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void toggleFlashLight(Camera mCamera)
    {
        if (mCamera == null)
            return;
        Parameters parameters = mCamera.getParameters();
        if (parameters == null)
            return;
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (flashModes == null)
            return;
        if (!Parameters.FLASH_MODE_OFF.equals(flashMode)
                && (flashModes.contains(Parameters.FLASH_MODE_OFF)))
        {
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.release();
        }
        else if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)
                && (flashModes.contains(Parameters.FLASH_MODE_TORCH)))
        {
            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        }

    }

    public static boolean gprsIsAbled(String methodName)
    {
        Class cmClass = mConnectivityManager.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;

        Boolean isOpen = false;
        try
        {
            Method method = cmClass.getMethod(methodName, argClasses);
            isOpen = (Boolean) method.invoke(mConnectivityManager, argObject);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return isOpen;
    }

    private void toggleGPRS(String methodName, boolean isEnable)
    {
        Class cmClass = mConnectivityManager.getClass();
        Class[] argClasses = new Class[1];
        argClasses[0] = boolean.class;
        try
        {
            Method method = cmClass.getMethod(methodName, argClasses);
            method.invoke(mConnectivityManager, !isEnable);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setAirplaneMode(boolean setAirPlane)
    {

        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, setAirPlane ? 1 : 0);

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", setAirPlane);// pad no work

        mContext.sendBroadcast(intent);

    }

    public static boolean brightnessIsAbled()
    {
        try
        {
            currentBrightness = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (SettingNotFoundException e)
        {
            e.printStackTrace();
        }
        int half = 225 / 2;
        if (currentBrightness > half)
        {
            currentBrightness -= half;
            return true;
        }
        else
        {
            currentBrightness += half;
            return false;
        }

    }

    private void toggleBrightness()
    {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, currentBrightness);
        // the main activity has been finished,the brightness setting cann't
        // work by service immediately,so start another activity to update the
        // brightness and finish itself soon.
        Intent it = new Intent(mContext, RefreshBrightnessActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.putExtra("brightness", currentBrightness);
        mContext.startActivity(it);
    }

    // no work
    private void toggleGPS()
    {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            log.e("inti able");
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        mContext.sendBroadcast(gpsIntent);
        try
        {
            PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
        }
        catch (CanceledException e)
        {
            e.printStackTrace();
        }
    }

    // work!
    public void turnGPSOn()
    {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        mContext.sendBroadcast(intent);

        String provider = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps"))
        { // if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            mContext.sendBroadcast(poke);

        }
    }

    public void turnGPSOff()
    {
        String provider = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider.contains("gps"))
        { // if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            mContext.sendBroadcast(poke);

            // doesn't work either!
            // Intent intent = new
            // Intent("android.location.GPS_ENABLED_CHANGE");
            // intent.putExtra("enabled", false);
            // mContext.sendBroadcast(intent);
        }
    }

    public void openGPSSetting()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            mContext.startActivity(intent);

        }
        catch (ActivityNotFoundException ex)
        {
            log.e("not found activity!");
            // The Android SDK doc says that the location settings activity
            // may not be found. In that case show the general settings.
            // General settings activity
            intent.setAction(Settings.ACTION_SETTINGS);
            try
            {
                mContext.startActivity(intent);
            }
            catch (Exception e)
            {
            }
        }
    }

    // no work
    public void toggleGps(boolean b)
    {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.setLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER, b);
    }

    public static boolean gpsIsAbled()
    {
        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}