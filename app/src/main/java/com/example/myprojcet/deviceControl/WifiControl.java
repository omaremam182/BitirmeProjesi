package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class WifiControl {

    private Context context;

    public WifiControl(Context context) {
        this.context = context;
    }

    // Function to toggle Wi-Fi
    public void toggleWifi(boolean enableWifi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, direct toggle is not allowed, show Wi-Fi settings
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            context.startActivity(intent);
            Toast.makeText(context, "Go to Wi-Fi settings to toggle Wi-Fi", Toast.LENGTH_SHORT).show();
        } else {
            // For Android 9 and below, we can toggle Wi-Fi programmatically
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.setWifiEnabled(enableWifi);
                Toast.makeText(context, "Wi-Fi " + (enableWifi ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
