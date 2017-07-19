package com.example.aetas.furstapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

public class MainActivity extends AppCompatActivity implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    //BluetoothSerial
    private BluetoothSerial bluetoothSerial;

    //these have to be outside of onCreate to be avail. in methods and the timer
    //in the future, the timer might not need access to these
    private Button red_button;
    private Button blue_button;
    private Button green_button;
    private Button bt_connect;
    private Button button_send;
    private EditText edit_message;

    private TextView bt_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buttons
        red_button = (Button) findViewById(R.id.red_button);
        blue_button = (Button) findViewById(R.id.blue_button);
        green_button = (Button) findViewById(R.id.green_button);
        bt_connect = (Button) findViewById(R.id.bt_connect);
        button_send = (Button) findViewById(R.id.button_send);
        //EditText
        edit_message = (EditText) findViewById(R.id.edit_message);

        bluetoothSerial = new BluetoothSerial(this, this);
        bluetoothSerial.setup();

        bt_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                showDeviceListDialog();
            }
        });

        //Real ghetto that is.
        red_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bluetoothSerial.getState() == BluetoothSerial.STATE_CONNECTED){
                    bluetoothSerial.write("red", true);   //return line is appended because the uController is looking for them to do a match check
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        blue_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bluetoothSerial.getState() == BluetoothSerial.STATE_CONNECTED){
                    bluetoothSerial.write("blue", true);
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        green_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bluetoothSerial.getState() == BluetoothSerial.STATE_CONNECTED){
                    bluetoothSerial.write("green", true);
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bluetoothSerial.getState() == BluetoothSerial.STATE_CONNECTED){
                    bluetoothSerial.write(edit_message.getText().toString(), true);   //grabs the text in the text box and casts to string
                    edit_message.setText("");   //clear text box
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle("paired devices");
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        bluetoothSerial.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = "Connecting";
                break;
            case BluetoothSerial.STATE_CONNECTED:
                subtitle = bluetoothSerial.getConnectedDeviceName();
                break;
            default:
                subtitle = "Disconnected";
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public void onBluetoothNotSupported() {
        Log.v("MyAPP", "Drat! Bluetooth not supported!");
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onBluetoothSerialRead(String message) {
        Log.v("MyAPP", "got message: " + message);
    }

    @Override
    public void onBluetoothSerialWrite(String message) {
        Log.v("MyAPP", "sent message: " + message);
    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

}
