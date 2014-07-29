package com.tochange.yang.sector.service;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

import com.tochange.yang.lib.Utils;
import com.tochange.yang.lib.log;
import com.tochange.yang.lib.ui.Graphics;
import com.tochange.yang.sector.R;
import com.tochange.yang.sector.screenobserver.ScreenObserver;
import com.tochange.yang.sector.shake.ShakeInterface;
import com.tochange.yang.sector.shake.ShakeListener;
import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.BackItemInfo;
import com.tochange.yang.sector.tools.BackPanelBin;
import com.tochange.yang.view.Item;
import com.tochange.yang.view.MainView;

public abstract class BaseFloatWindowService extends Service implements
        FloatWindowServiceInterface
{
    public static boolean mAlreadyDestory;

    public Intent mIntent;

    // sony st18i,and almost top left
    protected int DEFAULT_DISPLAY_HIGHT = (int) (854 / 4.0);

    protected int DEFAULT_DISPLAY_WIDTH = (int) (480 / 6.0);

    protected int mScreanW;

    protected int mScreanH;

    protected boolean mStickyHasReset;

    protected boolean mCanNew = true;

    /**
     * sticky to the border of your screen
     */
    protected boolean mCanReStartShake;

    protected boolean mCanMove = true;

    protected boolean mIsSticky;

    protected boolean mIsMoving;

    protected int mSleepTime = 10;

    // if stick too slowly,moving when being alpha will awkward,so be quick
    protected final int STICKY_OFFSET = 50;

    protected int mStatusBarHeight;

    protected LayoutParams mLayoutParams;

    protected RelativeLayout mFloatLayout;

    protected WindowManager mWindowManager;

    protected GestureDetector mGestureDetector;

    protected Item mFatherItem;

    protected List<Item> mChoosedBackClildItemList;

    protected List<List<Item>> mClildItemList;

    protected ArrayList<BackItemInfo> mChoosedBackClildList;

    protected MainView mMainView;

    protected int mEvilMarginTop;

    protected SharedPreferences mSharedPreferences;

    protected static final int SEND_NOTIFICATION = 47;

    protected static final int CLEAR_NOTIFICATION = 48;

    // i donn't know how to clear notification in main activity,just use static
    // variable
    public static NotificationManager mNotificationManager;

    protected ShakeListener mShakeListener;

    protected BackPanelBin mBackPanelBin;

    protected ScreenObserver mScreenObserver;

    public static BaseFloatWindowService instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mGestureDetector = getGestureDetector();
        mLayoutParams = new LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(
                getApplication().WINDOW_SERVICE);
        mSharedPreferences = getSharedPreferences(
                AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        mBackPanelBin = new BackPanelBin(this);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        initEnvironment();
        mIntent = intent;
        setLayoutParamsWidthAndHight(mLayoutParams);
        setLayoutParameter(mLayoutParams);
        if (mCanNew)
        {
            mCanNew = false;
            createFloatView(mLayoutParams);

            mCanReStartShake = true;
            mAlreadyDestory = false;
            mShakeListener = new ShakeListener(this);
            mShakeListener.setOnShakeListener(new ShakeInterface() {
                public void onShake() throws InterruptedException
                {
                    if (mCanMove)
                    {
                        mMainView.setSticky(!mIsSticky);
                        stickBorder();
                        mIsSticky = !mIsSticky;
                        mShakeListener.stopShakeListen();
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

    private void setLayoutParameter(LayoutParams lp)
    {
        lp.gravity = Gravity.LEFT | Gravity.TOP;// forever
        lp.type = LayoutParams.TYPE_PRIORITY_PHONE;
        lp.format = PixelFormat.RGBA_8888;// transparent
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;// -2

    }

    private void createFloatView(LayoutParams lp)
    {
        mFloatLayout = (RelativeLayout) LayoutInflater.from(getApplication())
                .inflate(R.layout.sectorbutton_view, null);
        mWindowManager.addView(mFloatLayout, lp);
        getAndSetMainView();
        mFatherItem.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mGestureDetector.onTouchEvent(event);
                // no enough event in customs gesture detector
                if (mIsMoving && event.getAction() == MotionEvent.ACTION_UP)
                {
                    mIsMoving = false;
                    if (!saveCurrentPosition())
                        log.e("save position error!");
                    if (mIsSticky && !mStickyHasReset)
                    {
                        mStickyHasReset = true;
                        // sleep more time to make sure last
                        // StickyUpdateTask has died
                        Utils.sleep(mSleepTime);
                        Utils.sleep(mSleepTime);
                        mStickyHasReset = false;
                        stickBorder();
                    }
                }
                return true;
            }
        });                     
    }

    private void getAndSetMainView()
    {
        mMainView = (MainView) mFloatLayout.findViewById(R.id.sector);
        mFatherItem = mMainView.getFatherItem();

        int value = intentNotReOpen() ? mIntent.getIntExtra(
                AppUtils.KEY_BACKPANEL_VALUES, -1) : mSharedPreferences.getInt(
                AppUtils.KEY_BACKPANEL_VALUES, -1);
        mChoosedBackClildItemList = getBackChildListByValue(value);

        mClildItemList = new ArrayList<List<Item>>();
        mClildItemList.add(AppUtils.ENUM_CHILDORDER.app.ordinal(),
                getAppItemList());
        mClildItemList.add(AppUtils.ENUM_CHILDORDER.back.ordinal(),
                mChoosedBackClildItemList);

        mMainView.initData(mClildItemList);
        mEvilMarginTop = mMainView.getEvilMarginTop();
        // mLayoutParams.dimAmount = 0.6f;
        mMainView.setLinster(getChildrenLinster());
    }

    private boolean intentNotReOpen()
    {
        return (mIntent != null && !mIntent.getBooleanExtra(
                AppUtils.KEY_ISREOPEN, false));
    }

    private List<Item> getAppItemList()
    {
        List<Item> appItemList = new ArrayList<Item>();
        int value, size;
        if (intentNotReOpen())
        {
            size = mIntent.getIntExtra(AppUtils.KEY_SIZE, -1);
            for (int i = 0; i < size; i++)
            {
                addImageStringToChildList(
                        mIntent.getStringExtra(AppUtils.KEY_IMAGESTRING + i),
                        appItemList);
            }
        }
        else
        {// when service auto start intent will be null,get last time
         // parameters
            size = mSharedPreferences.getInt(AppUtils.KEY_SIZE, -1);
            value = mSharedPreferences
                    .getInt(AppUtils.KEY_BACKPANEL_VALUES, -1);

            if (mIntent == null)
                mIntent = new Intent(this, FloatWindowService.class);
            mIntent.putExtra(AppUtils.KEY_SIZE, size);
            mIntent.putExtra(AppUtils.KEY_BACKPANEL_VALUES, value);
            for (int i = 0; i < size; i++)
            {
                addImageStringToChildList(
                        mSharedPreferences.getString(AppUtils.KEY_IMAGESTRING
                                + i, "default imagestring"), appItemList);
                mIntent.putExtra(
                        AppUtils.KEY_PACKAGENAME + i,
                        mSharedPreferences.getString(AppUtils.KEY_PACKAGENAME
                                + i, "default packgename"));
            }
        }
        return appItemList;
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
                res = BackPanelBin.isAble(tmp.value) ? tmp.iconResOn
                        : tmp.iconResOff;
                child.setBackgroundDrawable(getResources().getDrawable(res));
                resultList.add(child);
            }
        }
        return resultList;
    }

    @SuppressWarnings("deprecation")
    private void addImageStringToChildList(String imageString,
            List<Item> resultList)
    {
        Item child = new Item(this, null);
        // no round corner
        // child.setImageDrawable(Utils.byteToDrawable(imageString));

        child.setBackgroundDrawable(Graphics.convertBitmap2Drawable((Graphics
                .getOval(Graphics.string2Bitmap(imageString)))));
        resultList.add(child);

    }

    protected boolean saveIsReopen(boolean isReopen)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(AppUtils.PREFERENCES_ISREOPEN, isReopen);
        return editor.commit();
    }

    protected boolean saveCurrentPosition()
    {
        Editor editor = mSharedPreferences.edit();
        editor.putInt(AppUtils.PREFERENCESNAME_POSITION_X, mLayoutParams.x);
        editor.putInt(AppUtils.PREFERENCESNAME_POSITION_Y, mLayoutParams.y);
        return editor.commit();
    }

    protected int[] getCurrentPosition()
    {
        int x = mSharedPreferences.getInt(AppUtils.PREFERENCESNAME_POSITION_X,
                DEFAULT_DISPLAY_WIDTH);

        int y = mSharedPreferences.getInt(AppUtils.PREFERENCESNAME_POSITION_Y,
                DEFAULT_DISPLAY_HIGHT);
        return new int[] { x, y };
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
