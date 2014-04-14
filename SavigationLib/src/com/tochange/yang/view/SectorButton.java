package com.tochange.yang.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tochange.yang.R;
import com.tochange.yang.lib.Utils;
import com.tochange.yang.lib.log;

public class SectorButton extends RelativeLayout
{

    private final int DELAYED_TIME = 26;

    private final int LINE_TIME = 180;

    private final int SCALE_TIME = 400;

    private final int CHILD_ROTATE_TIME = 680;

    private final int CHILD_WIDTH = 65;// 58 // 65

    // private final int CHILD_WIDTH = 58;// 58 // 65

    private final int FATHER_WIDTH = 85;// 65 // 85

    // private final int FATHER_WIDTH = 65;// 65 // 85

    private int ALL_TIME;

    private final int CLOSE_HANDLE_NUM = 100;

    private final float RADIUS_SCALE = 1.3f;

    // (no so big,0-3 will be good)
    private static final int CHILD_ENLARGE_SCALE = 2;

    private static final short BASE_POSITION = 10;

    // max number depend on physic screen size( < min(width,hight))
    private static final int MENU_HIGHT = 225;

    private List<List<Item>> mAllChildrenList;

    private List<Item> mChildrenList;

    private Item mFatherItem;

    private Context mContext;

    private int mRingMaxVolume;

    private AudioManager mAudioManager;

    private ChildrenInterface mChildrenLinster;

    private int mEvilMarginTop;

    private boolean mCanRotate = true;

    private boolean mFatherVisible;

    private boolean mIsSticky;

    private int mMoveTimes = 0;

    private boolean mIsback = false;

    private boolean mNeedUpdateBackChild = false;

    // private SoundPool mSoundPool;// no work!

    public SectorButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;

        mChildrenList = new ArrayList<Item>();
        // mBackupChildrenList = new ArrayList<Item>();
        // mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mRingMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING);

        // it cost too much memory
        // Timer timer = new Timer();
        // timer.scheduleAtFixedRate(new RefreshTask(), 0, 3000);
    }

    public int getEvilMarginTop()
    {
        return mEvilMarginTop;
    }

    public boolean getSticky()
    {
        return mIsSticky;
    }

    public void setSticky(boolean isSticky)
    {
        mIsSticky = isSticky;
    }

    public void inScaleFather()
    {
        // && !(mFatherItem.getVisibility() == View.INVISIBLE)
        if (!mFatherItem.getIsOpen())
        {
            mFatherVisible = false;
            Animation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(10 * SCALE_TIME);
            mFatherItem.startAnimation(alphaAnimation);
            new SleepTask().execute();
        }
    }

    private void outScaleFather()
    {
        log.e("");
        mFatherItem.clearAnimation();
        mFatherItem.setVisibility(View.VISIBLE);
        mFatherItem.startAnimation(getScaleAnimation(-1, 0f, 1f, LINE_TIME));
    }

    public Item getFatherItem()
    {
        Item fatherItem = new Item(mContext, null);
        fatherItem.setBackgroundResource(R.drawable.composer_father_l);
        mFatherItem = fatherItem;
        return mFatherItem;
    }

    // public void updateBackPanelChild(List<Item> backchild)
    // {
    // mNeedUpdateBackChild = true;
    // // mUpdateBackChild = backchild;
    //
    // }

    public void updateBackPanelChild()
    {
        mNeedUpdateBackChild = true;
    }

    public void initData(List<List<Item>> childListOut)
    {
        mAllChildrenList = childListOut;
        initChild();
    }

    public void setLinster(ChildrenInterface pathAnimMenuLinster)
    {
        mChildrenLinster = pathAnimMenuLinster;
    }

    public void doFatherListener()
    {

        mFatherItem.setOpen(!mFatherItem.getIsOpen());
        if (0 == ALL_TIME)
            ALL_TIME = LINE_TIME * 3 + DELAYED_TIME * mChildrenList.size();
        final LayoutParams lp = (LayoutParams) getLayoutParams();
        if (mFatherItem.getIsOpen())
        {
            mFatherVisible = true;
            // may be no need to do so if you write broadcast receiver to catch
            // all kind of back item event
            if (mIsback)
                mChildrenLinster.updateBackChildList();

            // i donn't know why 318,it just work well
            lp.width = 318;
            lp.height = 318;
            setLayoutParams(lp);
            expendAnim();
            // mHandler.postDelayed(new MyRunnable(OPEN_HANDLE_NUM, mHandler),
            // ALL_TIME - DELAYED_TIME + 1000);
        }
        else
            closedAnim();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                mMoveTimes++;
                if (mCanRotate && mMoveTimes > 5)
                {
                    mCanRotate = false;
                    rotate();
                }
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_OUTSIDE:
                if (mFatherItem.getVisibility() == View.INVISIBLE
                        && !mFatherItem.getIsOpen())
                {
                    outScaleFather();
                }
                mMoveTimes = 0;
                break;

            case MotionEvent.ACTION_UP:
                if (mCanRotate && mFatherItem.getIsOpen())
                    touchToClose();
                mCanRotate = true;
                break;
            default:
                break;
        }

        return true;// super.onTouchEvent(event);
    }

    private void initChild()
    {
        SectorButton.this.removeAllViews();
        mChildrenList.clear();
        if (mIsback)
        {
            mChildrenList.addAll(mAllChildrenList.get(1));
            addAllItems(mAllChildrenList.get(1));
        }
        else
        {// app data load first(in app project),then shortcut data in back side
            mChildrenList.addAll(mAllChildrenList.get(0));
            addAllItems(mAllChildrenList.get(0));// default load app
        }
    }

    private View.OnClickListener mFatherListener = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
            doFatherListener();
        }
    };

    private void touchToClose()
    {
        mFatherItem.setOpen(false);
        closedAnim();
    }

    private View.OnClickListener mChildListener = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
            Item item = (Item) v;
            AnimationSet animationSet = new AnimationSet(getContext(), null);
            Animation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(SCALE_TIME * 2);
            animationSet.addAnimation(alphaAnimation);
            animationSet.addAnimation(getScaleAnimation(
                    (Integer) item.getTag(), 1f, CHILD_ENLARGE_SCALE,
                    SCALE_TIME));
            item.startAnimation(animationSet);
            int index = (Integer) item.getTag();
            int size = mChildrenList.size();
            for (int i = 0; i < size; i++)
            {
                if (index != i)
                {
                    Item tempAnimItem = mChildrenList.get(i);
                    AnimationSet animationSet1 = new AnimationSet(getContext(),
                            null);
                    animationSet1.addAnimation(alphaAnimation);
                    alphaAnimation.setDuration(SCALE_TIME);
                    animationSet1
                            .addAnimation(getScaleAnimation(
                                    (Integer) tempAnimItem.getTag(), 1f, 0f,
                                    SCALE_TIME));
                    tempAnimItem.startAnimation(animationSet1);
                }
            }
            if (null != mChildrenLinster)
            {
                int volume = mAudioManager
                        .getStreamVolume(AudioManager.STREAM_RING);
                MediaPlayer fatherPlayer = MediaPlayer.create(mContext,
                        R.raw.slide_recoil);
                play(fatherPlayer, volume);
                mChildrenLinster.didSelectedItem(mIsback,
                        (Integer) item.getTag());
            }
            mFatherItem.setOpen(false);
            mHandler.postDelayed(new MyRunnable(CLOSE_HANDLE_NUM, mHandler),
                    ALL_TIME);
        }
    };

    private void addAllItems(List<Item> childList)
    {
        if (childList.isEmpty())
        {
            Toast.makeText(getContext(), "haven't choose any child!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // MENU_LENGTH = getLayoutParams().height;// 225
        int childHight = childList.get(0).getViewHeight();

        int far = (int) (RADIUS_SCALE * (MENU_HIGHT - BASE_POSITION - (mFatherItem
                .getViewHeight() + childHight) / 2));
        if (!(far >= 11 * childHight * (CHILD_ENLARGE_SCALE - 1)))
            Utils.Toast(mContext, "CHILD_ENLARGE_SCALE too big");
        // divide into 11 parts
        int end = far / 11 * 10;
        int near = far / 11 * 9;
        double angle = 0;
        int size = mChildrenList.size();
        if (size > 1)
            angle = Math.PI / 180 * (90 / (size - 1));
        // log.e("angle=" + angle);
        // log.e("end=" + end);
        // log.e("near=" + near);
        // log.e("far=" + far);

        // i donn't know why 220,it just work well
        Position startPosition = new Position(BASE_POSITION, far - 220);

        for (int i = 0; i < size; i++)
        {
            Item child = mChildrenList.get(i);
            child.setStartPosition(startPosition);

            double sin = Math.sin(angle * i);
            double cos = Math.cos(angle * i);
            child.setEndPosition(new Position(startPosition.x
                    + (int) (cos * end), (startPosition.y + (int) (sin * end))));
            child.setNearPosition(new Position(startPosition.x
                    + (int) (cos * near),
                    (startPosition.y + (int) (sin * near))));
            child.setFarPosition(new Position(startPosition.x
                    + (int) (cos * far), (startPosition.y + (int) (sin * far))));
            child.setOnClickListener(mChildListener);
            child.setLayoutParams(getLayoutParams(child.getStartPosition(),
                    child, false));

            child.setTag(i);
            child.setVisibility(View.GONE);
            addView(child);
        }
        Position startPosition1 = new Position(BASE_POSITION, far);
        mFatherItem.setStartPosition(startPosition1);
        mFatherItem.setOnClickListener(mFatherListener);
        mFatherItem.setLayoutParams(getLayoutParams(
                mFatherItem.getStartPosition(), mFatherItem));
        addView(mFatherItem);
        mFatherItem.setOpen(false);
        invalidate();
    }

    private void closedAnim()
    {
        mHandler.postDelayed(new MyRunnable(CLOSE_HANDLE_NUM, mHandler),
                ALL_TIME - DELAYED_TIME);

        mFatherItem.startAnimation(getRotateAnimation(-270f, 0.0f, 0.5f, 0.5f));
        int size = mChildrenList.size();
        for (int i = 0; i < size; i++)
        {
            mHandler.postDelayed(new MyRunnable(i, mHandler), DELAYED_TIME
                    * (size - 1 - i));
        }
    }

    private void expendAnim()
    {
        mFatherItem.startAnimation(getRotateAnimation(0, -270f, 0.5f, 0.5f));
        int size = mChildrenList.size();
        for (int i = 0; i < size; i++)
            mHandler.postDelayed(new MyRunnable(i, mHandler), DELAYED_TIME * i);
    }

    /**
     * it is hard to control picture resource's pixel,so set it stable,other
     * data are use for adjust to layout
     */
    private RelativeLayout.LayoutParams getLayoutParams(Position position,
            Item fatherItem)
    {
        // log.e("f y=" + position.y);
        RelativeLayout.LayoutParams result = getLayoutParams(position);
        result.height = FATHER_WIDTH;// 65-85
        result.width = FATHER_WIDTH;

        result.bottomMargin = 0;
        result.leftMargin = result.leftMargin - BASE_POSITION;
        return result;
    }

    private RelativeLayout.LayoutParams getLayoutParams(Position position,
            Item childItem, boolean isOpen)
    {
        // log.e("c y=" + position.y);
        RelativeLayout.LayoutParams result = getLayoutParams(position);
        result.height = CHILD_WIDTH;// mFatherItem.getViewHeight();//58-65
        result.width = CHILD_WIDTH;// mFatherItem.getViewHeight();
        // when use 58
        // result.leftMargin = result.leftMargin - BASE_POSITION / 2 - 1;
        result.leftMargin = result.leftMargin - BASE_POSITION / 2 + 4;
        // when use 58
        // result.topMargin = result.topMargin + BASE_POSITION / 2;
        if (isOpen)
        {
            // log.e("open top");
            result.topMargin = position.y;
        }
        else
        {
            // log.e("not open top");
            result.topMargin = 0;
        }
        result.topMargin = result.topMargin + BASE_POSITION / 2 + 4;
        return result;
    }

    private RelativeLayout.LayoutParams getLayoutParams(Position position)
    {
        RelativeLayout.LayoutParams result = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // result.topMargin = position.y;
        if (position.y > mEvilMarginTop)
        {
            // log.e("giving evil count..");
            mEvilMarginTop = position.y;
        }

        result.leftMargin = position.x;
        return result;
    }

    /**
     * it is hard to control picture resource's pixel,so set it stable,other
     * data are use for adjust to layout
     */

    protected Animation getRotateAnimation(float fromDegress, float toDegrees,
            float pivotXValue, float pivotYValue)
    {
        RotateAnimation animation = new RotateAnimation(fromDegress, toDegrees,
                Animation.RELATIVE_TO_SELF, pivotXValue,
                Animation.RELATIVE_TO_SELF, pivotYValue);
        animation.setDuration(ALL_TIME);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                animation.setFillAfter(true);
            }
        });
        return animation;
    }

    protected Animation getTranslateAnimation(Position fromPosition,
            Position toPosition)
    {
        TranslateAnimation anTransformation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, toPosition.x
                        - fromPosition.x, Animation.RELATIVE_TO_SELF, 0,
                Animation.ABSOLUTE, toPosition.y - fromPosition.y);
        anTransformation.setDuration(LINE_TIME);
        return anTransformation;
    }

    private Animation getExpendAnimation(final int itemIndex)
    {
        final Item item = mChildrenList.get(itemIndex);
        item.setVisibility(View.VISIBLE);
        Animation animation = getTranslateAnimation(item.getStartPosition(),
                item.getFarPosition());
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                item.clearAnimation();
                item.setLayoutParams(getLayoutParams(item.getFarPosition(),
                        item, true));

                animation = getTranslateAnimation(item.getFarPosition(),
                        item.getNearPosition());
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        item.clearAnimation();
                        item.setLayoutParams(getLayoutParams(
                                item.getNearPosition(), item, true));
                        animation = getTranslateAnimation(
                                item.getNearPosition(), item.getEndPosition());
                        animation.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation)
                            {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation)
                            {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation)
                            {
                                item.setLayoutParams(getLayoutParams(
                                        item.getEndPosition(), item, true));
                                item.clearAnimation();
                            }
                        });
                        item.startAnimation(animation);
                    }
                });
                item.startAnimation(animation);
            }
        });
        return animation;
    }

    private Animation getCloseAnimation(final int itemIndex)
    {
        final Item item = mChildrenList.get(itemIndex);
        Animation animation = getTranslateAnimation(item.getEndPosition(),
                item.getFarPosition());
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                item.setLayoutParams(getLayoutParams(item.getFarPosition(),
                        item, true));

                animation = getTranslateAnimation(item.getFarPosition(),
                        item.getStartPosition());
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        item.setLayoutParams(getLayoutParams(
                                item.getStartPosition(), item, false));
                        item.clearAnimation();
                    }
                });
                item.startAnimation(animation);
            }
        });
        return animation;
    }

    protected Animation getScaleAnimation(final int itemIndex, float from,
            float to, int time)
    {
        ScaleAnimation scaleAnimation = new ScaleAnimation(from, to, from, to,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnimation.setDuration(time);
        if (itemIndex >= 0)
        {
            scaleAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation)
                {
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {
                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    Item item = mChildrenList.get(itemIndex);
                    item.clearAnimation();
                    item.setLayoutParams(getLayoutParams(
                            item.getStartPosition(), item, false));

                }
            });
        }
        return scaleAnimation;
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            int what = msg.what;
            if (mChildrenList.size() > what)
            {
                MediaPlayer fatherPlayer;
                int volume = mAudioManager
                        .getStreamVolume(AudioManager.STREAM_RING);
                Item tmpitem = mChildrenList.get(what);
                int tag = (Integer) tmpitem.getTag();
                if (mFatherItem.getIsOpen())
                {
                    if (0 == tag)
                    {
                        fatherPlayer = MediaPlayer.create(mContext,
                                R.raw.nav_out);
                        play(fatherPlayer, volume);
                    }
                    Animation animation = getExpendAnimation(tag);
                    tmpitem.startAnimation(animation);
                }
                else
                {
                    RotateAnimation animation1 = new RotateAnimation(0, 360f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animation1.setDuration(CHILD_ROTATE_TIME);
                    if (mChildrenList.size() - 1 == tag)
                    {
                        fatherPlayer = MediaPlayer.create(mContext,
                                R.raw.nav_in);
                        play(fatherPlayer, volume);
                    }
                    Animation animation2 = getCloseAnimation(tag);
                    AnimationSet animationSet = new AnimationSet(getContext(),
                            null);
                    animationSet.addAnimation(animation1);
                    animationSet.addAnimation(animation2);
                    animationSet.setDuration(SCALE_TIME / 2);
                    tmpitem.startAnimation(animationSet);
                }
            }
            else if (CLOSE_HANDLE_NUM == what)
            {
                if (!mFatherItem.getIsOpen())
                {
                    for (int i = 0; i < mChildrenList.size(); i++)
                        mChildrenList.get(i).setVisibility(View.GONE);
                    // mFatherItem.setLayoutParams(getLayoutParams(
                    // mFatherItem.getStartPoint(), mFatherItem));
                    // ScaleAnimation scaleAnimation = new ScaleAnimation(1f,
                    // 0.9f, 1f, 0.9f,
                    // Animation.RELATIVE_TO_SELF, 0f,
                    // Animation.RELATIVE_TO_SELF,
                    // 0f);
                    // scaleAnimation.setDuration(50);
                    // scaleAnimation.setAnimationListener(new
                    // AnimationListener(){
                    // @Override
                    // public void onAnimationEnd(Animation animation) {
                    // // TODO Auto-generated method stub
                    // // LayoutParams lp = (LayoutParams) getLayoutParams();
                    // // lp.width = FATHER_WIDTH;
                    // // lp.height = FATHER_WIDTH;
                    // // setLayoutParams(lp);
                    // }
                    // @Override
                    // public void onAnimationRepeat(Animation animation) {
                    // }
                    // @Override
                    // public void onAnimationStart(Animation animation) {
                    // }
                    // });
                    // setAnimation(scaleAnimation);

                    LayoutParams lp = (LayoutParams) getLayoutParams();
                    lp.width = FATHER_WIDTH;
                    lp.height = FATHER_WIDTH;
                    setLayoutParams(lp);

                    if (mNeedUpdateBackChild)
                    {
                        initChild();
                        mNeedUpdateBackChild = false;
                    }
                    if (mIsSticky)
                    {
                        inScaleFather();
                        // no work
                        // new SleepTask().execute();
                    }
                }
            }
        }
    };

    private void rotate()
    {
        Animation aniback = AnimationUtils.loadAnimation(mContext,
                R.anim.rotate_back);
        aniback.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                // make father clear
                mIsback = !mIsback;
                initData(mAllChildrenList);
                doFatherListener();
                SectorButton.this.startAnimation(AnimationUtils.loadAnimation(
                        mContext, R.anim.rotate_font));
            }
        });
        SectorButton.this.startAnimation(aniback);
    }

    private void play(MediaPlayer fatherPlayer, int volume)
    {
        float f = (float) volume / mRingMaxVolume;
        fatherPlayer.setVolume(f, f);
        fatherPlayer.start();
    }

    private class MyRunnable implements Runnable
    {

        public MyRunnable(int intData, Handler handler)
        {
            super();
            this.intData = intData;
            this.mHandler = handler;
        }

        private volatile int intData;

        private Handler mHandler;

        @Override
        public void run()
        {
            mHandler.sendEmptyMessage(intData);
        }
    }

    private class SleepTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... params)
        {
            Utils.sleep(10 * SCALE_TIME);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (mFatherVisible)
                mFatherItem.setVisibility(View.VISIBLE);
            else
                mFatherItem.setVisibility(View.INVISIBLE);
            super.onPostExecute(result);
        }

    }
}
