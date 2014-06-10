package com.tochange.yang.sector.tools.screenshot;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tochange.yang.R;
import com.tochange.yang.lib.FZProgressBar;
import com.tochange.yang.lib.Utils;
import com.tochange.yang.sector.tools.AppUtils;

public class ScreenShotActivity extends Activity implements OnClickListener,
        OnLongClickListener
{
    /**
     * default path to save pictures
     */
    private final String PATH = AppUtils.SCREENSHOT_PATH;

    /**
     * default Oval model, value is true
     */
    private int mShape;

    public static final int SHAPE_TOTAL_NUM = 3;

    public static final int SHAPE_RECT = 0;

    public static final int SHAPE_OVAL = 1;

    public static final int SHAPE_CUSTOM = 2;

    /**
     * cut result view
     */
    private ScreenShotView mCaptureView;

    private ImageView mShowCutImageView;

    private RelativeLayout mAllRelativeLayout;

    private boolean mIsLongPress;

    private DisplayMetrics mDisplayMetrics;

    private FZProgressBar FZProgressBar;

    private int mStatusBarHeight;

    private Button mCancel;

    private Button mCut;

    private String mPicName;

    private String[] mPicFile;

    private ScreenShotViewListener mScreenShotViewTouchListener;

    public static ScreenShotActivity instance;

    interface CaptureCallBack
    {
        void upActionNotify();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        instance = this;
        mPicFile = new String[1];
        // no need,if so,take status bar height into consider
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.screenshot_view);
        findView();
        setView();
        setCaptureView();
        mScreenShotViewTouchListener = new ScreenShotViewListener(this,
                mPicFile, mShowCutImageView);
        mShowCutImageView.setOnTouchListener(mScreenShotViewTouchListener);
        mCancel.setOnClickListener(this);
        mCut.setOnClickListener(this);
        mCut.setOnLongClickListener(this);
    }

    private void findView()
    {
        mCancel = (Button) findViewById(R.id.button_left);
        mCut = (Button) findViewById(R.id.button_right);
        FZProgressBar = (FZProgressBar) findViewById(R.id.fancyBarl);
        mAllRelativeLayout = (RelativeLayout) findViewById(R.id.allview);
        mShowCutImageView = (ImageView) findViewById(R.id.show_cut);
        mCaptureView = (ScreenShotView) findViewById(R.id.captureview);
    }

    private void setView()
    {
        mCancel.setText(R.string.cancel_exit);
        mCut.setText(R.string.ok_full);
        mStatusBarHeight = Utils.getStatusBarHeight(ScreenShotActivity.this);
        mDisplayMetrics = getResources().getDisplayMetrics();
        Utils.setProgressBar(FZProgressBar, Color.MAGENTA, Color.CYAN);
    }

    private void setCaptureView()
    {
        mCaptureView.setCallBack(new CaptureCallBack() {
            public void upActionNotify()
            {
                mPicName = "";// new file comes,don'nt delete the old
                updateAfterCutButtonUI();
            }

        });
        mCaptureView.setShape(SHAPE_RECT);// default shape
        mCaptureView.setVisibility(View.VISIBLE);
    }

    private void updateAfterCutButtonUI()
    {
        mCut.setText(R.string.ok_cut);
        mCancel.setText(R.string.cancei_abandon);

    }

    private boolean checkPath(String path)
    {
        File f = new File(PATH);
        if (!f.exists())
            return f.mkdir();
        return true;
    }

    private Bitmap takeScreenShot(int x, int y, int width, int hight)
    {
        // log.e("x=" + x + " y=" + y + " width=" + width + " hight=" + hight);
        if (checkPath(PATH))
            return ScreenShotWorker.getScreenBitmap(this);
        return null;
    }

    private void deleteFile()
    {
        if (mPicName != null && !mPicName.equals(""))
        {
            File f = new File(mPicName);
            if (f.exists())
                f.delete();
        }
    }

    @Override
    public void onClick(View v)
    {
        mShowCutImageView.setVisibility(View.GONE);
        if (v.getId() == R.id.button_left)
        {
            if (mCancel.getText().toString()
                    .equals(getResources().getString(R.string.cancel_exit)))
                finish();
            else
                deleteFile();
            mCaptureView.reset();

            // mCaptureView.setVisibility(View.VISIBLE);
            mCancel.setText(R.string.cancel_exit);
            mCut.setText(R.string.ok_full);
        }
        else if (v.getId() == R.id.button_right)
        {
            if (!mIsLongPress)
            {
                if (mCut.getText().toString()
                        .equals(getResources().getString(R.string.ok_save)))
                {
                    Utils.Toast(this, "save to:" + mPicName);
                    mPicName = "";// more sure for cann't be deleted
                    mCut.setText(R.string.ok_full);
                    mCancel.setText(R.string.cancel_exit);
                }
                else
                    new TakeScreenShotTask().execute();
            }
            mIsLongPress = false;
        }
    }

    // cut into rectangle
    private Bitmap cropImage(Bitmap bit, Rect cropRect)
    {
        int width = cropRect.width();
        int height = cropRect.height();
        Bitmap croppedImage = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedImage);
        Rect dstRect = new Rect(0, 0, width, height);
        canvas.drawBitmap(bit, cropRect, dstRect, null);
        return croppedImage;
    }

    private Rect getCapture(int statusBarHight)
    {
        Rect cropRect = mCaptureView.getCaptureRect(statusBarHight);
        int width = cropRect.width();
        int height = cropRect.height();
        if (width <= 0 || height <= 0)
            return null;
        return cropRect;
    }

    private class TakeScreenShotTask extends AsyncTask<String, Integer, String>
    {
        private Rect cropRect;

        private Path p;

        private boolean cutSuccess;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Utils.showFZProgressBar(FZProgressBar);
            cropRect = getCapture(mStatusBarHeight);
            p = mCaptureView.getCustomPath(mStatusBarHeight);
            // is custom shape and hasn't capture;not custom shape(maybe cause
            // problems when there are not mutual condition) and hasn't capture
            if ((((mShape % ScreenShotActivity.SHAPE_TOTAL_NUM) == ScreenShotActivity.SHAPE_CUSTOM) && (p == null))
                    || ((((mShape % ScreenShotActivity.SHAPE_TOTAL_NUM) != ScreenShotActivity.SHAPE_CUSTOM)) && (cropRect == null)))
            {
                mAllRelativeLayout.setVisibility(View.GONE);
                // Utils.Toast(ScreenShotActivity.this, "haven't capture!");
                // return;
            }
        }

        @Override
        protected String doInBackground(String... arg0)
        {

            /**
             * no need root,but only get current activity's view you can get
             * ActivityTask,and find the top activity in stack, but i didn't get
             * the root view,someone says 4.0+ has ScreenShot API,but hidden
             */
            /*
             * View v = ScreenShotActivity.this.getWindow().getDecorView();
             * v.setDrawingCacheEnabled(true); v.buildDrawingCache(); Bitmap ret
             * = v.getDrawingCache();
             */
            // get full screen
            Bitmap ret = takeScreenShot(0, 0 + mStatusBarHeight,
                    mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels
                            - mStatusBarHeight);

            // CopyOfScreenShotActivity cc = new CopyOfScreenShotActivity(1);
            // Bitmap ret = cc.getScreenShot(cc.getDevice());
            if (ret != null)
            {
                mPicName = PATH + File.separator
                        + AppUtils.SCREENSHOT_PICPREFIX
                        + Utils.getCurTimeToString(-1, 0) + ".png";
                if (mShape % SHAPE_TOTAL_NUM == SHAPE_CUSTOM)
                {
                    cutSuccess = (p == null) ? Utils.bitmapToPNGFile(ret,
                            mPicName) : Utils.bitmapToPNGFile(
                            Utils.getPathBitmap(ret, p), mPicName);
                    return null;
                }
                if (cropRect != null)
                {// rectangle
                    ret = cropImage(ret, cropRect);
                    if (mShape % SHAPE_TOTAL_NUM == SHAPE_OVAL)
                        ret = Utils.getOval(ret);
                }
                cutSuccess = Utils.bitmapToPNGFile(ret, mPicName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            Utils.closeFZProgressBar(FZProgressBar);
            if (!(mAllRelativeLayout.getVisibility() == View.VISIBLE))
                mAllRelativeLayout.setVisibility(View.VISIBLE);
            if (cutSuccess)
            {
                mPicFile[0] = mPicName;
                mShowCutImageView.setImageBitmap(BitmapFactory
                        .decodeFile(mPicName));
                mShowCutImageView.setVisibility(View.VISIBLE);

                mCut.setText(R.string.ok_save);
                mCancel.setText(R.string.cancei_abandon);
            }
            else
            {
                mCancel.setText(R.string.cancel_exit);
                mCut.setText(R.string.ok_full);
                Utils.Toast(ScreenShotActivity.this,
                        "fail to capture,pad or unrooted device is unavailable.");
            }
            mCaptureView.reset();
            // not working
            mScreenShotViewTouchListener.reset(mDisplayMetrics.widthPixels / 2,
                    mDisplayMetrics.heightPixels / 2);
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        mIsLongPress = true;
        mShape++;
        mCaptureView.setShape(mShape % SHAPE_TOTAL_NUM);
        showToast();
        Utils.vibrate(this);
        return false;
    }

    @Override
    protected void onDestroy()
    {// refresh to see screen shot file direct
        sendBroadcast(new Intent(
                Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        super.onDestroy();
    }

    private void showToast()
    {
        String s;
        switch (mShape % SHAPE_TOTAL_NUM)
        {
            case SHAPE_CUSTOM:// 2
                s = "Custom";
                break;
            case SHAPE_OVAL:// 1
                s = "Oval";
                break;
            case SHAPE_RECT:// 0
                s = "Rectangle";
                break;
            default:
                s = "unknow shape";
        }

        // this can make mShowCutImageView gone,no need to reset (draw auto)
        Utils.Toast(ScreenShotActivity.this, s + " model");
    }
}
