package com.example.omnimouse;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "OmniMouseMain";

    // WiFi connection fields:
    private Socket wifiSocket;
    private DataOutputStream dataOutputStream;
    private static final int WIFI_PORT = 5000;  // Update if needed (note: comment says "Using TLS port (443)")
    // Update this with the IP address of your Python server
    private String SERVER_IP = "172.20.10.1";
    private volatile boolean isConnected = false;  // connection flag

    // Sensor fields (initialized but not registered yet):
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor rotationVectorSensor;

    // Calibration fields:
    private float neutralPitch = 0, neutralRoll = 0;
    private float lastPitch = 0, lastRoll = 0;
    private boolean isCalibrated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity started");

        // Initialize sensor manager and sensors (but do not register yet)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Set up button click events immediately (they don't rely on sensor data)
        Button leftClickButton = findViewById(R.id.left_button);
        Button rightClickButton = findViewById(R.id.right_button);
        leftClickButton.setOnClickListener(v -> {
            Log.d(TAG, "Left click button pressed");
            sendClickEvent("LEFT");
        });
        rightClickButton.setOnClickListener(v -> {
            Log.d(TAG, "Right click button pressed");
            sendClickEvent("RIGHT");
        });

        Button calibrateButton = findViewById(R.id.CalibrateGyro);
        calibrateButton.setOnClickListener(v -> {
            // Update the neutral calibration point using the latest sensor readings
            neutralPitch = lastPitch;
            neutralRoll = lastRoll;
            isCalibrated = true;
            Log.d(TAG, "Calibration button pressed - new calibration set: neutralPitch="
                    + neutralPitch + ", neutralRoll=" + neutralRoll);
            Toast.makeText(MainActivity.this, "Calibrated!", Toast.LENGTH_SHORT).show();
        });

        // Start connection; once connected, sensor registration will occur.
        connectToWifiFunc();
    }

    /**
     * Registers the sensor listeners once the connection is established.
     */
    private void registerSensors() {
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            Log.d(TAG, "Gyroscope sensor registered");
        } else {
            Log.e(TAG, "Gyroscope sensor not available");
        }
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
            Log.d(TAG, "Rotation vector sensor registered");
        } else {
            Log.e(TAG, "Rotation vector sensor not available");
        }
    }

    /**
     * Attempts to connect as a secure WiFi client to a remote server using SSL/TLS.
     * Uses a loop to retry until a connection is established.
     */
    private void connectToWifiFunc() {
        new Thread(() -> {
            while (!isConnected) {
                try {
                    Log.d(TAG, "Attempting to connect securely using custom SSLContext to server at " + SERVER_IP + ":" + WIFI_PORT);

                    // Get the custom SSLContext loaded with your self-signed certificate from res/raw/cert.pem
                    SSLContext sslContext = CustomSSLUtil.getSSLContext(MainActivity.this);
                    if (sslContext == null) {
                        throw new IOException("SSLContext is null");
                    }
                    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                    // Create an SSL socket using the custom SSLContext
                    wifiSocket = sslSocketFactory.createSocket(SERVER_IP, WIFI_PORT);
                    dataOutputStream = new DataOutputStream(wifiSocket.getOutputStream());
                    isConnected = true;  // set flag to true once connected
                    Log.d(TAG, "Securely connected to server using custom certificate!");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Connected to secure server!", Toast.LENGTH_SHORT).show();
                        // Register sensors only after a successful connection
                        registerSensors();
                    });

                    // Start a thread to listen for server responses
                    listenForServerResponses();
                } catch (IOException e) {
                    Log.e(TAG, "connectToWifiFunc: Secure connection failed: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to connect to secure server! Retrying...", Toast.LENGTH_SHORT).show());
                    try {
                        Thread.sleep(3000);  // Wait 3 seconds before retrying
                    } catch (InterruptedException ie) {
                        Log.e(TAG, "InterruptedException during sleep", ie);
                    }
                }
            }
        }).start();
    }

    /**
     * Listens for responses from the server and logs them.
     */
    private void listenForServerResponses() {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(wifiSocket.getInputStream()));
                String response;
                while ((response = reader.readLine()) != null) {
                    Log.d(TAG, "Server response: " + response);
                    // Optionally update the UI here using runOnUiThread(...)
                }
            } catch (IOException e) {
                Log.e(TAG, "listenForServerResponses: Error reading server response", e);
            }
        }).start();
    }

    /**
     * Sends data over the secure WiFi connection using the DataOutputStream.
     */
    private void sendData(String data) {
        if (!isConnected || dataOutputStream == null) {
            Log.e(TAG, "sendData: DataOutputStream is null or not connected");
            return;
        }
        try {
            // Append newline to delimit messages (helpful for server-side reading)
            dataOutputStream.writeBytes(data + "\n");
            dataOutputStream.flush();
            Log.d(TAG, "sendData: Data sent: " + data);
        } catch (IOException e) {
            Log.e(TAG, "sendData: Data send failed", e);
        }
    }

    /**
     * Convenience method to send click events.
     */
    private void sendClickEvent(String type) {
        Log.d(TAG, "Sending click event: " + type);
        sendData("CLICK:" + type);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Since sensors are now registered only after connection, we no longer get events too early.
        float dx = 0;
        float dy = 0;
        float rotationRateX = 0;
        float rotationRateY = 0;

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Retrieve gyroscope values (angular speeds)
            rotationRateX = event.values[0];
            rotationRateY = event.values[1];
            Log.d(TAG, "onSensorChanged: Gyroscope event - X: " + rotationRateX + ", Y: " + rotationRateY);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // Use rotation vector to get device orientation
            float[] rotationMatrix = new float[9];
            float[] orientationAngles = new float[3];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            float pitch = (float) Math.toDegrees(orientationAngles[1]);
            float roll = (float) Math.toDegrees(orientationAngles[2]);
            Log.d(TAG, "onSensorChanged: Rotation vector event - Pitch: " + pitch + ", Roll: " + roll);

            // Update the latest sensor readings
            lastPitch = pitch;
            lastRoll = roll;

            // Auto-calibrate on the first reading if not yet calibrated
            if (!isCalibrated) {
                neutralPitch = pitch;
                neutralRoll = roll;
                Log.d(TAG, "onSensorChanged: Auto-calibrating - neutralPitch: " + neutralPitch + ", neutralRoll: " + neutralRoll);
            }
            // Calculate differences from the neutral position
            dx = (roll - neutralRoll) * 2; // speed adjustment factor
            dy = (neutralPitch - pitch) * 2;
        }

        // Combine sensor data to send movement data
        String moveData = "MOVE:" + (dx * (rotationRateX + 1)) + "," + (dy * (rotationRateY + 1));
        Log.d(TAG, "onSensorChanged: Sending movement data: " + moveData);
        sendData(moveData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: Sensor " + sensor.getName() + " accuracy changed to " + accuracy);
    }
}
