package com.tochange.yang.sector.screenobserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.tochange.yang.R;
import com.tochange.yang.lib.Utils;
import com.tochange.yang.lib.log;
import com.tochange.yang.sector.service.BaseFloatWindowService;
import com.tochange.yang.sector.service.FloatWindowService;
import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.numberlocation.GetLocationByNumber;

public class StaticBroadcastReceiver extends BroadcastReceiver
{
    private Context mContext;

    private static boolean mIsNewCall = true;

    private PhoneStateListener mIncomePhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            super.onCallStateChanged(state, incomingNumber);
            switch (state)
            {
                case TelephonyManager.CALL_STATE_IDLE:
                    mIsNewCall = true;
                    restartService();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (mIsNewCall)
                    {
                        mIsNewCall = false;
                        showLocation(incomingNumber);
                    }
                    break;
                default:
                    log.d("unknow phone status");
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        mContext = context;
        // screen unlock
        if (Intent.ACTION_USER_PRESENT.equals(action))
            restartService();
        // application uninstall
        else if (Intent.ACTION_PACKAGE_REMOVED.equals(action))
        {
            BaseFloatWindowService service = BaseFloatWindowService.instance;
            if (service != null)
            {
                String packagename = intent.getData().getSchemeSpecificPart();
                int size = service.mIntent.getIntExtra(AppUtils.KEY_SIZE, -1);
                int i = 0;
                for (; i < size; i++)
                {
                    if (service.mIntent.getStringExtra(
                            AppUtils.KEY_PACKAGENAME + i).equals(packagename))
                        break;

                }
                if ((i < size)
                        && (storeImageAndPackageName(service, size, packagename)))
                    restartService(service);
            }
        }
        // outgoing phone call
        else if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL))
        {
            String outgoingNum = intent
                    .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            showLocation(outgoingNum);
        }
        // dangerous way,but i haven't found incoming action api
        else if (action.equals("android.intent.action.PHONE_STATE"))
        {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mIncomePhoneStateListener,
                    PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private boolean getIsReOpen(BaseFloatWindowService service)
    {
        SharedPreferences sp = service.getSharedPreferences(
                AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(AppUtils.PREFERENCES_ISREOPEN, true);
    }

    private void restartService()
    {
        BaseFloatWindowService service = BaseFloatWindowService.instance;
        if (service != null && service.mAlreadyDestory && !getIsReOpen(service))
            restartService(service);
    }

    private void restartService(BaseFloatWindowService a)
    {
        Intent i = new Intent(a, FloatWindowService.class);
        i.putExtra(AppUtils.KEY_ISREOPEN, true);
        a.stopService(i);
        a.startService(i);
    }

    private boolean storeImageAndPackageName(BaseFloatWindowService a,
            int size, String packagename)
    {
        SharedPreferences sp = a.getSharedPreferences(
                AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        int count = 0;
        Editor editor = sp.edit();
        for (int j = 0; j < size; j++)
        {
            String pn = a.mIntent.getStringExtra(AppUtils.KEY_PACKAGENAME + j);
            if (pn.equals(packagename))
                continue;
            editor.putString(AppUtils.KEY_PACKAGENAME + count, pn);
            String imageString = a.mIntent
                    .getStringExtra(AppUtils.KEY_IMAGESTRING + j);
            if (imageString == null || imageString.equals(""))
                imageString = sp.getString(AppUtils.KEY_IMAGESTRING + j,
                        "default imagestring1");
            editor.putString(AppUtils.KEY_IMAGESTRING + count, imageString);
            count++;
        }
        editor.putInt(AppUtils.KEY_SIZE, count);
        return editor.commit();
    }

    private void showLocation(String num)
    {
        if (Utils.copyAssetsToFiles(mContext,
                AppUtils.PHONELOCATION_FILENAME))
        {
            String location = GetLocationByNumber.getCallerInfo(num, mContext);
            location = appendOperator(num, location);
            Toast.makeText(mContext, location, Toast.LENGTH_LONG).show();
        }
    }

    private String appendOperator(String num, String location)
    {
        String GSM_TDSCDMA[];
        String WCDMA[];
        String CDMA2000[];
        String head = "^(";
        String tail = ").*$";
        String[] config = mContext.getResources().getStringArray(
                R.array.phone_location);
        num = num.replace("+86", "");
        // the order in config file:GSM_TDSCDMA,then WCDMA and CDMA2000 last
        if ((WCDMA = config[1].split(";")) != null
                && num.matches(head + WCDMA[1] + tail))
            location += WCDMA[0];
        else if ((CDMA2000 = config[2].split(";")) != null
                && num.matches(head + CDMA2000[1] + tail))
            location += CDMA2000[0];
        else if ((GSM_TDSCDMA = config[0].split(";")) != null
                && num.matches(head + GSM_TDSCDMA[1] + tail))
            location += GSM_TDSCDMA[0];
        return location;
    }
}