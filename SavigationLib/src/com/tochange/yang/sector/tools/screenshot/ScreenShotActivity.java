package com.tochange.yang.sector.tools.screenshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.tochange.yang.lib.log;
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
    private boolean mIsOval = true;

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
        mCaptureView.setOval(mIsOval);
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

    private boolean makeFile(Bitmap b)
    {
        if (b == null)
            return false;
        try
        {
            mPicName = PATH + "/sector" + Utils.getCurTimeToString(-1, 0)
                    + ".png";
            FileOutputStream out = new FileOutputStream(mPicName);
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
        // log.e(cropRect.top + " " + cropRect.bottom + " " + cropRect.left +
        // " "
        // + cropRect.right + " " + cropRect.width() + " "
        // + cropRect.height());
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
        Rect cropRect;

        boolean cutSuccess;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Utils.showFZProgressBar(FZProgressBar);
            cropRect = getCapture(mStatusBarHeight);
            if (cropRect == null)
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
                if (cropRect != null)
                {// rectangle
                    ret = cropImage(ret, cropRect);
                    if (mIsOval)
                        ret = Utils.getOval(ret);
                }
                cutSuccess = makeFile(ret);
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
//                log.e("file=" + mPicName);
                mShowCutImageView.setVisibility(View.VISIBLE);

                mCut.setText(R.string.ok_save);
                mCancel.setText(R.string.cancei_abandon);
            }
            else
            {
                mCancel.setText(R.string.cancel_exit);
                mCut.setText(R.string.ok_full);
                Utils.Toast(ScreenShotActivity.this, "fail to capture,pad or unrooted device is unavailable.");
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
        mIsOval = !mIsOval;
        mCaptureView.setOval(mIsOval);
        showToast();
        return false;
    }

    private void showToast()
    {
        String s;
        if (mIsOval)
            s = "oval";
        else
            s = "rectangle";
        // this can make mShowCutImageView gone,no need to reset (draw auto)
        Utils.Toast(ScreenShotActivity.this, "change to " + s + " model.");
    }
}
