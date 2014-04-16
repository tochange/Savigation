package com.tochange.yang.sector;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.tochange.yang.R;
import com.tochange.yang.lib.FZProgressBar;
import com.tochange.yang.lib.SimpleLogFile;
import com.tochange.yang.lib.SlideMenu;
import com.tochange.yang.lib.Utils;
import com.tochange.yang.sector.background.AppData;
import com.tochange.yang.sector.background.ListToAdapter;
import com.tochange.yang.sector.service.BaseFloatWindowService;
import com.tochange.yang.sector.service.FloatWindowService;
import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.BackItemInfo;

public class SectorButtonMainActivity extends Activity implements
        OnClickListener
{

    private ListView mListView;

    private ListView mListViewBack;

    private FZProgressBar FZProgressBar;

    private Button start, remove;

    private SlideMenu slideMenu;

    public static SectorButtonMainActivity instance;

    private ArrayList<AppData> mCheckAppList = new ArrayList<AppData>();

    private ArrayList<BackItemInfo> mBackList = new ArrayList<BackItemInfo>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SimpleLogFile.killLogcat();
        SimpleLogFile.captureLogToFile(this, getApplication().getPackageName());

        instance = this;
        Utils.setContext(this);
        setContentView(R.layout.main);
        findView();
        Utils.setProgressBar(FZProgressBar, Color.MAGENTA);

        ListToAdapter listener = new ListToAdapter(this, mListView,
                mListViewBack, mCheckAppList, mBackList);
        listener.myNotify();

        ImageView menuImage = (ImageView) findViewById(R.id.title_bar_menu_btn);
        menuImage.setOnClickListener(this);
        start.setOnClickListener(this);
        remove.setOnClickListener(this);
    }

    private void findView()
    {
        slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
        start = (Button) findViewById(R.id.button_right);
        remove = (Button) findViewById(R.id.button_left);
        mListView = (ListView) findViewById(R.id.listview);
        mListViewBack = (ListView) findViewById(R.id.listview_backpanel);
        FZProgressBar = (FZProgressBar) findViewById(R.id.fancyBar1);
    }

    private class PutParameterTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected void onPreExecute()
        {
            mListView.setVisibility(View.GONE);
            start.setVisibility(View.GONE);
            remove.setVisibility(View.GONE);
            Utils.showFZProgressBar(FZProgressBar);
        }

        @Override
        protected String doInBackground(String... params)
        {
            SharedPreferences sp = getSharedPreferences(
                    AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
            Editor editor = sp.edit();
            Intent intent = new Intent(SectorButtonMainActivity.this,
                    FloatWindowService.class);
            int value = getBackPanelValue();
            intent.putExtra(AppUtils.KEY_ISREOPEN, false);
            intent.putExtra(AppUtils.KEY_BACKPANEL_VALUES, value);
            Display d = getWindowManager().getDefaultDisplay();
            int w = d.getWidth();
            int h = d.getHeight();
            intent.putExtra(AppUtils.KEY_SCREEN_W, w);// 480
            intent.putExtra(AppUtils.KEY_SCREEN_H, h);// 854
            editor.putInt(AppUtils.KEY_SCREEN_W, w);
            editor.putInt(AppUtils.KEY_SCREEN_H, h);

            int allSize = mCheckAppList.size();
            int size = 0;
            for (int i = 0; i < allSize; i++)
            {
                AppData tmp = mCheckAppList.get(i);
                if (tmp.choosed)
                {
                    String imageString = Utils.drawableToByte(tmp.appIcon);
                    intent.putExtra(AppUtils.KEY_PACKAGENAME + size,
                            tmp.packageName);
                    intent.putExtra(AppUtils.KEY_IMAGESTRING + size,
                            imageString);
                    editor.putString(AppUtils.KEY_IMAGESTRING + size,
                            imageString);
                    editor.putString(AppUtils.KEY_PACKAGENAME + size,
                            tmp.packageName);
                    size++;
                }

            }

            editor.putInt(AppUtils.KEY_BACKPANEL_VALUES, value);
            editor.putInt(AppUtils.KEY_SIZE, size);
            editor.commit();
            intent.putExtra(AppUtils.KEY_SIZE, size);
            stopService(intent);// now can change child without close app
            startService(intent);
            return null;
        }

        private int getBackPanelValue()
        {
            int ret = 0;
            int size = mBackList.size();
            for (int i = 0; i < size; i++)
            {
                BackItemInfo tmp = mBackList.get(i);
                if (tmp.choosed)
                    ret |= tmp.value;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(String result)
        {
            finish();
            Utils.closeFZProgressBar(FZProgressBar);
        }

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.title_bar_menu_btn:
                if (slideMenu.isMainScreenShowing())
                    slideMenu.openMenu();
                else
                    slideMenu.closeMenu();
                break;
            case R.id.button_right:
                int i = 0;
                int allSize = mCheckAppList.size();
                for (; i < allSize; i++)
                    if (mCheckAppList.get(i).choosed)
                        break;

                if (i == allSize)
                {
                    Utils.Toast(SectorButtonMainActivity.this,
                            "haven't choose any item!");
                    return;
                }
                new PutParameterTask().execute();
                break;
            case R.id.button_left:
                // how to get service class name?
                if (Utils.serviceIsRunning(SectorButtonMainActivity.this,
                        getPackageName() + ".service.FloatWindowService"))
                {
                    Intent intent = new Intent(SectorButtonMainActivity.this,
                            FloatWindowService.class);
                    stopService(intent);
                }
                else
                {
                    Utils.Toast(SectorButtonMainActivity.this,
                            "haven't open yet!");
                    if (BaseFloatWindowService.mNotificationManager != null)
                        BaseFloatWindowService.mNotificationManager.cancelAll();
                }
                break;
        }

    }
}