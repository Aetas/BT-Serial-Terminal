package com.example.aetas.furstapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends AppCompatActivity {

    BluetoothSPP bt;
    private Timer myTimer;

    //these have to be outside of onCreate to be avail. in methods and the timer
    //in the future, the timer might not need access to these
    private Button red_button;
    private Button blue_button;
    private Button green_button;
    private Button bt_connect;
    private Button button_send;
    private EditText edit_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        red_button = (Button) findViewById(R.id.red_button);
        blue_button = (Button) findViewById(R.id.blue_button);
        green_button = (Button) findViewById(R.id.green_button);
        bt_connect = (Button) findViewById(R.id.bt_connect);
        button_send = (Button) findViewById(R.id.button_send);

        edit_message = (EditText) findViewById(R.id.edit_message);

        bt = new BluetoothSPP(getApplicationContext());
        myTimer = new Timer();

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerCheck();
            }
        }, 0, 1000);

        bt_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });

        //Real ghetto that is.
        red_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED){
                    bt.send("red", true);   //return line is appended because the uController is looking for them to do a match check
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        blue_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED){
                    bt.send("blue", true);
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        green_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED){
                    bt.send("green", true);
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED){
                    bt.send(edit_message.getText().toString(), true);   //grabs the text in the text box and casts to string
                } else {
                    Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // Do something when data incoming
                //probs throw into a toast or something
            }
        });

        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if(state == BluetoothState.STATE_CONNECTED) {
                    // Do something when successfully connected
                    Toast.makeText(getApplicationContext(), "Connected to device", Toast.LENGTH_SHORT).show();
                } else if(state == BluetoothState.STATE_CONNECTING) {
                    // Do something while connecting
                    Toast.makeText(getApplicationContext(), "Connecting to device", Toast.LENGTH_SHORT).show();
                } else if(state == BluetoothState.STATE_LISTEN) {
                    // Do something when device is waiting for connection
                    Toast.makeText(getApplicationContext(), "Listening to device", Toast.LENGTH_SHORT).show();
                } else if(state == BluetoothState.STATE_NONE) {
                    // Do something when device don't have any connection
                    Toast.makeText(getApplicationContext(), "No State", Toast.LENGTH_SHORT).show();
                }
        }});

    }

    //this might be a problem child down the road
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                // Do something if user doesn't choose any device (Pressed back)
                Toast.makeText(getApplicationContext(), "No device selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //things here are run in the timer thread
    private void TimerCheck(){
        //pass immediately to the gui thread
        this.runOnUiThread(Timer_Tick);
    }
    //things in here are run on the GUI thread
    private Runnable Timer_Tick = new Runnable(){
        public void run(){
            if(bt.isBluetoothEnabled()){
                red_button.setText("Bluetooth is enabled");
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                red_button.setText("Well Shit");
                bt.stopService();    //I think this should avoid the crashing
            }
        }
    };
}
