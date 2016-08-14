package org.thewdj.blueserial.core;

import android.content.Context;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

/**
 * Created by drzzm on 2016.8.8.
 */
public class BlueSerial {
    public static BluetoothSPP instance;
    public static String addrBuf;

    public static void setInstance(Context context) {
        instance = new BluetoothSPP(context);
    }

}
