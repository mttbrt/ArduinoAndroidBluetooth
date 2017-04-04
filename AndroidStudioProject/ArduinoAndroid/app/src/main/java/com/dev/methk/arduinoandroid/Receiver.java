package com.dev.methk.arduinoandroid;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class Receiver extends AppCompatActivity {

    // Widgets
    public Button btnDis;
    public TextView dataTW;
    public String address = null;

    // Bluetooth
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBTConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Thread workerThread;
    byte[] generalBuffer;
    int generalBufferPosition;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newInt = getIntent();
        address = newInt.getStringExtra(DeviceList.EXTRA_ADDRESS); // MAC address of the chosen device

        setContentView(R.layout.activity_receiver);

        // Initialize widgets
        btnDis = (Button)findViewById(R.id.BTN_disc);
        dataTW = (TextView)findViewById(R.id.TW_data);

        new ConnectBT().execute(); // Connection class

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
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

    private void toast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    public void beginListenForData() {
        final Handler handler = new Handler(); // Interacts between this thread and UI thread
        final byte delimiter = 35; // ASCII code for (#) end of transmission

        stopWorker = false;
        generalBufferPosition = 0;
        generalBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = btSocket.getInputStream().available(); // Received bytes by bluetooth module

                        if (bytesAvailable > 0) {
                            byte[] packet = new byte[bytesAvailable];
                            btSocket.getInputStream().read(packet);

                            for (int i=0; i<bytesAvailable; i++) {
                                byte b = packet[i];
                                if (b == delimiter) { // If found a # print on screen
                                    byte[] arrivedBytes = new byte[generalBufferPosition];
                                    System.arraycopy(generalBuffer, 0, arrivedBytes, 0, arrivedBytes.length);
                                    final String data = new String(arrivedBytes, "US-ASCII"); // Decode from bytes to string
                                    generalBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            dataTW.setText(data+" °C"); // Print on screen
                                        }
                                    });
                                }
                                else { // If there is no # add bytes to buffer
                                    generalBuffer[generalBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> { // UI thread

        private boolean connectionSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Receiver.this, "Connecting...", "Please wait!"); // Connection loading dialog
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
                beginListenForData();
                isBTConnected = true;
            }
            progress.dismiss();
        }
    }
}
