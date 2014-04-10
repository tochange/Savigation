package com.tochange.yang.sector.tools;

public class BackItemInfo
{
    public String name;

    public int value;

    public boolean choosed;

    public int iconResOn;

    public int iconResOff;

    BackItemInfo(String n, int v, boolean co, int on, int off)
    {
        name = n;
        value = v;
        choosed = co;
        iconResOn = on;
        iconResOff = off;
    }

}