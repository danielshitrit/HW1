package com.example.hw1;

import static android.Manifest.permission_group.LOCATION;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import  android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private EditText passwordEditText; // battery
    private boolean isWifiConnected = false;
    private boolean isFlashOn;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String deviceModel = Build.BRAND;


    protected void onStart() {
        super.onStart();
        tryToReadSSID();
        if(tryToReadSSID().equals("\"Afeka-Wifi-Open\""))
            isWifiConnected=true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordEditText = findViewById(R.id.passwordEditText);

        Log.d("pttt", ""+deviceModel);

        // Get battery percentage
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, intentFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        int batteryPercentage = (int) (batteryPct * 100);

        // Set password to battery level percentage
        String password = String.valueOf(batteryPercentage);

        // wifi
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            tryToReadSSID();
        } else {
            // Location permissions have not been granted, request them
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }

        // flash
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Check if the device has a flash unit
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            try {
                // Get the ID of the back-facing camera
                String cameraId = cameraManager.getCameraIdList()[0];

                cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                    @Override
                    public void onTorchModeChanged(String cameraId, boolean enabled) {
                        super.onTorchModeChanged(cameraId, enabled);

                        // Check the current state of the torch mode
                        if (enabled) {
                            isFlashOn=true;
                            // Torch mode is ON
                        } else {

                            isFlashOn=false;
                            // Torch mode is OFF
                        }
                    }
                }, new Handler());

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        // to pass to other activity
        findViewById(R.id.loginButton).setOnClickListener(view -> {
            String enteredPassword = passwordEditText.getText().toString();

            if (enteredPassword.equals(password) && isFlashOn && isWifiConnected && deviceModel.equals("samsung") ) {
                Toast.makeText(this, "Login approved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Login denied", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // get wifi name
    private String tryToReadSSID() {
        //If requested permission isn't Granted yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Integer.parseInt(String.valueOf(LOCATION)));
        }else{//Permission already granted
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo.getSupplicantState() == SupplicantState.COMPLETED){
                String ssid = wifiInfo.getSSID();//Here you can access your SSID
                return ssid;
            }
        }
        return null;
    }

    // get location name for wifi
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the permission request was for location permissions
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the location permissions have been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permissions have been granted, proceed with app logic
                // ...
            } else {
                // Location permissions have been denied, handle this case as necessary
                // ...
            }
        }
    }

}
