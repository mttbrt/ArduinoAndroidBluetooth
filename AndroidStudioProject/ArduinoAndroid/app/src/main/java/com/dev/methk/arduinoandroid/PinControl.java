package com.dev.methk.arduinoandroid;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class PinControl extends AppCompatActivity {

    // Widgets
    Button btnOn, btnOff, btnDis;
    SeekBar powerBar;
    TextView pwrTW;

    // Bluetooth
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBTConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newInt = getIntent();
        address = newInt.getStringExtra(DeviceList.EXTRA_ADDRESS); // MAC address of the chosen device

        setContentView(R.layout.activity_pin_control);

        // Initialize widgets
        btnOn = (Button)findViewById(R.id.on_btn);
        btnOff = (Button)findViewById(R.id.off_btn);
        btnDis = (Button)findViewById(R.id.disc_btn);
        powerBar = (SeekBar)findViewById(R.id.pwr_seekbar);
        pwrTW = (TextView)findViewById(R.id.pwr);

        new ConnectBT().execute(); // Connection class

        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed();
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        powerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    int percent = (int)((progress/255.0)*100);
                    pwrTW.setText(percent + "%");
                    try {
                        btSocket.getOutputStream().write(progress); // Send data to bluetooth module
                    }
                    catch (IOException e) {
                        toast("Error Sending Data");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    // If go back disconnect
    @Override
    public void onBackPressed() {
        disconnect();
        finish();
        return;
    }

    // Disconnection
    private void disconnect() {
        turnOffLed();
        if (btSocket != null) { // If bluetooth socket is taken then disconnect
            try {
                btSocket.close(); // Close bluetooth connection
            }
            catch (IOException e) {
                toast("Error Closing Socket");
            }
        }
        finish();
    }

    private void turnOffLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(0); // Send data to bt module: 0 (off)
                powerBar.setProgress(0);
                pwrTW.setText("0%");
            }
            catch (IOException e) {
                toast("Error Sending Data");
            }
        }
    }

    private void turnOnLed() {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(255); // Send data to bt module: 255 (on) [max attainable value]
                powerBar.setProgress(255);
                pwrTW.setText("100%");
            }
            catch (IOException e) {
                toast("Error Sending Data");
            }
        }
    }

    private void toast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> { // UI thread

        private boolean connectionSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(PinControl.this, "Connecting...", "Please wait!");  // Connection loading dialog
        }

        @Override
        protected Void doInBackground(Void... devices) { // Connect with bluetooth socket

            try {
                if (btSocket == null || !isBTConnected) { // If socket is not taken or device not connected
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address); // Connect to the chosen MAC address
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID); // This connection is not secure (mitm attacks)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery(); // Discovery process is heavy
                    btSocket.connect();
                }
            }
            catch (IOException e) {
                connectionSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) { // After doInBackground
            super.onPostExecute(result);

            if (!connectionSuccess) {
                toast("Connection Failed. Try again.");
                finish();
            }
            else {
                toast("Connected.");
                isBTConnected = true;
            }
            progress.dismiss();
        }
    }
}
