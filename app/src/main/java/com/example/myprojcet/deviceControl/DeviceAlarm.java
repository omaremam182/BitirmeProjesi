package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.widget.Toast;

public class DeviceAlarm {

    private Context context;

    public DeviceAlarm(Context context) {
        this.context = context;
    }

    /**
     * Sets a real device alarm that will ring and wake the user.
     *
     * @param hour    Hour in 24-hour format (0-23)
     * @param minute  Minute (0-59)
     * @param message Label for the alarm
     */
    public void setDeviceAlarm(int hour, int minute, String message) {
        try {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);

            // Show Clock app UI for user confirmation (safe on MIUI / Samsung / modern Android)
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);

            // Verify that an app exists to handle this intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Toast.makeText(context, "Setting alarm for " + hour + ":" + (minute < 10 ? "0" + minute : minute), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No Clock app found to set the alarm!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to set alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
