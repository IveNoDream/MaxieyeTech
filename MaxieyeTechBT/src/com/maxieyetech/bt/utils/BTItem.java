package com.maxieyetech.bt.utils;

/**
 * Created by root on 5/27/16.
 */
public class BTItem {
    private String mBTName =null;
    private String mBTAddress =null;
    private int mBTType =-1;

    public String getBTName() {
        return mBTName;
    }
    public void setBTName(String mBTName) {
        this.mBTName = mBTName;
    }
    public String getBTAddress() {
        return mBTAddress;
    }
    public void setBTAddress(String mBTAddress) {
        this.mBTAddress = mBTAddress;
    }
    public int getBTType() {
        return mBTType;
    }
    public void setBTType(int mBTType) {
        this.mBTType = mBTType;
    }
}
