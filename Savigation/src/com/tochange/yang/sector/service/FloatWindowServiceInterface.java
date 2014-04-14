package com.tochange.yang.sector.service;

import android.view.GestureDetector;
import android.view.WindowManager.LayoutParams;

import com.tochange.yang.view.ChildrenInterface;

public interface FloatWindowServiceInterface
{
    void setLayoutParamsWidthAndHight(LayoutParams layouparameter);

    void initEnvironment();
    
    GestureDetector getGestureDetector();

    ChildrenInterface getChildrenLinster();

    void stickBorder();
}
