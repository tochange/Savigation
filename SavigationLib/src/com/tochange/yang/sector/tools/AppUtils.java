package com.tochange.yang.sector.tools;

import java.util.ArrayList;

import com.tochange.yang.R;

public class AppUtils {
    public static final String PHONELOCATION_FILENAME               = "AreaData.dat";  
	public static final String SCREENSHOT_PATH                      = "/mnt/sdcard/sector";
	public static final String SCREENSHOT_PICPREFIX                 = "sector";
	public static final String KEY_PACKAGENAME                      = "packagename";
	public static final String KEY_IMAGESTRING                      = "imagestring";
	public static final String KEY_SCREEN_W                         = "screenwidth";
	public static final String KEY_SCREEN_H                         = "screenhigh";
	public static final String KEY_SIZE                             = "childsize";
	public static final String KEY_ISREOPEN                         = "isreopen";
	public static final String KEY_BACKPANEL_VALUES                 = "values";
	
	public static final String PREFERENCES_ISREOPEN                 = "preferenceisreopen";
	public static final String PREFERENCES_FILENAME                 = "position&iconmessage";
	public static final String PREFERENCESNAME_POSITION_X           = "xposition";
	public static final String PREFERENCESNAME_POSITION_Y           = "yposition";
	
	public static final int SECONDPANELKEY_WIFI                     = 1 << 0;
	public static final int SECONDPANELKEY_BLUETOOTH                = 1 << 1;
	public static final int SECONDPANELKEY_GPS                      = 1 << 2;
	public static final int SECONDPANELKEY_BRIGHTNESS               = 1 << 3;
	public static final int SECONDPANELKEY_RING                     = 1 << 4;
	public static final int SECONDPANELKEY_AIRPLANMODE              = 1 << 5;
	public static final int SECONDPANELKEY_GPRS                     = 1 << 6;
	public static final int SECONDPANELKEY_FLASHLIGHT               = 1 << 7;
	public static final int SECONDPANELKEY_SCREENORIENTATION        = 1 << 8;
	public static final int SECONDPANELKEY_SCREENSHOT               = 1 << 9;
	
	public static enum ENUM_CHILDORDER{app, back};

	public static ArrayList<BackItemInfo> getBackPanelDataList(ArrayList<BackItemInfo> backList){
	       if (backList == null)
	           backList = new ArrayList<BackItemInfo>();
	       backList.clear();
	       backList.add(new BackItemInfo("截屏", AppUtils.SECONDPANELKEY_SCREENSHOT, true, R.drawable.screenshot,R.drawable.screenshot));
	       backList.add(new BackItemInfo("GPRS流量", AppUtils.SECONDPANELKEY_GPRS, true, R.drawable.gprs,R.drawable.gprs_off));
	       backList.add(new BackItemInfo("GPR", AppUtils.SECONDPANELKEY_GPS, false, R.drawable.gps,R.drawable.gps));
	       backList.add(new BackItemInfo("WiFi", AppUtils.SECONDPANELKEY_WIFI, true, R.drawable.wifi,R.drawable.wifi_off));
	       backList.add(new BackItemInfo("自动旋转屏幕", AppUtils.SECONDPANELKEY_SCREENORIENTATION, true, R.drawable.rotation,R.drawable.rotation_off));
	       backList.add(new BackItemInfo("静音-振动", AppUtils.SECONDPANELKEY_RING, false, R.drawable.ring,R.drawable.ring_off));
	       backList.add(new BackItemInfo("飞行模式", AppUtils.SECONDPANELKEY_AIRPLANMODE, false, R.drawable.airplane,R.drawable.airplane_off));
	       backList.add(new BackItemInfo("蓝牙", AppUtils.SECONDPANELKEY_BLUETOOTH, true, R.drawable.bluetooth,R.drawable.bluetooth_off));
	       backList.add(new BackItemInfo("屏幕亮度", AppUtils.SECONDPANELKEY_BRIGHTNESS, true, R.drawable.brightness,R.drawable.brightness_off));
	       backList.add(new BackItemInfo("闪光灯", AppUtils.SECONDPANELKEY_FLASHLIGHT, false,R.drawable.flashlisht,R.drawable.flashlisht));
	     return backList;
	   }
}