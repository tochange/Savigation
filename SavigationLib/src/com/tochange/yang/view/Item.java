package com.tochange.yang.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class Item extends ImageButton {
	private Position mStartPosition;

	private Position mEndPosition;

	private Position mNearPosition;

	private Position mFarPosition;

	private int mViewHeight;

	private boolean mIsOpen;

	public Item(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
		Drawable drawable = getBackground();
		mViewHeight = drawable.getIntrinsicHeight();
	}

	public void setOpen(boolean isopen) {
		mIsOpen = isopen;
	}

	public boolean getIsOpen() {
		return mIsOpen;
	}

	public Position getStartPosition() {
		return mStartPosition;
	}

	public void setStartPosition(Position startPosition) {
		mStartPosition = startPosition;
	}

	public Position getEndPosition() {
		return mEndPosition;
	}

	public void setEndPosition(Position endPosition) {
		mEndPosition = endPosition;
	}

	public Position getNearPosition() {
		return mNearPosition;
	}

	public void setNearPosition(Position nearPosition) {
		mNearPosition = nearPosition;
	}

	public Position getFarPosition() {
		return mFarPosition;
	}

	public void setFarPosition(Position farPosition) {
		mFarPosition = farPosition;
	}

	public int getViewHeight() {
		return mViewHeight;
	}

}
