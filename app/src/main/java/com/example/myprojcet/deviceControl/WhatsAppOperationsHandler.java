package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class WhatsAppOperationsHandler {

    private final Context context;

    public WhatsAppOperationsHandler(Context context) {
        this.context = context;
    }

    public void sendWhatsAppMessage(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(context, "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("PhoneNumber1", phoneNumber);
//        phoneNumber = phoneNumber.replace("+", "").replace(" ", "");

        phoneNumber = normalizeNumberForWhatsApp(phoneNumber);

        Log.d("PhoneNumber2", phoneNumber);


        try {
            Uri uri = Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.whatsapp");

            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
        }
    }


    public void makeWhatsAppCall(String phoneNumber, boolean videoCall) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(context, "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }
//       phoneNumber = phoneNumber.replace("+", "").replace(" ", "");

        phoneNumber = normalizeNumberForWhatsApp(phoneNumber);

        Log.d("PhoneNumber2", phoneNumber);


        try {
            String uri;

            if (videoCall) {
                // WhatsApp video call
                uri = "whatsapp://call?video=true&number=" + phoneNumber;
            } else {
                // WhatsApp voice call
                uri = "whatsapp://call?number=" + phoneNumber;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
        }
    }
    private String normalizeNumberForWhatsApp(String number) {
        if (number == null) return null;

        number = number.replaceAll("[^0-9]", "");

        // Convert 05xxxxxxxx to 90xxxxxxxxxx
        if (number.startsWith("0")) {
            number = "90" + number.substring(1);   // 🇹🇷 Türkiye
        }

        return number;
    }

}
