package com.example.myprojcet.deviceControl;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.myprojcet.MainActivity;

public class BluetoothControl {

    private Context context;

    public BluetoothControl(Context context) {
        this.context = context;
    }

    // Function to enable or disable Bluetooth based on the API level
    public void toggleBluetooth(boolean enableBluetooth) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle permissions for Android 12 (API 31) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Activity activity = (Activity) context;
                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_ADMIN
                        }, 1);
                // Toast.makeText(context, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // For Android 9 (API 28) and below, directly enable/disable Bluetooth
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (enableBluetooth) {
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable(); // Enable Bluetooth
                    Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable(); // Disable Bluetooth
                    Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Bluetooth is already disabled", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // For Android 10 (API 29) and above
            if (enableBluetooth) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    context.startActivity(enableBtIntent); // Ask user to enable Bluetooth
                    Toast.makeText(context, "Requesting to enable Bluetooth", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Redirect user to Bluetooth settings (to disable it)
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                context.startActivity(intent); // Let user disable Bluetooth manually
                Toast.makeText(context, "Go to Bluetooth settings to disable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
