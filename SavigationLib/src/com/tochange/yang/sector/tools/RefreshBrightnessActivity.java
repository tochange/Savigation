package com.tochange.yang.sector.tools;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;

public class RefreshBrightnessActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int b = getIntent().getIntExtra("brightness", 30);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = b / 255f;
		getWindow().setAttributes(lp);
		new DelayTask().execute();
	}

	class DelayTask extends AsyncTask {
		protected Object doInBackground(Object... params) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			finish();
		}

	}

}