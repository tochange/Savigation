package com.tochange.yang.lib;

import static android.view.Gravity.BOTTOM;
import static com.tochange.yang.lib.toast.AppMsg.LENGTH_SHORT;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.devspark.appmsg.R;
import com.tochange.yang.lib.FZProgressBar.Mode;
import com.tochange.yang.lib.toast.AppMsg;

public class Utils
{
    static Context mContext;

    public static void setContext(Context c)
    {
        mContext = c;
    }

    static class FileInfos
    {
        Calendar c;

        File file;

        public FileInfos(Calendar c, File file)
        {
            this.c = c;
            this.file = file;
        }
    }

    static class TimeComparator implements Comparator<FileInfos>
    {
        public int compare(FileInfos o1, FileInfos o2)
        {
            return (o1.c.compareTo(o2.c));
        }
    }

    public static List<FileInfos> getDeletedFiles(String path, String appName)
    {
        ArrayList<FileInfos> ret = new ArrayList<FileInfos>();
        File f = new File(path);
        if (!f.isDirectory())
            log.e(path + " not a directory!");
        else
        {
            File[] fileList = f.listFiles();
            getFileInfo(fileList, ret, appName);
        }

        Collections.sort(ret, new TimeComparator());

//        for (FileInfos ff : ret)
//            log.e(ff.file.getAbsolutePath());
        if (ret.size() >= 10)
            return ret.subList(0, ret.size() - 10);
        else
            return null;
    }

    public static void getFileInfo(File[] fileList, ArrayList<FileInfos> list,
            String appName)
    {
        for (int i = 0; i < fileList.length; i++)
        {
            File tmp = fileList[i];
            String path = tmp.getAbsolutePath();
            if (tmp.isFile() && path.contains(appName)
                    && !path.endsWith(appName))
            {
                path = path.replace("_", "-").replace(".", "-");
                String[] array = path.split("-");
                Calendar c = Calendar.getInstance();
                c.set(Integer.parseInt(array[1]), Integer.parseInt(array[2]),
                        Integer.parseInt(array[3]), Integer.parseInt(array[4]),
                        Integer.parseInt(array[5]), Integer.parseInt(array[6]));
                list.add(new FileInfos(c, tmp));
            }
            else if (tmp.isDirectory())
            {
                getFileInfo(tmp.listFiles(), list, appName);
            }
        }
    }

    static void getFileStat(String path)
    {
        try
        {
            Process p = Runtime.getRuntime().exec("stat " + path);
            p.waitFor();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = bf.readLine();
            while (line != null)
            {
                line = bf.readLine();
                log.e("line=" + line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleep(int millisecond)
    {
        try
        {
            Thread.sleep(millisecond);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static Intent getViewIntent(File file)
    {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String extension = android.webkit.MimeTypeMap
                .getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String minType = android.webkit.MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, minType);
        return intent;
    }

    public static Intent getShareIntent(File file)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String extension = android.webkit.MimeTypeMap
                .getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String minType = android.webkit.MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension);
        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType(minType);
        return intent;
    }

    public static ComponentName getTopActivity(Activity context)
    {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity;
        else
            return null;
    }

    public static void Toast(Context c, String msg)
    {
        if (!(c instanceof Activity))
        {
            // AppMsg only accept activity context
            Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        AppMsg appMsg = AppMsg.makeText((Activity) c, msg, R.color.button_col,
                new AppMsg.Style(LENGTH_SHORT, R.color.toast_col));
        appMsg.setLayoutGravity(BOTTOM);

        appMsg.setAnimation(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        appMsg.show();
    }

    public static FZProgressBar setProgressBar(FZProgressBar b, int color)
    {
        b.animation_config(1, 20);
        int[] colors1 = { color, Color.TRANSPARENT };
        b.bar_config(10, 0, 10, Color.TRANSPARENT, colors1);
        return b;
    }

    public static FZProgressBar setProgressBar(FZProgressBar b, int colorStart,
            int colorEnd)
    {
        b.animation_config(1, 20);
        int[] colors1 = { colorStart, colorEnd };
        b.bar_config(10, 0, 10, Color.TRANSPARENT, colors1);
        return b;
    }

    public static void showFZProgressBar(FZProgressBar fZProgressBar)
    {
        if (fZProgressBar.getVisibility() == View.GONE)
        {
            fZProgressBar.animation_start(Mode.INDETERMINATE);
            fZProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public static void closeFZProgressBar(FZProgressBar fZProgressBar)
    {
        if (!(fZProgressBar.getVisibility() == View.GONE))
        {
            fZProgressBar.setVisibility(View.GONE);
            fZProgressBar.animation_stop();
        }

    }

    public static String getCurTimeToString(int i, int n)
    {
        Calendar c = Calendar.getInstance();
        if (c == null)
            return null;
        String time;
        String s1 = "-", s2 = ":", s3 = " ";
        c.add(Calendar.DATE, n);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSecond = c.get(Calendar.SECOND);

        time = "" + mYear;
        if (1 == i)
        {
            time += s1;
        }
        int mon = mMonth + 1;
        if (mon < 10)
        {
            time = time + 0 + mon;
        }
        else
        {
            time += mon;
        }
        if (1 == i)
        {
            time += s1;
        }
        if (mDay < 10)
        {
            time = time + 0 + mDay;
        }
        else
        {
            time += mDay;
        }
        if (1 == i)
        {
            time += s3;
        }
        if (mHour < 10)
        {
            time = time + 0 + mHour;
        }
        else
        {
            time += mHour;
        }
        if (1 == i)
        {
            time += s2;
        }
        if (mMinute < 10)
        {
            time = time + 0 + mMinute;
        }
        else
        {
            time += mMinute;
        }
        if (1 == i)
        {
            time += s2;
        }
        if (mSecond < 10)
        {
            time = time + 0 + mSecond;
        }
        else
        {
            time += mSecond;
        }

        time = time.replace(" ", "_").replace(":", ".");
        time = "_" + time;
        return time;
    }

    public static boolean serviceIsRunning(Context mContext, String className)
    {

        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);
        if ((serviceList.size() <= 0))
            return false;
        for (int i = 0; i < serviceList.size(); i++)
            if (serviceList.get(i).service.getClassName().equals(className) == true)
                return true;
        return false;
    }

    public static String getUsedPercentValue(Context context)
    {
        String dir = "/proc/meminfo";
        try
        {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine
                    .indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll(
                    "\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize)
                    / (float) totalMemorySize * 100);
            return percent + "%";
        }
        catch (IOException e)
        {
            log.e("getUsedPercentValue error");
            e.printStackTrace();
        }
        return "error";
    }

    private static long getAvailableMemory(Context context)
    {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }

    static ActivityManager mActivityManager;

    private static ActivityManager getActivityManager(Context context)
    {
        if (mActivityManager == null)
        {
            mActivityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    public static int getThisProcessMemeryInfoInKbit(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        int pid = android.os.Process.myPid();
        android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager
                .getProcessMemoryInfo(new int[] { pid });
        return memoryInfoArray[0].getTotalPrivateDirty();
    }

    public static void uninstallApp(Context cc, String packageName)
    {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        cc.startActivity(uninstallIntent);
        // setIntentAndFinish(true, true);
    }

    public static void forceStopApp(Context cc, String packageName)
    {
        ActivityManager am = (ActivityManager) cc
                .getSystemService(Context.ACTIVITY_SERVICE);
        // am.forceStopPackage(packageName);

        // Class c =
        // Class.forName("com.android.settings.applications.ApplicationsState");
        // Method m = c.getDeclaredMethod("getInstance", Application.class);

    }

    public static int getStatusBarHeight(Context cc)
    {
        int ret = -1;
        try
        {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            ret = cc.getResources().getDimensionPixelSize(x);
        }
        catch (Exception e)
        {
            log.e("getStatusBarHeight error");
            e.printStackTrace();
        }
        return ret;
    }

    public static void openApp(Context c, String packageName)
    {
        Intent i = c.getPackageManager().getLaunchIntentForPackage(packageName);
        if (i != null)
            c.startActivity(i);
        else
            Toast.makeText(c, "cann't launcher this app", Toast.LENGTH_SHORT)
                    .show();

    }

    public static void openAppTroublesome(Context c, String packageName)

    {
        PackageInfo pi;
        try
        {
            pi = c.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);
            List<ResolveInfo> apps = c.getPackageManager()
                    .queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null)
            {
                String packageName1 = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                ComponentName cn = new ComponentName(packageName1, className);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(cn);
                c.startActivity(intent);
            }
        }
        catch (NameNotFoundException e)
        {
            log.e("open app error,maybe cann't find the launcher activity");
            e.printStackTrace();
        }

    }

    public static double[] getPhysicInch(Context context)
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        double result[] = new double[3];
        result[0] = Math.pow(dm.widthPixels / dm.xdpi, 2);
        result[1] = Math.pow(dm.heightPixels / dm.ydpi, 2);
        result[2] = Math.sqrt(result[0] + result[1]);
        return result;
    }

    public static synchronized String drawableToByte(Drawable drawable)
    {

        if (drawable != null)
        {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;

            // 鍒涘缓涓�釜瀛楄妭鏁扮粍杈撳嚭娴�娴佺殑澶у皬涓簊ize
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            // 璁剧疆浣嶅浘鐨勫帇缂╂牸寮忥紝璐ㄩ噺涓�00%锛屽苟鏀惧叆瀛楄妭鏁扮粍杈撳嚭娴佷腑
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            // 灏嗗瓧鑺傛暟缁勮緭鍑烘祦杞寲涓哄瓧鑺傛暟缁刡yte[]
            byte[] imagedata = baos.toByteArray();

            String icon = Base64.encodeToString(imagedata, Base64.DEFAULT);
            return icon;
        }
        return null;
    }

    public static synchronized Drawable byteToDrawable(String icon)
    {
        if (icon == null || icon.equals(""))
        {
            log.e("image string null");
            return null;
        }
        byte[] img = Base64.decode(icon.getBytes(), Base64.DEFAULT);
        Bitmap bitmap;
        if (img != null)
        {

            bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            @SuppressWarnings("deprecation")
            Drawable drawable = new BitmapDrawable(bitmap);

            return drawable;
        }
        return null;

    }

    // 浠庤祫婧愪腑鑾峰彇Bitmap
    public static Bitmap getBitmapFromResources(Activity act, int resId)
    {
        Resources res = act.getResources();
        return BitmapFactory.decodeResource(res, resId);
    }

    // byte[] 鈫�Bitmap
    public static Bitmap convertBytes2Bimap(byte[] b)
    {
        if (b.length == 0)
        {
            return null;
        }
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    // Bitmap 鈫�byte[]
    public static byte[] convertBitmap2Bytes(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap string2Bitmap(String s)
    {
        Bitmap b;
        byte[] array = Base64.decode(s, Base64.DEFAULT);
        b = BitmapFactory.decodeByteArray(array, 0, array.length);
        return b;
    }

    public static Bitmap convertDrawable2BitmapSimple(Drawable drawable)
    {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    // Bitmap 鈫�Drawable
    public static Drawable convertBitmap2Drawable(Bitmap bitmap)
    {
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        // 鍥犱负BtimapDrawable鏄疍rawable鐨勫瓙绫伙紝鏈�粓鐩存帴浣跨敤bd瀵硅薄鍗冲彲銆�
        return bd;
    }

    public static Bitmap drawabletoBitmap(Drawable drawable)
    {

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicWidth();

        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);

        drawable.draw(canvas);

        return bitmap;
    }

    /*************************** 鍥剧墖鍦嗚澶勭悊 ********************************/
    public static Bitmap getRCB(Bitmap bitmap, float roundPX)
    {
        Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(dstbmp);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return dstbmp;
    }

    public static Bitmap getOval(Bitmap bitmap)
    {
        Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(dstbmp);
        final int color = 0xff00ff00;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 255, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return dstbmp;
    }

    public static Bitmap getTransparentOval(Bitmap bitmap)
    {
        Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(dstbmp);
        final int color = 0x9900ff00;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 255, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return dstbmp;
    }
    // class Monitor implements Runnable
    // {
    // List<Item> backchild;
    //
    // private volatile boolean go = false;
    //
    // public Monitor(List<Item> backchild)
    // {
    // this.backchild = backchild;
    // }
    //
    // public synchronized void gotMessage() throws InterruptedException
    // {
    // go = true;
    // notify();
    // }
    //
    // public synchronized void watching() throws InterruptedException
    // {
    // while (go == false)
    // wait();
    // // beginUpdate(backchild);
    // }
    //
    // public void run()
    // {
    // try
    // {
    // watching();
    // }
    // catch (InterruptedException e)
    // {
    // e.printStackTrace();
    // }
    // }
    // }
    // class RefreshTask extends TimerTask {
    // @Override
    // public void run() {
    // handler.post(new Runnable() {
    // @Override
    // public void run() {
    // Toast.makeText(getContext(),
    // Utils.getUsedPercentValue(mContext),
    // Toast.LENGTH_SHORT).show();
    // }
    // });
    // }
    // }
}