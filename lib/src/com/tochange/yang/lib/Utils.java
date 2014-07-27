package com.tochange.yang.lib;

import static android.view.Gravity.BOTTOM;
import static com.tochange.yang.lib.toast.AppMsg.LENGTH_SHORT;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    
    public static void showHardwareInfoLog(Context context) {
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics m = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(m);
		float d = m.density;
		int dd = m.densityDpi;
		log.e("density:" + d + ";densityDpi:" + dd + ";screenSize:" + m.widthPixels
				+ "*" + m.heightPixels + ";MANUFACTURER:"
				+ Build.MANUFACTURER + ";BRAND:" + Build.BRAND
				+ ";MODEL:" + Build.MODEL + ";RELEASE:"
				+ Build.VERSION.RELEASE + ";SDK:" + Build.VERSION.SDK);
	}
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }
    public static boolean copyAssetsToFiles(Context context, String filaName)
    {
        if (!(new File(context.getFilesDir() + File.separator + filaName))
                .exists())
        {
            try
            {
                InputStream inputStream = context.getAssets().open(filaName);
                int length = inputStream.available();
                byte[] buffer = new byte[length];
                inputStream.read(buffer);
                inputStream.close();
                FileOutputStream fileOutputStream = context.openFileOutput(
                        filaName, Context.MODE_PRIVATE);
                fileOutputStream.write(buffer);
                fileOutputStream.close();
                return true;
            }
            catch (FileNotFoundException e)
            {
                log.e(e.toString());
                return false;
            }
            catch (IOException e)
            {
                log.e(e.toString());
                return false;
            }

        }
        else
            return true;
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

        // for (FileInfos ff : ret)
        // log.e(ff.file.getAbsolutePath());
        if (ret.size() >= 10)
            return ret.subList(0, ret.size() - 10);
        else
            return null;
    }

    public static void vibrate(Context c)
    {

        Vibrator  mVibrator = (Vibrator) c.getApplicationContext().getSystemService(
                Service.VIBRATOR_SERVICE);
        mVibrator.vibrate(new long[] { 50, 50, 0, 0 }, -1);
    }

    public static void getFileInfo(File[] fileList, ArrayList<FileInfos> list,
            String appName)
    {
        int length = fileList.length;
        for (int i = 0; i < length; i++)
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

    public static void getFileStat(String path)
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
                log.e("stat:" + line);
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
        int size = serviceList.size();
        for (int i = 0; i < size; i++)
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

    public static boolean bitmapToPNGFile(Bitmap b, String fileName)
    {
        if (b == null)
            return false;
        try
        {
            FileOutputStream out = new FileOutputStream(fileName);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (FileNotFoundException e)
        {
            log.e(e.toString());
        }
        catch (IOException e)
        {
            log.e(e.toString());
        }
        return true;
    }
    
    public static Bitmap getBitmapFromView(View view) {  
        Bitmap bitmap = null;  
        try {  
            int width = view.getWidth();  
            int height = view.getHeight();  
            if(width != 0 && height != 0){  
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
                Canvas canvas = new Canvas(bitmap);  
                view.layout(0, 0, width, height);  
                view.draw(canvas);  
            }  
        } catch (Exception e) {  
            bitmap = null;  
            e.getStackTrace();  
        }  
        return bitmap;  
    }
    
    //add water mark in right|bottom
    public static Bitmap addWatermark(Bitmap src, Bitmap watermark) {  
        if (src == null || watermark  == null) {  
            log.e("src is null");  
            return src;  
        }  
          
        int sWid = src.getWidth();  
        int sHei = src.getHeight();  
        int wWid = watermark.getWidth();  
        int wHei = watermark.getHeight();  
        if (sWid == 0 || sHei == 0) {  
            return null;  
        }  
          
        if (sWid < wWid || sHei < wHei) {  
            return src;  
        }  
          
        Bitmap bitmap = Bitmap.createBitmap(sWid, sHei, Config.ARGB_8888);  
        try {  
            Canvas cv = new Canvas(bitmap);  
            cv.drawBitmap(src, 0, 0, null);  
            cv.drawBitmap(watermark, sWid - wWid - 5, sHei - wHei - 5, null);  
            cv.save(Canvas.ALL_SAVE_FLAG);  
            cv.restore();  
        } catch (Exception e) {  
            bitmap = null;  
            e.getStackTrace();  
        }  
        return bitmap;  
    }  
    
    
    public static Bitmap addDeletemark(Bitmap src, Bitmap watermark) {  
        if (src == null || watermark  == null) {  
            log.e("src is null");  
            return src;  
        }  
          
        int sWid = src.getWidth();  
        int sHei = src.getHeight();  
        int wWid = watermark.getWidth();  
        int wHei = watermark.getHeight();  
        if (sWid == 0 || sHei == 0) {  
            return null;  
        }  
          
        if (sWid < wWid || sHei < wHei) {  
            return src;  
        }  
          
        Bitmap bitmap = Bitmap.createBitmap(sWid + wWid / 2, sHei + wHei / 2, Config.ARGB_8888);  
        try {  
            Canvas cv = new Canvas(bitmap);  
            cv.drawBitmap(src, 0, wHei / 2, null);  
            cv.drawBitmap(watermark, sWid - wWid / 2, 0, null);  
            cv.save(Canvas.ALL_SAVE_FLAG);  
            cv.restore();  
        } catch (Exception e) {  
            bitmap = null;  
            e.getStackTrace();  
        }  
        return bitmap;  
    } 
    
    public static boolean saveBitmap(Bitmap bitmap,String path, String fileName) {  
        File file = new File(path);  
        if (!file.exists()) {  
            file.mkdir();  
        }  
        File imageFile = new File(file, fileName);  
        try {  
            imageFile.createNewFile();  
            FileOutputStream fos = new FileOutputStream(imageFile);  
            bitmap.compress(CompressFormat.JPEG, 50, fos);  
            fos.flush();  
            fos.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return true;  
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

            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
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

    public static Bitmap getBitmapFromResources(Activity activity, int resId)
    {
        return BitmapFactory.decodeResource(activity.getResources(), resId);
    }

    public static Bitmap convertBytes2Bimap(byte[] b)
    {
        if (b.length == 0)
        {
            return null;
        }
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

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

    public static Drawable convertBitmap2Drawable(Bitmap bitmap)
    {
        BitmapDrawable bd = new BitmapDrawable(bitmap);
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

    public static Bitmap getPathBitmap(Bitmap bitmap, Path path)
    {
        Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(dstbmp);
        final int color = 0xff00ff00;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 255, 0, 0);
        paint.setColor(color);
        canvas.drawPath(path, paint);
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
    
    /** 
     * Checks if the device is a tablet or a phone 
     *  
     * @param activityContext 
     *            The Activity Context. 
     * @return Returns true if the device is a Tablet 
     */  
    public static boolean isTabletDevice(Context activityContext) {  
        // Verifies if the Generalized Size of the device is XLARGE to be  
        // considered a Tablet  
        
//        yangxj@20140722
//        boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout &   
//                            Configuration.SCREENLAYOUT_SIZE_MASK) ==   
//                            Configuration.SCREENLAYOUT_SIZE_XLARGE);  
        boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout &   
                Configuration.SCREENLAYOUT_SIZE_MASK) >=   
                Configuration.SCREENLAYOUT_SIZE_LARGE); 
        
        // If XLarge, checks if the Generalized Density is at least MDPI  
        // (160dpi)  
        if (xlarge) { 
            DisplayMetrics metrics = new DisplayMetrics();  
            Activity activity = (Activity) activityContext;  
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);  
      
            // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160,  
            // DENSITY_TV=213, DENSITY_XHIGH=320  
            if (metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT  
                    || metrics.densityDpi == DisplayMetrics.DENSITY_HIGH  
                    || metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM  
                    || metrics.densityDpi == DisplayMetrics.DENSITY_TV  
                    || metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {  
      
                // Yes, this is a tablet!  
                return true;  
            }  
        }  
      
        // No, this is not a tablet!  
        return false;  
    }  

    public void setUpFloatWindow(String message)
    {
        LinearLayout tv = (LinearLayout) LayoutInflater.from(
                mContext.getApplicationContext()).inflate(R.layout.app_msg,
                null);
        TextView t = (TextView) tv.findViewById(android.R.id.message);
        t.setText(message);
        t.setTextColor(Color.RED);
        t.setTextSize(25);
        WindowManager wm = (WindowManager) mContext.getSystemService("window");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.alpha = 1.0f; // lower than one makes it more transparent
        lp.dimAmount = 0f; // set it higher if you want to dim behind the window
        lp.gravity = Gravity.CENTER;
        lp.type = LayoutParams.TYPE_PRIORITY_PHONE;
        lp.format = PixelFormat.RGBA_8888;// transparent
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = LayoutParams.FILL_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;// -2
        wm.addView(tv, lp);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 5, 1, 5,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnimation.setDuration(5000);
        t.startAnimation(scaleAnimation);
    }

    class Monitor implements Runnable
    {
        List<String> backchild;

        private volatile boolean go = false;

        public Monitor(List<String> backchild)
        {
            this.backchild = backchild;
        }

        public synchronized void gotMessage() throws InterruptedException
        {
            go = true;
            notify();
        }

        public synchronized void watching() throws InterruptedException
        {
            while (go == false)
                wait();
            // beginUpdate(backchild);
        }

        public void run()
        {
            try
            {
                watching();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    class RefreshTask extends TimerTask
    {
        Handler handler;

        @Override
        public void run()
        {
            handler.post(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(mContext,
                            Utils.getUsedPercentValue(mContext),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//    timer = new Timer();
//    timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
//    timer.cancel();
    
    class SleepTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... arg0)
        {
            Utils.sleep(Integer.parseInt(arg0[0]));
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
        }
    }
    // new SleepTask().execute(1 + "");
}