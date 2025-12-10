package com.example.myprojcet.deviceControl;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class SmsSender {

    private Context context;

    public static final String SMS_SENT = "SMS_SENT";
    public static final String SMS_DELIVERED = "SMS_DELIVERED";

    public SmsSender(Context context) {
        this.context = context;
    }

    public void sendSms(String phoneNumber, String message) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(context, "SMS permission not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();

        PendingIntent sentPI = PendingIntent.getBroadcast(
                context, 0, new Intent(SMS_SENT), PendingIntent.FLAG_IMMUTABLE);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(
                context, 0, new Intent(SMS_DELIVERED), PendingIntent.FLAG_IMMUTABLE);

        smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        Toast.makeText(context, "Sending SMS…", Toast.LENGTH_SHORT).show();
    }
}
