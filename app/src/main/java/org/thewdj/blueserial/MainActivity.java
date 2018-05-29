package org.thewdj.blueserial;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import org.thewdj.blueserial.core.BlueSerial;


import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BlueSerial.setInstance(getApplicationContext());

        FloatingActionButton buttonConnect = (FloatingActionButton) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BlueSerial.instance.isBluetoothEnabled()) {
                    Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not enabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    if (BlueSerial.instance.isBluetoothAvailable()) {
                        if (BlueSerial.instance.getServiceState() == BluetoothState.STATE_CONNECTED)
                            BlueSerial.instance.disconnect();
                        if (!BlueSerial.instance.isServiceAvailable()){
                            BlueSerial.instance.setupService();
                            BlueSerial.instance.startService(BluetoothState.DEVICE_OTHER);
                        }
                        Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                        startActivity(intent);
                    } else {
                        Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not available", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
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

                if (BlueSerial.instance.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    output.append(">>> " + input.getText() + "\n");
                    if (radioString.isChecked()) {
                        BlueSerial.instance.send(input.getText().toString(), false);
                    } else if (radioHex.isChecked()) {
                        byte dat;
                        for (String i : input.getText().toString().split(" ")) {
                            if (i.startsWith("0x")) {
                                dat = Integer.valueOf(i.substring(2), 16).byteValue();
                                BlueSerial.instance.send(new byte[]{dat}, false);
                            } else if (i.startsWith("0b")) {
                                dat = Integer.valueOf(i.substring(2), 2).byteValue();
                                BlueSerial.instance.send(new byte[]{dat}, false);
                            } else {
                                for (char j : i.toCharArray()) {
                                    BlueSerial.instance.send(new byte[]{(byte) j}, false);
                                }
                            }
                        }
                    }
                }

                input.setText("");
            }
        });

        BlueSerial.instance.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // Do something when data incoming
                EditText output = (EditText) findViewById(R.id.receive_output);
                RadioButton radioString = (RadioButton) findViewById(R.id.type_string);
                RadioButton radioHex = (RadioButton) findViewById(R.id.type_hex);

                if (radioString.isChecked()) {
                    output.append(message + "\n");
                } else if (radioHex.isChecked()) {
                    for (byte i : data) {
                        output.append(Integer.toHexString((int) i) + " ");
                    }
                }
            }
        });

        BlueSerial.instance.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is successfully connected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                BlueSerial.connected = true;
            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                Snackbar.make(findViewById(R.id.button_connect), "Bluetooth connection was disconnected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                BlueSerial.connected = false;
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                Snackbar.make(findViewById(R.id.button_connect), "Bluetooth connection was failed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                BlueSerial.connected = false;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BlueSerial.instance.isBluetoothEnabled()) {
            Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not enabled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        } else if (!BlueSerial.instance.isBluetoothAvailable()) {
            Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not available", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if (!BlueSerial.instance.isServiceAvailable()){
            BlueSerial.instance.setupService();
            BlueSerial.instance.startService(BluetoothState.DEVICE_OTHER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BlueSerial.instance.stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_extra) {
            //if (BlueSerial.instance.getServiceState() == BluetoothState.STATE_CONNECTED) {
                Intent intent = new Intent(getApplicationContext(), ExtraActivity.class);
                startActivity(intent);
            //} else {
            //    Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not connected", Snackbar.LENGTH_LONG)
            //            .setAction("Action", null).show();
            //}
            return true;
        } else if (id == R.id.action_controller) {
            //if (BlueSerial.instance.getServiceState() == BluetoothState.STATE_CONNECTED) {
                Intent intent = new Intent(getApplicationContext(), ControllerActivity.class);
                startActivity(intent);
            //} else {
            //    Snackbar.make(findViewById(R.id.button_connect), "Bluetooth is not connected", Snackbar.LENGTH_LONG)
            //            .setAction("Action", null).show();
            //}
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

