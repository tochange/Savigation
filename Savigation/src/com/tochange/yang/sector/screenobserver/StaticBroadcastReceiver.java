package com.tochange.yang.sector.screenobserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.tochange.yang.sector.service.BaseFloatWindowService;
import com.tochange.yang.sector.service.FloatWindowService;
import com.tochange.yang.sector.tools.AppUtils;

public class StaticBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// screen unlock
		if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
			BaseFloatWindowService service = BaseFloatWindowService.instance;
			if (service != null) {
				SharedPreferences sp = service.getSharedPreferences(
						AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
				boolean isReopen = sp.getBoolean(AppUtils.PREFERENCES_ISREOPEN,
						true);
				if (!isReopen) {
					restartService(service);
				}
			}
		}
		// application uninstall
		else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			BaseFloatWindowService service = BaseFloatWindowService.instance;
			if (service != null) {
				String packagename = intent.getData().getSchemeSpecificPart();
				int size = service.mIntent.getIntExtra(AppUtils.KEY_SIZE, -1);
				int i = 0;
				for (; i < size; i++) {
					if (service.mIntent.getStringExtra(
							AppUtils.KEY_PACKAGENAME + i).equals(packagename))
						break;

				}
				if ((i < size)
						&& (storeImageAndPackageName(service, size, packagename)))
					restartService(service);
			}
		}
	}

	private void restartService(BaseFloatWindowService a) {
		Intent i = new Intent(a, FloatWindowService.class);
		i.putExtra(AppUtils.KEY_ISREOPEN, true);
		a.stopService(i);
		a.startService(i);
	}

	private boolean storeImageAndPackageName(BaseFloatWindowService a,
			int size, String packagename) {
		SharedPreferences sp = a.getSharedPreferences(
				AppUtils.PREFERENCES_FILENAME, Context.MODE_PRIVATE);
		int count = 0;
		Editor editor = sp.edit();
		for (int j = 0; j < size; j++) {
			String pn = a.mIntent.getStringExtra(AppUtils.KEY_PACKAGENAME + j);
			if (pn.equals(packagename))
				continue;
			editor.putString(AppUtils.KEY_PACKAGENAME + count, pn);
			String imageString = a.mIntent
					.getStringExtra(AppUtils.KEY_IMAGESTRING + j);
			if (imageString == null || imageString.equals(""))
				imageString = sp.getString(AppUtils.KEY_IMAGESTRING + j,
						"default imagestring1");
			editor.putString(AppUtils.KEY_IMAGESTRING + count, imageString);
			count++;
		}
		editor.putInt(AppUtils.KEY_SIZE, count);
		return editor.commit();
	}
}