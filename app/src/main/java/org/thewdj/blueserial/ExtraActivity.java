package org.thewdj.blueserial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.thewdj.blueserial.core.BlueSerial;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class ExtraActivity extends AppCompatActivity {

    private boolean isCmdOK = false;

    private void sendValue(char addr, int angle) {
        if (isCmdOK) BlueSerial.instance.send(addr + String.valueOf(angle), true);
        isCmdOK = false;
        RadioButton cmdResult = (RadioButton) findViewById(R.id.cmdResult);
        cmdResult.setChecked(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        RadioButton cmdResult = (RadioButton) findViewById(R.id.cmdResult);
        cmdResult.setChecked(false);

        BlueSerial.instance.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // Do something when data incoming
                if (message.contains("OK")) {
                    isCmdOK = true;
                    RadioButton cmdResult = (RadioButton) findViewById(R.id.cmdResult);
                    cmdResult.setChecked(true);
                } else {
                    isCmdOK = false;
                    RadioButton cmdResult = (RadioButton) findViewById(R.id.cmdResult);
                    cmdResult.setChecked(false);
                }
            }
        });

        BlueSerial.instance.send("M4V1", true);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBarA);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView text = (TextView) findViewById(R.id.valueA);
                text.setText(String.format("%d", i - 90));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendValue('A', seekBar.getProgress());
            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBarB);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView text = (TextView) findViewById(R.id.valueB);
                text.setText(String.format("%d", i - 90));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendValue('B', seekBar.getProgress());
            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBarC);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView text = (TextView) findViewById(R.id.valueC);
                text.setText(String.format("%d", i - 90));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendValue('C', seekBar.getProgress());
            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBarD);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView text = (TextView) findViewById(R.id.valueD);
                text.setText(String.format("%d", i - 90));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendValue('D', seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    }

}
