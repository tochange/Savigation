package com.tochange.yang.sector.background;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.BackItemInfo;

public class ListToAdapter {
	private BackAdapter adapterb;

	private AppAdapter adapter;

	private Context c;

	private ListView appListView;

	private ListView backListView;

	private ArrayList<AppData> appList;

	private int handleCount;

	private ArrayList<BackItemInfo> backList;

	private Handler mUpdateAppListViewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AppData tmp = (AppData) msg.obj;
			appList.add(tmp);
			// 5 items notify together will lead to some item show without
			// notify,so if it was clicked,collapse:The content of the adapter
			// has changed but ListView did not receive a notification.
			// if (handleCount % 5 == 0)
			adapter.notifyDataSetChanged();
			handleCount++;
		}
	};

	public ListToAdapter(final Context c, ListView lv, ListView mListViewBack,
			final ArrayList<AppData> mCheckAppList,
			final ArrayList<BackItemInfo> mBackList) {
		this.c = c;
		appListView = lv;
		backListView = mListViewBack;
		appList = mCheckAppList;
		backList = mBackList;

		adapter = new AppAdapter(c, appList);
		appListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				appList.get(arg2).choosed = !appList.get(arg2).choosed;
				adapter.notifyDataSetChanged();
				// Utils.Toast(c,
				// "loading app data haven't finish,we will notify soon");
			}
		});
		appListView.setAdapter(adapter);

		loadAppData();
		loadBackData();

	}

	private void loadAppData() {
		AppData.getAppList(mUpdateAppListViewHandler, c, appList);
	}

	private void loadBackData() {
		if ((backListView != null))
			AppUtils.getBackPanelDataList(backList);
	}

	public void myNotify() {
		adapterb = new BackAdapter(c, backList);
		backListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				backList.get(arg2).choosed = !backList.get(arg2).choosed;
				adapterb.notifyDataSetChanged();
			}
		});
		backListView.setAdapter(adapterb);
		// short time,no need to do so
		// adapterb.notifyDataSetChanged();
	}

}