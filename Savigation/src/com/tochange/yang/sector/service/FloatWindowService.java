package com.tochange.yang.sector.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.tochange.yang.lib.Utils;
import com.tochange.yang.lib.log;
import com.tochange.yang.lib.ui.ScreenLib;
import com.tochange.yang.lib.utils.ApkInstaller;
import com.tochange.yang.sector.R;
import com.tochange.yang.sector.screenobserver.ScreenObserver;
import com.tochange.yang.sector.screenobserver.ScreenObserver.ScreenStateListener;
import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.BackItemInfo;
import com.tochange.yang.sector.tools.BackPanelBin;
import com.tochange.yang.view.ChildrenInterface;

public class FloatWindowService extends BaseFloatWindowService
{
    private class MyOnGestureListener extends SimpleOnGestureListener
    {
        @Override
        public void onLongPress(MotionEvent e)
        {
            // mSectorButton.setSticky(!mIsSticky);
            // stickBorder();
            // mIsSticky = !mIsSticky;
            Utils.vibrate(FloatWindowService.this);
            mCanMove = !mCanMove;
            int res = mCanMove ? R.drawable.composer_father_l
                    : R.drawable.composer_father_ll;
            mFatherItem.setBackgroundResource(res);
            // ensure change the background
            mMainView.invalidate();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY)
        {
            mIsMoving = true;
            if (mCanMove)
            {
                mLayoutParams.x = (int) e2.getRawX()
                        - mFatherItem.getMeasuredWidth() / 2;
                mLayoutParams.y = (int) e2.getRawY()
                        - mFatherItem.getMeasuredHeight() / 2
                        - ScreenLib.getStatusBarHeight(FloatWindowService.this);

                // log.e("mLayoutParams.y=" + mLayoutParams.y
                // + "  mLayoutParams.x=" + mLayoutParams.x);

                // if (mFatherItem.getIsOpen())
                // mLayoutParams.y -= mEvilMarginTop;
                mWindowManager.updateViewLayout(mFloatLayout, mLayoutParams);
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            if (saveIsReopen(true))
                mMainView.playHiddenAnimation();
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            mMainView.doFatherListener();
            return false;
        }

    }

    @Override
    public void stickBorder()
    {
        if (mScreanW == 0 && mScreanH == 0)
        {
            mScreanW = mSharedPreferences.getInt(AppUtils.KEY_SCREEN_W,
                    DEFAULT_DISPLAY_WIDTH);
            mScreanH = mSharedPreferences.getInt(AppUtils.KEY_SCREEN_H,
                    DEFAULT_DISPLAY_HIGHT);
        }
        boolean isXcoordinate = false;
        int tovalue = 0;

        int top = Math.abs(mLayoutParams.y);
        int bottom = Math.abs(mLayoutParams.y - mScreanH);
        int left = Math.abs(mLayoutParams.x);
        int right = Math.abs(mScreanW - mLayoutParams.x);
        // bottom
        if (bottom < top && bottom < left && bottom < right)
        {
            isXcoordinate = false;
            tovalue = mScreanH;
        }
        // top
        else if (bottom > top && top < left && top < right)
        {
            isXcoordinate = false;
            tovalue = 0;
        }
        // left
        else if (left < top && right > left && bottom > left)
        {
            isXcoordinate = true;
            tovalue = 0;
        }
        // right
        else if (right < top && right < left && bottom > right)
        {
            isXcoordinate = true;
            tovalue = mScreanW;
        }

        new StickyUpdateTask(isXcoordinate, tovalue).execute();
    }

    private class HiddenTask extends AsyncTask
    {
        int allTime;

        boolean isHidden;

        public HiddenTask(boolean isHidden, int allTime)
        {
            this.isHidden = isHidden;
            this.allTime = allTime;
        }

        @Override
        protected Object doInBackground(Object... arg0)
        {
            if (isHidden)
            {
                int times = allTime / mSleepTime;
                float offX = mLayoutParams.x / times;
                float offY = mLayoutParams.y / times;
                for (int i = 0; i < times || mLayoutParams.x > 0
                        || mLayoutParams.y > 0; i++, Utils.sleep(mSleepTime))
                {
                    mLayoutParams.x -= offX;
                    mLayoutParams.y -= offY;
                    publishProgress(mLayoutParams);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values)
        {
            super.onProgressUpdate(values);
            if (!mAlreadyDestory)
                mWindowManager.updateViewLayout(mFloatLayout, mLayoutParams);
        }

        @Override
        protected void onPostExecute(Object result)
        {
            super.onPostExecute(result);
            if (isHidden)
                doSomethingOnScreenOff();
        }

    }

    private class StickyUpdateTask extends AsyncTask
    {
        int tovalue;

        boolean isXcoordinate;

        public StickyUpdateTask(boolean isXcoordinate, int toValue)
        {
            this.tovalue = toValue;
            this.isXcoordinate = isXcoordinate;
        }

        private void slowDown(boolean isXcoordinate, int toValue)

        {
            if (isXcoordinate)
            {
                if (toValue == 0)
                {
                    for (; mLayoutParams.x > 0 && !mStickyHasReset; Utils
                            .sleep(mSleepTime))
                    {
                        // log.e("x 0 =" + mLayoutParams.x);
                        mLayoutParams.x -= STICKY_OFFSET;
                        publishProgress(mLayoutParams);
                    }
                }
                else if (toValue == mScreanW)
                {
                    for (; mLayoutParams.x < toValue && !mStickyHasReset; Utils
                            .sleep(mSleepTime))
                    {
                        // log.e("x -0 =" + mLayoutParams.x);
                        mLayoutParams.x += STICKY_OFFSET;
                        publishProgress(mLayoutParams);
                    }
                }

            }
            else
            {
                if (toValue == 0)
                {
                    for (; mLayoutParams.y > 0 && !mStickyHasReset; Utils
                            .sleep(mSleepTime))
                    {
                        // log.e("y 0 =" + mLayoutParams.y);
                        mLayoutParams.y -= STICKY_OFFSET;
                        publishProgress(mLayoutParams);
                    }
                }
                else if (toValue == mScreanH)
                {
                    for (; mLayoutParams.y < toValue && !mStickyHasReset; Utils
                            .sleep(mSleepTime))
                    {
                        // log.e("y -0 =" + mLayoutParams.y);
                        mLayoutParams.y += STICKY_OFFSET;
                        publishProgress(mLayoutParams);
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Object... values)
        {
            super.onProgressUpdate(values);
            if (!mAlreadyDestory)
                mWindowManager.updateViewLayout(mFloatLayout, mLayoutParams);
        }

        @Override
        protected void onPostExecute(Object result)
        {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            {
                if (mMainView.getSticky())
                {
                    mMainView.inScaleFather();
                    mMainView.invalidate();
                }
            }
        }

        @Override
        protected Object doInBackground(Object... params)
        {
            slowDown(isXcoordinate, tovalue);
            return null;
        }

    }

    @Override
    public GestureDetector getGestureDetector()
    {
        return new GestureDetector(this, new MyOnGestureListener());
    }

    @Override
    public ChildrenInterface getChildrenLinster()
    {
        return new ChildrenInterface() {
            @Override
            public void didSelectedItem(boolean isBackPanel, int index)
            {
                // sometimes doesn't work
                // openApp(getPackageName(index));
                if (isBackPanel)
                    mBackPanelBin.openToolByType((mChoosedBackClildList
                            .get(index)).value);
                else
                    new LauncherAppTask().execute("" + index);
            }

            @Override
            public void updateBackChildList()
            {
                int size = mChoosedBackClildList.size();
                for (int i = 0; i < size; i++)
                {
                    BackItemInfo tmp = mChoosedBackClildList.get(i);
                    int res = BackPanelBin.isAble(tmp.value) ? tmp.iconResOn
                            : tmp.iconResOff;
                    mChoosedBackClildItemList.get(i).setBackgroundDrawable(
                            getResources().getDrawable(res));
                    mMainView.updateBackPanelChild();
                }
            }

            @Override
            public void showHiddenAnimation(boolean isHidden, int allTime)
            {
                if (isHidden)// && saveCurrentPosition()//no need to do so
                    new HiddenTask(true, allTime).execute();
            }
        };
    }

    private class LauncherAppTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... arg0)
        {
            ApkInstaller.openAppTroublesome(FloatWindowService.this,
                    getPackageName(Integer.parseInt(arg0[0])));
            return null;
        }

    }

    private String getPackageName(int index)
    {
        String ret;
        if (null != mIntent)
            ret = mIntent.getStringExtra(AppUtils.KEY_PACKAGENAME + index);
        else
            ret = mSharedPreferences.getString(
                    AppUtils.KEY_PACKAGENAME + index, "default packagename");
        return ret;
    }

    public void sendNotification(int type, int id, String s)
    {
        switch (type)
        {
            case CLEAR_NOTIFICATION:
                mNotificationManager.cancelAll();
                break;
            case SEND_NOTIFICATION:
                mNotificationManager.notify(3, getNotification(s));
                this.stopService(mIntent);
                break;
        }
    }

    private Notification getNotification(String s)
    {
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), "title..",
                "content..", getPendingIntent());
        notification.icon = R.drawable.composer_father_s;
        notification.tickerText = s;
        notification.contentView = getRemoteViews();
        return notification;
    }

    private PendingIntent getPendingIntent()
    {
        mIntent.putExtra(AppUtils.KEY_ISREOPEN, true);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getService(this, 0, mIntent, 0);
    }

    private RemoteViews getRemoteViews()
    {
        RemoteViews contentView = new RemoteViews(getPackageName(),
                R.layout.notification_item);
        contentView.setImageViewResource(R.id.image,
                R.drawable.composer_father_l);
        contentView.setTextViewText(R.id.text, "Click me to reopen..");
        contentView.setTextViewText(R.id.time, getTimeString());
        return contentView;
    }

    private String getTimeString()
    {
        Time t = new Time();
        t.setToNow();
        return t.hour + ":" + t.minute;
    }

    @Override
    public void setLayoutParamsWidthAndHight(LayoutParams layouparameter)
    {

        if (null != mIntent
                && mIntent.getExtras() != null
                && !mIntent.getExtras()
                        .getBoolean(AppUtils.KEY_ISREOPEN, false))
        {
            mScreanW = mIntent.getIntExtra(AppUtils.KEY_SCREEN_W,
                    DEFAULT_DISPLAY_WIDTH);
            mScreanH = mIntent.getIntExtra(AppUtils.KEY_SCREEN_H,
                    DEFAULT_DISPLAY_HIGHT);

            layouparameter.x = (int) (1.0 / 6 * mScreanW);
            layouparameter.y = (int) (1.0 / 4 * mScreanH);
            saveCurrentPosition();

        }
        else
        {
            int[] p = getCurrentPosition();
            layouparameter.x = p[0];
            layouparameter.y = p[1];
        }
    }

    private void uiDeal()
    {
        if (mFloatLayout != null)
        {
            mWindowManager.removeView(mFloatLayout);
            mCanNew = true;
        }
    }

    private void beginScreenObserver()
    {
        mScreenObserver = new ScreenObserver(this);
        mScreenObserver.requestScreenStateUpdate(new ScreenStateListener() {
            @Override
            public void onScreenOn()
            {// no use,yangxj@20140609
                doSomethingOnScreenOn();
            }

            @Override
            public void onScreenOff()
            {
                doSomethingOnScreenOff();
            }

            @Override
            public void onDialogClose(Intent intent)
            {// no use,yangxj@20140609
                String reason = intent.getStringExtra("reason");
                //when alarm comes,this will call,but reason is null
                if (reason != null)
                {
                    if (reason.equals("homekey"))
                        log.e("home key");
                    else if (reason.equals("recentapps"))
                        log.e("recentapps key");
                    else
                        log.e("other");
                }
            }

            // @Override
            // public void onUserPresent() {
            // doSomethingOnUserPresent();
            // }
        });
    }

    private void doSomethingOnScreenOn()
    {
        log.e("Screen on");
    }

    private void doSomethingOnScreenOff()
    {
        if (mIntent != null)
            sendNotification(SEND_NOTIFICATION, 2, "i'am hiding here..  >.<");
    }

    @Override
    public void initEnvironment()
    {// a little arbitrary,when new data comes
     // just abandon old data
        sendNotification(CLEAR_NOTIFICATION, -1, null);
        beginScreenObserver();
        saveIsReopen(false);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mAlreadyDestory = true;
        mBackPanelBin.detach();
        mCanReStartShake = false;
        mShakeListener.stopShakeListen();
        mScreenObserver.stopScreenObserver();
        uiDeal();
        log.e("onDestory");
    }
}
