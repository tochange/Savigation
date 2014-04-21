package com.tochange.yang.sector.screenobserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tochange.yang.sector.service.BaseFloatWindowService;
import com.tochange.yang.sector.service.FloatWindowService;
import com.tochange.yang.sector.tools.AppUtils;

public class ScreenUnlockReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction()))
        {
        	BaseFloatWindowService a = BaseFloatWindowService.instance;
            if (a != null)
            {
                SharedPreferences sp = a.getSharedPreferences(
                        AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
                boolean isReopen = sp.getBoolean(AppUtils.PREFERENCES_ISREOPEN,
                        true);
                if (!isReopen)
                {
                    Intent i = new Intent(a, FloatWindowService.class);
                    i.putExtra(AppUtils.KEY_ISREOPEN, true);
                    a.stopService(i);
                    a.startService(i);
                }
            }
        }
    }
}