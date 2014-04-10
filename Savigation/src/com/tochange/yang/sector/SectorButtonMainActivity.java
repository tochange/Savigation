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
import android.widget.Toast;

import com.tochange.yang.FZProgressBar;
import com.tochange.yang.R;
import com.tochange.yang.SlideMenu;
import com.tochange.yang.Utils;
import com.tochange.yang.sector.background.AppData;
import com.tochange.yang.sector.background.ListToAdapter;
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

        // SimpleLogFile.captureLogToFile(this, getApplication()
        // .getPackageName());

        instance = this;
        Utils.setContext(this);
        setContentView(R.layout.main);
        slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
        start = (Button) findViewById(R.id.button_right);
        remove = (Button) findViewById(R.id.button_left);
        mListView = (ListView) findViewById(R.id.listview);
        mListViewBack = (ListView) findViewById(R.id.listview_backpanel);
        FZProgressBar = (FZProgressBar) findViewById(R.id.fancyBar1);
        ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);
        Utils.setProgressBar(FZProgressBar, Color.MAGENTA);

        // it seems much faster than use the PutParameterTask below
        ListToAdapter listener = new ListToAdapter(this, mListView,
                mListViewBack, mCheckAppList, mBackList);
        listener.myNotify();

        // new PutParameterTask(true).execute();

        menuImg.setOnClickListener(this);
        start.setOnClickListener(this);
        remove.setOnClickListener(this);
    }

    private class PutParameterTask extends AsyncTask<String, Integer, String>
    {
        boolean isOnCreate;

        ListToAdapter listener;

        public PutParameterTask(boolean isOnCreate)
        {
            this.isOnCreate = isOnCreate;
        }

        @Override
        protected void onPreExecute()
        {
            if (!isOnCreate)
            {
                mListView.setVisibility(View.GONE);
                start.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
            Utils.showFZProgressBar(FZProgressBar);
        }

        @Override
        protected String doInBackground(String... params)
        {
            if (!isOnCreate)
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
                intent.putExtra(AppUtils.KEY_SCREEN_W, d.getWidth());// 480
                intent.putExtra(AppUtils.KEY_SCREEN_H, d.getHeight());// 854
             
                
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
                        // log.e("size=" + size);
                    }

                }
                editor.putInt(AppUtils.KEY_SCREEN_W, d.getWidth());
                editor.putInt(AppUtils.KEY_SCREEN_H, d.getHeight());
                
                editor.putInt(AppUtils.KEY_BACKPANEL_VALUES, value);
                editor.putInt(AppUtils.KEY_SIZE, size);
                editor.commit();
                // log.e("value=" + value);
                intent.putExtra(AppUtils.KEY_SIZE, size);
                // log.e("w=" + d.getWidth() + "  h=" + d.getHeight());
                stopService(intent);// now can change child without close app
                startService(intent);
            }
            else
            {
                listener = new ListToAdapter(SectorButtonMainActivity.this,
                        mListView, mListViewBack, mCheckAppList, mBackList);
            }
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
        protected void onProgressUpdate(Integer... values)
        {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (!isOnCreate)
                finish();
            else
            {
                // listener.adapter.notifyDataSetChanged();
                // listener.adapterb.notifyDataSetChanged();
                listener.myNotify();

            }
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
                    if (AppUtils.isGorgeousModel)
                        Utils.Toast(SectorButtonMainActivity.this,
                                "haven't choose any item!");
                    else
                        Toast.makeText(SectorButtonMainActivity.this,
                                "haven't open yet!", Toast.LENGTH_SHORT).show();

                    return;
                }
                new PutParameterTask(false).execute();
                break;
            case R.id.button_left:
                // how to get service class name?
                if (Utils.serviceIsRunning(SectorButtonMainActivity.this,
                        getPackageName() + ".FloatWindowService"))
                {
                    Intent intent = new Intent(SectorButtonMainActivity.this,
                            FloatWindowService.class);
                    stopService(intent);
                }
                else
                {
                    if (AppUtils.isGorgeousModel)
                        Utils.Toast(SectorButtonMainActivity.this,
                                "haven't open yet!");

                    else
                        Toast.makeText(SectorButtonMainActivity.this,
                                "haven't choose any item!", Toast.LENGTH_SHORT)
                                .show();
                }
                break;
        }

    }

}