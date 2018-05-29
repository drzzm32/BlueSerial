/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thewdj.blueserial.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.EditText;


import org.thewdj.blueserial.ConnectActivity;
import org.thewdj.blueserial.R;

import java.util.ArrayList;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class BLEMainActivity extends AppCompatActivity {
    private final static String TAG = BLEMainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private BluetoothAdapter mBtAdapter;

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBLEService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private boolean mConnected = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBLEService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
        }
    };

    public static void doConnect(Activity activity, String name, String addr) {
        final Intent intent = new Intent(activity, BLEMainActivity.class);
        intent.putExtra(EXTRAS_DEVICE_NAME, name);
        intent.putExtra(EXTRAS_DEVICE_ADDRESS, addr);
        activity.startActivity(intent);
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is successfully connected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Snackbar.make(findViewById(R.id.button_connect), "Bluetooth connection was disconnected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBLEService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);

                EditText output = (EditText) findViewById(R.id.receive_output);
                RadioButton radioString = (RadioButton) findViewById(R.id.type_string);
                RadioButton radioHex = (RadioButton) findViewById(R.id.type_hex);
                try {
                    if (radioString.isChecked()) {
                        output.append(data.split("\n")[0] + "\n");
                    } else if (radioHex.isChecked()) {
                        output.append(data.split("\n")[1] + "\n");
                    }
                } catch (Exception e) {
                    output.append("[RX ERR]" + "\n");
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        FloatingActionButton buttonConnect = (FloatingActionButton) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBtAdapter.isEnabled()) {
                    Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not enabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    if (mConnected) {
                        mBLEService.disconnect();
                        mConnected = false;
                    }

                    Intent intent = new Intent(BLEMainActivity.this, ConnectActivity.class);
                    startActivity(intent);
                }
            }
        });

        RadioButton radioButton = (RadioButton) findViewById(R.id.type_string);
        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                RadioButton buttonString = (RadioButton) findViewById(R.id.type_string);
                RadioButton buttonHex = (RadioButton) findViewById(R.id.type_hex);
                buttonHex.setChecked(!buttonString.isChecked());
            }
        });
        radioButton = (RadioButton) findViewById(R.id.type_hex);
        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                RadioButton buttonString = (RadioButton) findViewById(R.id.type_string);
                RadioButton buttonHex = (RadioButton) findViewById(R.id.type_hex);
                buttonString.setChecked(!buttonHex.isChecked());
            }
        });

        Button buttonSend = (Button) findViewById(R.id.button_transmit_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.transmit_input);
                EditText output = (EditText) findViewById(R.id.receive_output);
                RadioButton radioString = (RadioButton) findViewById(R.id.type_string);
                RadioButton radioHex = (RadioButton) findViewById(R.id.type_hex);

                if (mConnected) {
                    output.append(">>> " + input.getText() + "\n");
                    if (radioString.isChecked()) {
                        mBLEService.write(input.getText().toString().getBytes());
                    } else if (radioHex.isChecked()) {
                        byte dat;
                        for (String i : input.getText().toString().split(" ")) {
                            if (i.startsWith("0x")) {
                                dat = Integer.valueOf(i.substring(2), 16).byteValue();
                                mBLEService.write(new byte[]{ dat });
                            } else if (i.startsWith("0b")) {
                                dat = Integer.valueOf(i.substring(2), 2).byteValue();
                                mBLEService.write(new byte[]{ dat });
                            } else {
                                dat = Integer.valueOf(i.substring(0, 2), 16).byteValue();
                                mBLEService.write(new byte[]{ dat });
                            }
                        }
                    }
                }

                input.setText("");
            }
        });

        Button buttonClear = (Button) findViewById(R.id.button_receive_cls);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText output = (EditText) findViewById(R.id.receive_output);
                output.setText("");
            }
        });

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLEService != null) {
            final boolean result = mBLEService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBLEService = null;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        if(gattServices.size() > 0 && mBLEService.getConnectedStatus(gattServices) >= 4) {
            if(mConnected) {
                mBLEService.enableBLESerial();
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBLEService.enableBLESerial();
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
