package org.thewdj.blueserial;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.thewdj.blueserial.ble.BLEMainActivity;
import org.thewdj.blueserial.ble.BluetoothLeService;
import org.thewdj.blueserial.core.BlueSerial;

import java.security.Permission;
import java.util.Set;

public class ConnectActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mLeScanner;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private FloatingActionButton scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        setTitle("Devices List");

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        scanButton = (FloatingActionButton) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 0);
                }

                Snackbar.make(view, "Searching for device", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                doDiscovery();
            }
        });

        // Initialize array adapters. One for already paired devices
        // and one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.list_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mLeScanner = mBtAdapter.getBluetoothLeScanner();

        // Get a set of currently paired devices
        pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add("No devices found");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
            mLeScanner.stopScan(mLeScanCallback);
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        this.finish();
    }

    // Start device discover with the BluetoothAdapter
    private void doDiscovery() {
        // Remove all element from the list
        mPairedDevicesArrayAdapter.clear();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add("No devices found");
        }

        // Indicate scanning in the title
        scanButton.hide();

        // Turn on sub-title for new devices
        // findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            mLeScanner.stopScan(mLeScanCallback);
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
        mLeScanner.startScan(mLeScanCallback);
    }

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            if(mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
                mLeScanner.stopScan(mLeScanCallback);
            }

            if(!((TextView) view).getText().toString().equals("No devices found")) {
                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = parseAddress(info);
                String name = info.replace(address, "").replace("\n", "");

                // Connect the device
                if (info.contains(BluetoothLeService.BLE)) {
                    BLEMainActivity.doConnect(ConnectActivity.this, name, address);
                } else {
                    BlueSerial.instance.connect(address);
                }
                finish();
            }
        }
    };

    private String parseAddress(String info) {
        return info.substring(info.length() - 17);
    }

    private boolean verifyByInfo(String info) {
        String item;
        for (int i = 0; i < mPairedDevicesArrayAdapter.getCount(); i++) {
            item = mPairedDevicesArrayAdapter.getItem(i);
            if (item != null) {
                if (item.equals(info)) return true;
            }
        }
        return false;
    }

    private boolean verifyByAddress(String addr) {
        String item;
        for (int i = 0; i < mPairedDevicesArrayAdapter.getCount(); i++) {
            item = mPairedDevicesArrayAdapter.getItem(i);
            if (item != null) {
                if (parseAddress(item).equals(addr))
                    return true;
            }
        }
        return false;
    }

    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                BluetoothDevice device = result.getDevice();
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if(mPairedDevicesArrayAdapter.getItem(0).equals("No devices found")) {
                        mPairedDevicesArrayAdapter.remove("No devices found");
                    }
                    String dev = device.getName() + BluetoothLeService.BLE + "\n" + device.getAddress();
                    if (!verifyByInfo(dev))
                        mPairedDevicesArrayAdapter.add(dev);
                    if (verifyByInfo(dev.replace(BluetoothLeService.BLE, "")))
                        mPairedDevicesArrayAdapter.remove(dev.replace(BluetoothLeService.BLE, ""));
                }
            }
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if(mPairedDevicesArrayAdapter.getItem(0).equals("No devices found")) {
                        mPairedDevicesArrayAdapter.remove("No devices found");
                    }
                    String dev = device.getName() + "\n" + device.getAddress();
                    if (!verifyByInfo(dev) && !verifyByAddress(device.getAddress()))
                        mPairedDevicesArrayAdapter.add(dev);
                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Snackbar.make(findViewById(R.id.button_scan), "Select a device to connect", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            scanButton.show();
        }
    };

}
