package com.tochange.yang.sector.background;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.tochange.yang.log;
import com.tochange.yang.sector.tools.AppUtils;

public class AppData
{
    public String appName;

    public String packageName;

    public Drawable appIcon;

    public boolean choosed;

    public static ArrayList<AppData> getAppList(Context c,
            ArrayList<AppData> appList)
    {
        PackageManager pm = c.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        int size = packages.size();
        for (int i = 0; i < size; i++)
        {
            PackageInfo packageInfo = packages.get(i);
            // system app
            // if ((packageInfo.applicationInfo.flags &
            // ApplicationInfo.FLAG_SYSTEM) == 0)
            // cann't launcher this app
            String name = packageInfo.packageName;
            if (pm.getLaunchIntentForPackage(name) == null)
                continue;
            AppData tmpInfo = new AppData();
            ApplicationInfo appinfo = packageInfo.applicationInfo;
            tmpInfo.packageName = name;
            tmpInfo.appName = appinfo.loadLabel(pm).toString();
            tmpInfo.appIcon = appinfo.loadIcon(pm);
            appList.add(tmpInfo);
        }
        return appList;
    }

    public static ArrayList<AppData> getAppList(final Handler handler,
            final Context c, ArrayList<AppData> appList)
    {

        class LoadAppThread extends Thread
        {
            PackageManager pm = c.getPackageManager();

            List<PackageInfo> packages = pm.getInstalledPackages(0);

            int size = packages.size();

            int i = 0;

            @Override
            public void run()
            {

                // log.e("size=" + size);
                ArrayList<String> packageList = getPackageNameList(c);
                while (i < size)
                {
                    PackageInfo packageInfo = packages.get(i);
                    // system app
                    // if ((packageInfo.applicationInfo.flags &
                    // ApplicationInfo.FLAG_SYSTEM) > 0)
                    {
                        // cann't launcher this app
                        String name = packageInfo.packageName;
                        AppData tmpInfo = null;
                        if (pm.getLaunchIntentForPackage(name) != null)
                        {

                            long t1 = System.currentTimeMillis();
                            tmpInfo = new AppData();
                            if (packageList != null)
                                tmpInfo.choosed = packageList.contains(name);
                            ApplicationInfo appinfo = packageInfo.applicationInfo;
                            tmpInfo.packageName = name;
                            tmpInfo.appName = appinfo.loadLabel(pm).toString();
                            tmpInfo.appIcon = appinfo.loadIcon(pm);

                            // log.e("tmpInfo.appName" + tmpInfo.appName);
                            Message m = new Message();
                            m.obj = tmpInfo;
                            handler.sendMessage(m);
                        }
                        i++;
//                        try
//                        {
//                            Thread.sleep(500);
//                        }
//                        catch (InterruptedException e)
//                        {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
                    }
                }
            }
        }
        new LoadAppThread().start();
        return appList;
    }

    public static ArrayList<String> getPackageNameList(Context c)
    {
        ArrayList<String> packageList = new ArrayList<String>();
        SharedPreferences sp = c.getSharedPreferences(
                AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        int size = sp.getInt(AppUtils.KEY_SIZE, -1);
        if (size > 0)
        {
            for (int j = 0; j < size; j++)
            {
                String packageName = sp.getString(AppUtils.KEY_PACKAGENAME + j,
                        "default packgename");
                packageList.add(packageName);
            }
        }
        return packageList;
    }
}
