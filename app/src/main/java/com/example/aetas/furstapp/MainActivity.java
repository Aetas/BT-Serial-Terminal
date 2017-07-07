package com.example.aetas.furstapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


//https://developer.android.com/training/basics/firstapp/starting-activity.html

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BluetoothSPP(getApplicationContext());

        final Button red_button = (Button) findViewById(R.id.red_button);
        final Button blue_button = (Button) findViewById(R.id.blue_button);
        final Button green_button = (Button) findViewById(R.id.green_button);
        final Button bt_connect = (Button) findViewById(R.id.bt_connect);

        bt_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });

        //manage error handling <-----
        //probably just make a real terminal over the weekend.
        //Real ghetto that is.
        red_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

            }
        });

        blue_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

            }
        });
        green_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // Do something when data incoming
            }
        });

        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if(state == BluetoothState.STATE_CONNECTED) {
                    // Do something when successfully connected
                } else if(state == BluetoothState.STATE_CONNECTING) {
                    // Do something while connecting
                } else if(state == BluetoothState.STATE_LISTEN) {
                    // Do something when device is waiting for connection
                } else if(state == BluetoothState.STATE_NONE) {
                    // Do something when device don't have any connection
            }
        }});

        if(bt.isBluetoothEnabled()){
            green_button.setText("Bluetooth is enabled");
            bt.startService(BluetoothState.DEVICE_OTHER);
        } else {
            green_button.setText("well shit");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                //setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
