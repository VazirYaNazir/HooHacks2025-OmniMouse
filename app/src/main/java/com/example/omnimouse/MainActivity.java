package com.example.omnimouse;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

// Bluetooth imports
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

// Hardware imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// Androidx imports
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

// Widget imports
import android.widget.Button;
import android.widget.Toast;

// View imports
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

// Java imports
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private float neutralPitch = 0, neutralRoll = 0;
    private boolean isCalibrated = false;

    private String DEVICE_ADDRESS = "XX:XX:XX:XX:XX:XX"; // Replace with your PC's Bluetooth MAC
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID

    private void connectToBluetoothFunc(){
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try{
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException aghhhh) {
            Log.e("Bluetooth", "Connection failed", aghhhh);
            Toast.makeText(this, "Failed to connect! Fix your Connection!", Toast.LENGTH_SHORT).show();
        }catch (SecurityException ohno){
            Log.e("Bluetooth", "Security failure", ohno);
        }
    }

    public void setDEVICE_ADDRESS(String DEVICE_ADDRESS){
        this.DEVICE_ADDRESS = DEVICE_ADDRESS;
    }

    private void sendData(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
            } catch (IOException e) {
                Log.e("Bluetooth", "Data send failed", e);
            }
        }
    }

    private void sendClickEvent(String type) {
        sendData("CLICK:" + type);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float rotationRateX = event.values[0];  // Rotation along X axis
            float rotationRateY = event.values[1];  // Rotation along Y axis
            float rotationRateZ = event.values[2];  // Rotation along Z axis

        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // create the two button's functionality
       Button leftClickButton = findViewById(R.id.left_button);
       Button rightClickButton = findViewById(R.id.right_button);
       leftClickButton.setOnClickListener(func -> sendClickEvent("LEFT"));
       rightClickButton.setOnClickListener(func -> sendClickEvent("RIGHT"));
       //Button calibrateButton = findViewById(R.id.calibrateButton);

        // code to go to place
       Button goToInputCodeButton = findViewById(R.id.goToInputCodeButton);
       goToInputCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InputCodeActivity.class);
                startActivity(intent);
            }
        });
//         final Button leftClick = findViewById(R.id.left_button);
//         leftClick.setOnClickListener(
//             new View.OnClickListener() {
//                 @Override
//                 public void onClick(View view) {
//                     Log.d("TestLeft", "Clicked Left!");
//                 }
//             }
//         );
//         final Button rightClick = findViewById(R.id.right_button);
//         rightClick.setOnClickListener(
//             new View.OnClickListener(){
//                 @Override
//                 public void onClick(View view) {
//                     Log.d("TestLeft", "Clicked Right!");
//                 }
//             }
//         );
    }
}
