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

import java.util.Timer;
import java.util.TimerTask;

import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

public class MainActivity extends AppCompatActivity implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    //BluetoothSerial
    private BluetoothSerial bluetoothSerial;

    Timer myTimer;

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
        //TextView
        bt_status = (TextView) findViewById(R.id.textView);
        //EditText
        edit_message = (EditText) findViewById(R.id.edit_message);

        bluetoothSerial = new BluetoothSerial(this, this);
        bluetoothSerial.setup();

        myTimer = new Timer();

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerCheck();
            }
        }, 0, 1000);

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
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        bluetoothSerial.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//            public void onDataReceived(byte[] data, String message) {
//                CharSequence text = message;
//                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
//                // Do something when data incoming
//                //probs throw into a toast or something
//            }
//        });
//
//        bluetoothSerial.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
//            public void onServiceStateChanged(int state) {
//                if(state == BluetoothState.STATE_CONNECTED) {
//                    // Do something when successfully connected
//                    Toast.makeText(getApplicationContext(), "Connected to device", Toast.LENGTH_SHORT).show();
//                } else if(state == BluetoothState.STATE_CONNECTING) {
//                    // Do something while connecting
//                    Toast.makeText(getApplicationContext(), "Connecting to device", Toast.LENGTH_SHORT).show();
//                } else if(state == BluetoothState.STATE_LISTEN) {
//                    // Do something when device is waiting for connection
//                    Toast.makeText(getApplicationContext(), "Listening to device", Toast.LENGTH_SHORT).show();
//                } else if(state == BluetoothState.STATE_NONE) {
//                    // Do something when device don't have any connection
//                    Toast.makeText(getApplicationContext(), "No State", Toast.LENGTH_SHORT).show();
//                }
//        }});

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

    //this might be a problem child down the road
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Toast.makeText(getApplicationContext(), "enter intent", Toast.LENGTH_SHORT).show();
//        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
//            if(resultCode == Activity.RESULT_OK) {
//                bluetoothSerial.connect(data);
//                Toast.makeText(getApplicationContext(), "connect", Toast.LENGTH_SHORT).show();
//            }
//            Toast.makeText(getApplicationContext(), "request connect", Toast.LENGTH_SHORT).show();
//        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
//            if(resultCode == Activity.RESULT_OK) {
//                bluetoothSerial.setupService();
//                bluetoothSerial.startService(BluetoothState.DEVICE_OTHER);
//                Toast.makeText(getApplicationContext(), "request enable", Toast.LENGTH_SHORT).show();
//            } else {
//                // Do something if user doesn't choose any device (Pressed back)
//                Toast.makeText(getApplicationContext(), "how did we get here", Toast.LENGTH_SHORT).show();
//            }
//        }
//        Toast.makeText(getApplicationContext(), "No device selected", Toast.LENGTH_SHORT).show();
//    }







    //
    // TIMER STUFF DOWN HERE
    //
    //things here are run in the timer thread
    private void TimerCheck(){
        //pass immediately to the gui thread
        this.runOnUiThread(Timer_Tick);
    }

    //things in here are run on the GUI thread
    private Runnable Timer_Tick = new Runnable(){
        public void run(){
            if(bluetoothSerial.isBluetoothEnabled()){
                bt_status.setText("BT: on");
                //bluetoothSerial.startService(BluetoothState.DEVICE_OTHER);
            } else {
                bt_status.setText("BT: off");
                //bluetoothSerial.stopService();    //I think this should avoid the crashing
            }
        }
    };


}
