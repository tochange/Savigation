package com.tochange.yang.sector;

import android.content.Intent;
import android.view.GestureDetector;
import android.view.WindowManager.LayoutParams;

import com.tochange.yang.view.ChildrenInterface;

public interface FloatWindowServiceInterface
{
    void setLayoutParamsWidthAndHight(LayoutParams layouparameter);

    void initEnvironment();
    
    Intent getIntent(Intent intent);

    GestureDetector getGestureDetector();

    ChildrenInterface getChildrenLinster();

    void stickBorder();
}
