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

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.EditText;


import org.thewdj.blueserial.ConnectActivity;
import org.thewdj.blueserial.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.ac.nya.ppgcalc.Breath;
import cn.ac.nya.ppgcalc.PPGData;
import cn.ac.nya.ppgcalc.SPO2;
import cn.ac.nya.ppgcalc.launcher.Util;
import cn.ac.nya.ppgcalc.math.Transform;

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

    private boolean mPPGCtrl = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private class PPGDataFIFO {

        private String buffer;

        public PPGDataFIFO() {
            buffer = "";
        }

        public void append(String str) {
            str = str.replace("OK", "");
            buffer = buffer.concat(str);
        }

        public String get() {
            if (buffer.isEmpty())
                return "";
            if (buffer.split(";").length == 0)
                return "";

            String res = buffer.split(";")[0];
            if (res.isEmpty())
                return "";

            if (buffer.length() > res.length() + 1)
                buffer = buffer.substring(res.length() + 1);
            else
                buffer = "";

            if (res.charAt(0) == 'P' && res.split(",").length == 2)
                return res + ";";

            return "";
        }

    }

    private PPGDataFIFO ppgDataFIFO = new PPGDataFIFO();
    private PPGData ppgData = new PPGData(1024);

    private class CalcUtil {

        private Transform transform = new Transform(1024, 25.0);
        private SPO2 spo2 = new SPO2(0.000454, 0.983560);
        private Breath breath;

        public double calcHeartRate(PPGData ppgData) {
            if (ppgData == null) return 0;
            return transform.output(transform.fft(ppgData.getRed()), 0.5, 3.0).f * 60;
        }

        public double calcBreathRate(PPGData ppgData) {
            if (ppgData == null) return 0;
            breath = new Breath(ppgData.getRedInt());
            return transform.output(transform.fft(breath.makeHs()), 0.2, 0.5).f * 60;
        }

        public double calcSpO2(PPGData data) {
            return spo2.calcSpO2(data) * 100;
        }

    }

    private CalcUtil calcUtil = new CalcUtil();

    private class DataPacker {

    /*	Total: 13 bytes

	0							0xFF
	1							0xFE
	2	uint8_t hour;			byte (0x00 ~ 0x17)
	3	uint8_t minute;			byte (0x00 ~ 0x3B)

	4	uint8_t heart;			byte (0x00 ~ 0xFF)
	5	float SpO2;				byte (0x00 ~ 0xFF) <- (int) float_data
	6							byte (0x00 ~ 0xFF) <- (int) ((float_data - ((int) float_data)) * 100)
	7	uint8_t breath;			byte (0x00 ~ 0xFF)

	8	uint8_t phone;			byte (0x00 ~ 0xFF)
	9	uint8_t message;		byte (0x00 ~ 0xFF)

	10	uint8_t weather;		byte (0x00 ~ 0x05)
			WEATHER_SUNNY	0
			WEATHER_CLOUDY	1
			WEATHER_FOG		2
			WEATHER_PCLOUDY	3
			WEATHER_RAINY	4
			WEATHER_WINDY	5

	11	uint8_t control;		byte (0x00: stop, 0x01: start)

	12		check sum			byte (0x00 ~ 0xFF) <- sum(0 : 11)

	*/

        private byte[] bytes = new byte[13];

        private byte b(int i) { return (byte) i; }

        public DataPacker() {
            bytes[0] = b(0xFF); bytes[1] = b(0xFE);
        }

        public void setHour(int hour) { bytes[2] = b(hour) > 0x17 ? 0x17 : b(hour); }
        public void setMinute(int minute) { bytes[3] = b(minute) > 0x3B ? 0x3B : b(minute); }
        public void setHeart(double heart) { bytes[4] = b((int) heart); }
        public void setSpO2(double SpO2) { bytes[5] = b((int) SpO2); bytes[6] = b((int) ((SpO2 - ((int) SpO2)) * 100)); }
        public void setBreath(double breath) { bytes[7] = b((int) breath); }
        public void setPhone(int phone) { bytes[8] = b(phone); }
        public void setMessage(int message) { bytes[9] = b(message); }
        public void setWeather(int weather) { bytes[10] = b(weather); }
        public void setCtrl(int ctrl) { bytes[11] = b(ctrl); }

        public byte[] make() {
            byte sum = 0;
            for (int i = 0; i < bytes.length - 1; i++)
                sum += bytes[i];
            bytes[12] = sum;
            return bytes;
        }

    }

    private DataPacker dataPacker = new DataPacker();
    private double heart, breath, spo2;
    private double progress = 0;

    private int getMissedCall() {
        int result = 0;

        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] { Manifest.permission.READ_CALL_LOG }, 0);
        } else {
            Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {
                    CallLog.Calls.TYPE
            }, " type=? and new=?", new String[] {
                    CallLog.Calls.MISSED_TYPE + "", "1"
            }, "date desc");

            if (cursor != null) {
                result = cursor.getCount();
                cursor.close();
            }
        }

        return result;
    }

    private int getSmsCount() {
        int result = 0;

        if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] { Manifest.permission.READ_SMS }, 0);
        } else {
            Cursor csr = getContentResolver().query(Uri.parse("content://sms"), null,
                    "type = 1 and read = 0", null, null);
            if (csr != null) {
                result = csr.getCount();
                csr.close();
            }

            csr = getContentResolver().query(Uri.parse("content://mms/inbox"),
                    null, "read = 0", null, null);
            if (csr != null) {
                result += csr.getCount();
                csr.close();
            }
        }

        return result;
    }

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (!mPPGCtrl) return;

            String buf;

            if (ppgData.len() == 1024) {
                buf = ppgDataFIFO.get();
                if (!buf.isEmpty())
                    Util.fromString(ppgData, buf);
            } else while (ppgData.len() < 1024) {
                buf = ppgDataFIFO.get();
                if (buf.isEmpty()) continue;
                if (ppgDataFIFO.buffer.isEmpty()) break;
                Util.fromString(ppgData, buf);
            }

            EditText output = (EditText) findViewById(R.id.receive_output);
            if (ppgData.len() == 1024) {
                heart = calcUtil.calcHeartRate(ppgData);
                breath = calcUtil.calcBreathRate(ppgData);
                spo2 = calcUtil.calcSpO2(ppgData);

                output.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText output = (EditText) findViewById(R.id.receive_output);
                        output.setText(String.format(Locale.CHINA, "Heart: %1.2f\n", heart));
                        output.append(String.format(Locale.CHINA, "Heart: %1.2f\n", breath));
                        output.append(String.format(Locale.CHINA, "Heart: %1.2f\n", spo2));
                        output.append("\n");
                    }
                });

                Date date = new Date(System.currentTimeMillis());
                int hour = date.getHours();
                int minute = date.getMinutes();

                int phone = getMissedCall();
                int message = getSmsCount();

                int weather = 0x00;

                dataPacker.setHeart(heart); dataPacker.setBreath(breath); dataPacker.setSpO2(spo2);
                dataPacker.setHour(hour); dataPacker.setMinute(minute);
                dataPacker.setPhone(phone); dataPacker.setMessage(message);
                dataPacker.setWeather(weather);
                dataPacker.setCtrl(0x81);

                byte[] data = dataPacker.make();
                for (int i = 0; i < 16; i++) {
                    if (mBLEService != null)
                        mBLEService.write(data);
                }
            } else {
                output.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText output = (EditText) findViewById(R.id.receive_output);
                        progress += 0.01;
                        output.setText(String.format(Locale.CHINA, "Progress: %1.2f\n", progress));
                    }
                });
            }
        }
    };

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
                    if (mPPGCtrl) {
                        ppgDataFIFO.append(data.split("\n")[0]);
                    } else {
                        if (radioString.isChecked()) {
                            output.append(data.split("\n")[0] + "\n");
                        } else if (radioHex.isChecked()) {
                            output.append(data.split("\n")[1] + "\n");
                        }
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
        setContentView(R.layout.activity_ble);
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

        SwitchCompat switchPPG = (SwitchCompat) findViewById(R.id.switch_ppg);
        switchPPG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPPGCtrl = b;
                progress = 0;
            }
        });
        timer.schedule(timerTask, 1000, 1000);

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
