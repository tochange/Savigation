package com.tochange.yang.sector;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.tochange.yang.R;
import com.tochange.yang.lib.Utils;
import com.tochange.yang.lib.log;
import com.tochange.yang.sector.ScreenObserver.ScreenStateListener;
import com.tochange.yang.sector.tools.AppUtils;
import com.tochange.yang.sector.tools.BackItemInfo;
import com.tochange.yang.sector.tools.BackPanelBin;
import com.tochange.yang.view.ChildrenInterface;

public class FloatWindowService extends BaseFloatWindowService {
	private class MyOnGestureListener extends SimpleOnGestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			// mSectorButton.setSticky(!mIsSticky);
			// stickBorder();
			// mIsSticky = !mIsSticky;
			mVibrator.vibrate(new long[] { 50, 50, 0, 0 }, -1);
			mCanMove = !mCanMove;
			int res = mCanMove ? R.drawable.composer_father_l
					: R.drawable.composer_father_ll;
			mFatherItem.setBackgroundResource(res);
			// ensure change the background
			mSectorButton.invalidate();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			mIsMoving = true;
			if (mCanMove) {
				mLayoutParams.x = (int) e2.getRawX()
						- mFatherItem.getMeasuredWidth() / 2;
				mLayoutParams.y = (int) e2.getRawY()
						- mFatherItem.getMeasuredHeight() / 2
						- Utils.getStatusBarHeight(FloatWindowService.this);

				// log.e("mLayoutParams.y=" + mLayoutParams.y
				// + "  mLayoutParams.x=" + mLayoutParams.x);

				// if (mFatherItem.getIsOpen())
				// mLayoutParams.y -= mEvilMarginTop;
				mWindowManager.updateViewLayout(mFloatLayout, mLayoutParams);
			}
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			doSomethingOnScreenOff();
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			mSectorButton.doFatherListener();
			return false;
		}

	}

	@Override
	public void stickBorder() {
		log.e("begin bad");

		if (mScreanW == 0 && mScreanH == 0) {
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
		if (bottom < top && bottom < left && bottom < right) {
			isXcoordinate = false;
			tovalue = mScreanH;
			// top
		} else if (bottom > top && top < left && top < right) {
			isXcoordinate = false;
			tovalue = 0;
			// left
		} else if (left < top && right > left && bottom > left) {
			isXcoordinate = true;
			tovalue = 0;
			// right
		} else if (right < top && right < left && bottom > right) {
			isXcoordinate = true;
			tovalue = mScreanW;
		}

		new StickyUpdateTask(isXcoordinate, tovalue).execute();
	}

	private class StickyUpdateTask extends AsyncTask {
		int tovalue;

		boolean isXcoordinate;

		public StickyUpdateTask(boolean isXcoordinate, int toValue) {
			this.tovalue = toValue;
			this.isXcoordinate = isXcoordinate;
		}

		void slowDown(boolean isXcoordinate, int toValue)

		{
			if (isXcoordinate) {
				if (toValue == 0) {
					for (; mLayoutParams.x > 0 && !mStickyHasReset; Utils
							.sleep(mSleepTime)) {
						// log.e("x 0 =" + mLayoutParams.x);
						mLayoutParams.x -= STICKY_OFFSET;
						publishProgress(mLayoutParams);
					}
				} else if (toValue == mScreanW) {
					for (; mLayoutParams.x < toValue && !mStickyHasReset; Utils
							.sleep(mSleepTime)) {
						// log.e("x -0 =" + mLayoutParams.x);
						mLayoutParams.x += STICKY_OFFSET;
						publishProgress(mLayoutParams);
					}
				}

			} else {
				if (toValue == 0) {
					for (; mLayoutParams.y > 0 && !mStickyHasReset; Utils
							.sleep(mSleepTime)) {
						// log.e("y 0 =" + mLayoutParams.y);
						mLayoutParams.y -= STICKY_OFFSET;
						publishProgress(mLayoutParams);
					}
				} else if (toValue == mScreanH) {
					for (; mLayoutParams.y < toValue && !mStickyHasReset; Utils
							.sleep(mSleepTime)) {
						// log.e("y -0 =" + mLayoutParams.y);
						mLayoutParams.y += STICKY_OFFSET;
						publishProgress(mLayoutParams);
					}
				}
			}
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			// log.e("onProgressUpdate=" );
			// if (FloatWindowService.this.isRestricted())
			if (!mAlreadyDestory)
				mWindowManager.updateViewLayout(mFloatLayout, mLayoutParams);
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			{
				if (mSectorButton.getSticky()) {
					log.e("bbbbbbbbbbbbbbad");
					mSectorButton.inScaleFather();
					mSectorButton.invalidate();
				}
			}
		}

		@Override
		protected Object doInBackground(Object... params) {
			slowDown(isXcoordinate, tovalue);
			return null;
		}

	}

	@Override
	public GestureDetector getGestureDetector() {
		return new GestureDetector(this, new MyOnGestureListener());
	}

	@Override
	public ChildrenInterface getChildrenLinster() {
		return new ChildrenInterface() {
			@Override
			public void didSelectedItem(boolean isBackPanel, int index) {
				// sometimes doesn't work
				// openApp(getPackageName(index));
				if (isBackPanel) {
					try {
						BackItemInfo tmp = mChoosedBackClildList.get(index);
						mBackPanelBin.doit(tmp.value);
					} catch (SettingNotFoundException e) {
						log.e(e.toString());
						e.printStackTrace();
					}
				} else
					new LauncherAppTask().execute("" + index);
			}

			@Override
			public void updateBackChildList() {
				int size = mChoosedBackClildList.size();
				for (int i = 0; i < size; i++) {
					BackItemInfo tmp = mChoosedBackClildList.get(i);
					int res = BackPanelBin.isAble(tmp.value) ? tmp.iconResOn
							: tmp.iconResOff;
					mChoosedBackClildItemList.get(i).setBackgroundDrawable(
							getResources().getDrawable(res));
					mSectorButton
							.updateBackPanelChild(mChoosedBackClildItemList);
				}
			}

		};
	}

	private class LauncherAppTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {
			Utils.openAppTroublesome(FloatWindowService.this,
					getPackageName(Integer.parseInt(arg0[0])));
			return null;
		}

	}

	private String getPackageName(int index) {
		String ret;
		if (null != mIntent)
			ret = mIntent.getStringExtra(AppUtils.KEY_PACKAGENAME + index);
		else
			ret = mSharedPreferences.getString(
					AppUtils.KEY_PACKAGENAME + index, "default packagename");
		return ret;
	}

	public void sendNotification(int type, int id, String s) {
		switch (type) {
		case CLEAR_NOTIFICATION:
			mNotificationManager.cancelAll();
			break;

		case SEND_NOTIFICATION:
			Notification notification = new Notification();
			notification.flags = Notification.FLAG_AUTO_CANCEL
					| Notification.FLAG_ONGOING_EVENT;
			RemoteViews contentView = new RemoteViews(
					"com.tochange.yang.sector", R.layout.notification_item);
			log.e("ttttttt=" + (mIntent == null));

			{
				// mIntent = null;
				// mIntent = new Intent(this, FloatWindowService.class);
				saveCurrentPosition();
				log.e("get.........");
				// int size = mSharedPreferences.getInt(AppUtils.KEY_SIZE,
				// 6);
				// mIntent.putExtra(AppUtils.KEY_SIZE, size);
				// for (int i = 0; i < size; i++)
				// {
				// String imageString = mSharedPreferences.getString(
				// AppUtils.KEY_IMAGESTRING + i,
				// "default imagestring");
				// mIntent.putExtra(AppUtils.KEY_IMAGESTRING + i,
				// imageString);
				// String packageName = mSharedPreferences.getString(
				// AppUtils.KEY_PACKAGENAME + i,
				// "default packgename");
				// mIntent.putExtra(AppUtils.KEY_PACKAGENAME + i,
				// packageName);
				// }

			}
			mIntent.putExtra(AppUtils.KEY_ISREOPEN, true);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pd = PendingIntent.getService(this, 0, mIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), "title..",
					"content..", pd);
			notification.icon = R.drawable.composer_father_s;
			notification.tickerText = s;
			contentView.setImageViewResource(R.id.image,
					R.drawable.composer_father_l);
			contentView.setTextViewText(R.id.text, "Click me to reopen..   ");
			Time t = new Time();
			t.setToNow();
			String time = t.hour + ":" + t.minute;
			contentView.setTextViewText(R.id.time, time);
			notification.contentView = contentView;
			mNotificationManager.notify(3, notification);

			this.stopService(mIntent);
			break;
		}

	}

	@Override
	public void setLayoutParamsWidthAndHight(LayoutParams layouparameter) {

		if (null != mIntent
				&& !mIntent.getExtras()
						.getBoolean(AppUtils.KEY_ISREOPEN, false)) {
			mScreanW = mIntent.getIntExtra(AppUtils.KEY_SCREEN_W,
					DEFAULT_DISPLAY_WIDTH);
			mScreanH = mIntent.getIntExtra(AppUtils.KEY_SCREEN_H,
					DEFAULT_DISPLAY_HIGHT);

			layouparameter.x = (int) (1.0 / 6 * mScreanW);
			layouparameter.y = (int) (1.0 / 4 * mScreanH);

		} else {
			int x = mSharedPreferences.getInt(
					AppUtils.PREFERENCESNAME_POSITION_X, DEFAULT_DISPLAY_WIDTH);

			int y = mSharedPreferences.getInt(
					AppUtils.PREFERENCESNAME_POSITION_Y, DEFAULT_DISPLAY_HIGHT);

			layouparameter.x = x;
			layouparameter.y = y;
			// log.e("......" + layouparameter.x);
			// log.e("......" + layouparameter.y);
		}
	}

	private void savePosition() {
		Editor editor = mSharedPreferences.edit();
		editor.putInt(AppUtils.PREFERENCESNAME_POSITION_X,
				DEFAULT_DISPLAY_WIDTH);
		editor.putInt(AppUtils.PREFERENCESNAME_POSITION_Y,
				DEFAULT_DISPLAY_HIGHT);
		editor.commit();
	}

	private void beforDestory() {
		if (mFloatLayout != null) {
			mWindowManager.removeView(mFloatLayout);
			mCanNew = true;
		}
		// maybe wrong
		// savePosition();
		saveCurrentPosition();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAlreadyDestory = true;
		mBackPanelBin.detach();
		mCanReStartShake = false;
		log.e("onDestory");
		mScreenObserver.stopScreenStateUpdate();
		beforDestory();
		// sendNotification(CLEAR_NOTIFICATION, -1, null);
	}

	@Override
	public void initEnvironment() {// a little arbitrary,when new data comes
									// just abandon old data
		sendNotification(CLEAR_NOTIFICATION, -1, null);
		beginScreenObserver();
	}

	private void beginScreenObserver() {
		// TODO Auto-generated method stub
		mScreenObserver = new ScreenObserver(this);
		mScreenObserver.requestScreenStateUpdate(new ScreenStateListener() {
			@Override
			public void onScreenOn() {
				doSomethingOnScreenOn();
			}

			@Override
			public void onScreenOff() {
				doSomethingOnScreenOff();
			}

			@Override
			public void onUserPresent() {
				doSomethingOnUserPresent();
			}
		});
	}

	private void doSomethingOnScreenOn() {
		log.e("Screen is on");
	}

	private void doSomethingOnScreenOff() {
		log.e("Screen is off");
		mCanReStartShake = false;
		mShakeListener.stop();
		sendNotification(SEND_NOTIFICATION, 2, "i'am hiding here..  >.<");
	}

	private void doSomethingOnUserPresent() {
		log.e("UserPresent");
		mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.startService(mIntent);
	}

	@Override
	public Intent getIntent(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
