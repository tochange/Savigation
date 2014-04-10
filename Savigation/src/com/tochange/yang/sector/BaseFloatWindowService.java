package com.tochange.yang.sector;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

import com.tochange.yang.R;
import com.tochange.yang.Utils;
import com.tochange.yang.log;
import com.tochange.yang.sector.background.ShakeInterface;
import com.tochange.yang.sector.background.ShakeListener;
import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.BackItemInfo;
import com.tochange.yang.sector.tools.BackPanelBin;
import com.tochange.yang.view.Item;
import com.tochange.yang.view.SectorButton;

public abstract class BaseFloatWindowService extends Service implements
        FloatWindowServiceInterface
{
    // sony st18i,and almost top left
    protected int DEFAULT_DISPLAY_HIGHT = (int) (854 / 4.0);

    protected int DEFAULT_DISPLAY_WIDTH = (int) (480 / 6.0);

    protected int mScreanW;

    protected int mScreanH;

    protected boolean mStickyHasReset; 

    protected boolean mCanNew = true;
/**
 *sticky to the border of your screen
 */
    protected boolean mCanReStartShake;

    protected boolean mCanMove = true;

    protected boolean mIsSticky;
    protected boolean mIsMoving;
    protected boolean mAlreadyDestory;

    
    protected long mSleepTime = 10;
    //if stick too slowly,moving when being alpha will awkward,so be quick
    protected final int STICKY_OFFSET = 50;

    protected int mStatusBarHeight;

    protected LayoutParams mLayoutParams;

    protected RelativeLayout mFloatLayout;

    protected WindowManager mWindowManager;

    protected GestureDetector mGestureDetector;

    protected Item mFatherItem;

    protected Vibrator mVibrator;

    protected Intent mIntent;

    protected List<Item> mChoosedBackClildItemList;

    protected ArrayList<BackItemInfo> mChoosedBackClildList;

    protected SectorButton mSectorButton;

    protected int mEvilMarginTop;

    protected SharedPreferences mSharedPreferences;

    // why must be static?
    protected static final int SEND_NOTIFICATION = 47;

    protected static final int CLEAR_NOTIFICATION = 48;

    protected NotificationManager mNotificationManager;

    protected ShakeListener mShakeListener;

    protected BackPanelBin mBackPanelBin;


    @Override
    public void onCreate()
    {
        super.onCreate();
        log.e("");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mVibrator = (Vibrator) getApplication().getSystemService(
                Service.VIBRATOR_SERVICE);
        mGestureDetector = getGestureDetector();
        mLayoutParams = new LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(
                getApplication().WINDOW_SERVICE);
        mSharedPreferences = getSharedPreferences(
                AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        initEnvironment();
        mBackPanelBin = new BackPanelBin(this);
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;// forever
        mIntent = intent;
        // log.e(" " + (mIntent.getBooleanExtra(AppUtils.KEY_ISREOPEN, false)));
        // log.e("start--" + mIntent.getIntExtra(AppUtils.KEY_SIZE, -999));

        setLayoutParamsWidthAndHight(mLayoutParams);
        mLayoutParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;// transparent
        mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mLayoutParams.width = LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = LayoutParams.WRAP_CONTENT;// -2
        if (mCanNew)
        {
            mCanNew = false;
            createFloatView();
            mCanReStartShake = true;
            mAlreadyDestory = false;
            mShakeListener = new ShakeListener(this);
            mShakeListener.setOnShakeListener(new ShakeInterface() {
                public void onShake() throws InterruptedException
                {
                    if (mCanMove)
                    {
                        // log.e("shake........");
                        mSectorButton.setSticky(!mIsSticky);
                        stickBorder();
                        mIsSticky = !mIsSticky;
                        mShakeListener.stop();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run()
                            {
                                if (mCanReStartShake)
                                    mShakeListener.start();
                            }
                        }, 2000);
                    }
                }
            });
        }
    }

    private void createFloatView()
    {
        mFloatLayout = (RelativeLayout) LayoutInflater.from(getApplication())
                .inflate(R.layout.sectorbutton_view, null);
        mWindowManager.addView(mFloatLayout, mLayoutParams);
        mSectorButton = (SectorButton) mFloatLayout.findViewById(R.id.pm);
        mFatherItem = mSectorButton.getFatherItem();
        mSectorButton.initData(initChildrenItemList());
        mEvilMarginTop = mSectorButton.getEvilMarginTop();
        // mLayoutParams.dimAmount = 0.6f;
        mSectorButton.setLinster(getChildrenLinster());
        mFatherItem.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mGestureDetector.onTouchEvent(event);
                // no enough event in customs gesture detector
                if (mIsMoving && event.getAction() == MotionEvent.ACTION_UP)
                {   mIsMoving = false;
                    saveCurrentPosition();

                    if (mIsSticky && !mStickyHasReset)
                    {
                        mStickyHasReset = true;
                            // sleep more time to make sure last
                            // StickyUpdateTask has died
                            sleep();
                            sleep();
                            mStickyHasReset = false;   
                            stickBorder();
                    }
                }
                return true;
            }
        });
    }

    private List<List<Item>> initChildrenItemList()
    {
        List<List<Item>> ret = new ArrayList<List<Item>>();
        List<Item> appItemList = new ArrayList<Item>();
//        Editor editor = mSharedPreferences.edit();
        log.e("rrrrrrrrrrr=" + (mIntent != null));
        // log.e("rrrrrrrrrrr="
        // + (mIntent.getIntExtra(AppUtils.KEY_SIZE, -5555555)));
        int value = -1;
        int size = -1;
        if (mIntent != null
                && !mIntent.getBooleanExtra(AppUtils.KEY_ISREOPEN, false))
        {
            size  = mIntent.getIntExtra(AppUtils.KEY_SIZE, -1);
            value = mIntent.getIntExtra(AppUtils.KEY_BACKPANEL_VALUES, -1);
            for (int i = 0; i < size; i++)
            {
                String imageString = mIntent
                        .getStringExtra(AppUtils.KEY_IMAGESTRING + i);
                String packagename = mIntent
                        .getStringExtra(AppUtils.KEY_PACKAGENAME + i);
                addImageStringToChildList(imageString, appItemList);
//                editor.putString(AppUtils.KEY_IMAGESTRING + i, imageString);
//                editor.putString(AppUtils.KEY_PACKAGENAME + i, packagename);
            }
            log.e("store.........value=" + value);
//            editor.putInt(AppUtils.KEY_BACKPANEL_VALUES, value);
//            editor.putInt(AppUtils.KEY_SIZE, size);
//            editor.commit();
        }
        else
        {// when service auto start intent will be null,get last time
         // parameters
            size = mSharedPreferences.getInt(AppUtils.KEY_SIZE, -1);
            value = mSharedPreferences
                    .getInt(AppUtils.KEY_BACKPANEL_VALUES, -1);

            log.e("get.........value=" + value);
            // add 30140224
            if (mIntent == null)
                mIntent = new Intent(this, FloatWindowService.class);
            mIntent.putExtra(AppUtils.KEY_SIZE, size);
            mIntent.putExtra(AppUtils.KEY_BACKPANEL_VALUES, value);
            for (int i = 0; i < size; i++)
            {
                String imageString = mSharedPreferences.getString(
                        AppUtils.KEY_IMAGESTRING + i, "default imagestring");
//                log.e(i + " 图片串：" +imageString);
                addImageStringToChildList(imageString, appItemList);

                // add 30140224
                String packageName = mSharedPreferences.getString(
                        AppUtils.KEY_PACKAGENAME + i, "default packgename");
                mIntent.putExtra(AppUtils.KEY_PACKAGENAME + i, packageName);
            }
        }
        mChoosedBackClildItemList = getBackChildListByValue(value);
        ret.add(appItemList);//pay attention to the order
        ret.add(mChoosedBackClildItemList);

        return ret;
    }

    private List<Item> getBackChildListByValue(int value)
    {
        if (mChoosedBackClildList == null)
            mChoosedBackClildList = new ArrayList<BackItemInfo>();
        mChoosedBackClildList.clear();
        List<Item> resultList = new ArrayList<Item>();
        ArrayList<BackItemInfo> list = AppUtils.getBackPanelDataList(null);
        int size = list.size();
        for (int i = 0; i < size; i++)
        {
                BackItemInfo tmp = list.get(i);
                if ((tmp.value & value) == tmp.value)
                {
                    int res;
                    mChoosedBackClildList.add(tmp);
                    Item child = new Item(this, null);
                    res = BackPanelBin.isAble(tmp.value) ?tmp.iconResOn:tmp.iconResOff;
                    child.setBackgroundDrawable(getResources().getDrawable(res));
                    resultList.add(child);
                }
        }
        return resultList;
    }

    private void addIntentFilterAction(IntentFilter filter, int value)
    {
        switch (value)
        {
            case AppUtils.SECONDPANELKEY_WIFI:
                filter.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);
                break;
            case AppUtils.SECONDPANELKEY_BLUETOOTH:
                filter.addAction(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
                break;
            case AppUtils.SECONDPANELKEY_GPS:
                break;
            case AppUtils.SECONDPANELKEY_BRIGHTNESS:
                break;
            case AppUtils.SECONDPANELKEY_RING:
                filter.addAction(android.media.AudioManager.RINGER_MODE_CHANGED_ACTION);
                break;
            case AppUtils.SECONDPANELKEY_AIRPLANMODE:
                filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                break;
            case AppUtils.SECONDPANELKEY_GPRS:
                filter.addAction(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
            default:
        }

    }

    private void addImageStringToChildList(String imageString,
            List<Item> resultList)
    {
        Item child = new Item(this, null);
        //no round corner
//        child.setImageDrawable(Utils.byteToDrawable(imageString));
        
        child.setBackgroundDrawable(Utils.convertBitmap2Drawable((Utils
                .getOval(Utils
                        .string2Bitmap(imageString)))));
        resultList.add(child);

    }

    protected void sleep() 
    {
        try
        {
            Thread.sleep(mSleepTime);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void saveCurrentPosition()
    {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(AppUtils.PREFERENCESNAME_POSITION_X, mLayoutParams.x);
        editor.putInt(AppUtils.PREFERENCESNAME_POSITION_Y, mLayoutParams.y);
        editor.commit();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
