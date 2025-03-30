package com.example.omnimouse;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "OmniMouseMain";
    private static final String BASE_URL = "http://172.20.10.4:5000";  // Replace with your Python server IP
    // Sensor fields:
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor rotationVectorSensor;

    // Calibration fields:
    private float neutralPitch = 0, neutralRoll = 0;
    private float lastPitch = 0, lastRoll = 0;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity started");

        // Set up Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Initialize sensor manager and sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Set up button click events
        Button leftClickButton = findViewById(R.id.left_button);
        Button rightClickButton = findViewById(R.id.right_button);
        Button scrollUpButton = findViewById(R.id.ScrollUp);
        Button scrollDownButton = findViewById(R.id.ScrollDown);
        Button resetButton = findViewById(R.id.ResetButton);
        leftClickButton.setOnClickListener(v -> {
            Log.d(TAG, "Left click button pressed");
            Toast.makeText(MainActivity.this, "Left Clicked", Toast.LENGTH_SHORT).show();
            sendButtonPress(0);
        });
        rightClickButton.setOnClickListener(v -> {
            Log.d(TAG, "Right click button pressed");
            Toast.makeText(MainActivity.this, "Right Clicked", Toast.LENGTH_SHORT).show();
            sendButtonPress(1);
        });
        scrollUpButton.setOnClickListener(v -> {
            Log.d(TAG, "Scroll up button pressed");
            Toast.makeText(MainActivity.this, "Scrolled Up", Toast.LENGTH_SHORT).show();
            sendButtonPress(2);
        });
        scrollDownButton.setOnClickListener(v -> {
            Log.d(TAG, "Scroll down button pressed");
            Toast.makeText(MainActivity.this, "Scrolled Down", Toast.LENGTH_SHORT).show();
            sendButtonPress(3);
        });
        resetButton.setOnClickListener(v -> {
            Log.d(TAG, "Reset button pressed");
            Toast.makeText(MainActivity.this, "Reset", Toast.LENGTH_SHORT).show();
            sendButtonPress(4);
        });

        Button calibrateButton = findViewById(R.id.CalibrateGyro);
        calibrateButton.setOnClickListener(v -> {
            neutralPitch = lastPitch;
            neutralRoll = lastRoll;
            Log.d(TAG, "Calibration button pressed - new calibration set: neutralPitch="
                    + neutralPitch + ", neutralRoll=" + neutralRoll);
            Toast.makeText(MainActivity.this, "Calibrated!", Toast.LENGTH_SHORT).show();
        });

        // Register sensors immediately
        registerSensors();
    }

    /**
     * Registers the sensor listeners.
     */
    private void registerSensors() {
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "Gyroscope sensor registered");
        } else {
            Log.e(TAG, "Gyroscope sensor not available");
        }
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "Rotation vector sensor registered");
        } else {
            Log.e(TAG, "Rotation vector sensor not available");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float adjustedRoll = 0;
        float adjustedPitch = 0;
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

            // Calculate differences from the neutral position
            adjustedRoll = (neutralRoll + roll) * 2; // speed adjustment factor
            adjustedPitch = (neutralPitch + pitch) * 2;

       //     if (!(adjustedPitch < .25) || !(adjustedRoll < .25)){
                // Send the movement data to Python server via Retrofit
                sendMovementData(adjustedRoll, adjustedPitch);
       //     }


        }
    }

    private void sendMovementData(float dx, float dy) {
        MovementData movementData = new MovementData(dx, dy);
        Call<ResponseData> call = apiService.sendMovementData(movementData);

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Data sent successfully: " + response.body().getMessage());
                } else {
                    Log.e(TAG, "Error sending data: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "Failed to send data: " + t.getMessage());
            }
        });
    }

    private void sendButtonPress(int button){
        Call<ResponseData> call = apiService.sendButtonPress(button);

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Data sent successfully: " + response.body().getMessage());
                } else {
                    Log.e(TAG, "Error sending data: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "Failed to send data: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: Sensor " + sensor.getName() + " accuracy changed to " + accuracy);
    }
}
