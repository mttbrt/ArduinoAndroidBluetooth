package com.dev.methk.arduinoandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Actions extends AppCompatActivity {

    // Widgets
    public Button btnSendData, btnReceiveData;
    public String address = null;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newInt = getIntent();
        address = newInt.getStringExtra(DeviceList.EXTRA_ADDRESS); // Get the MAC address

        setContentView(R.layout.activity_actions);

        btnSendData = (Button)findViewById(R.id.BTN_sendData);
        btnReceiveData = (Button)findViewById(R.id.BTN_receiveData);

        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Actions.this, PinControl.class);

                i.putExtra(EXTRA_ADDRESS, address); // This will be received at PinControl (class) Activity
                startActivity(i);
            }
        });

        btnReceiveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Actions.this, Receiver.class);

                i.putExtra(EXTRA_ADDRESS, address); // This will be received at Receiver (class) Activity
                startActivity(i);
            }
        });

    }

}
