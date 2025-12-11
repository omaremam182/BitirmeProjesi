package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class WhatsAppMessageHandler {

    private final Context context;

    public WhatsAppMessageHandler(Context context) {
        this.context = context;
    }

    public void sendWhatsAppMessage(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
           // Toast.makeText(context, "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        phoneNumber = phoneNumber.replace("+", "").replace(" ", "");

        phoneNumber = normalizeNumberForWhatsApp(phoneNumber);


        try {
            Uri uri = Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.whatsapp");

            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
        }
    }
//    public void sendWhatsAppMessage(String phoneNumber, String message) {
//
//        if (phoneNumber == null || phoneNumber.isEmpty()) {
//            Toast.makeText(context, "Invalid phone number.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Convert to international WhatsApp format
//        phoneNumber = normalizeNumberForWhatsApp(phoneNumber);
//
//        try {
//            Uri uri = Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message));
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            intent.setPackage("com.whatsapp");
//            context.startActivity(intent);
//
//        } catch (Exception e) {
//            Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
//        }
//    }

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
