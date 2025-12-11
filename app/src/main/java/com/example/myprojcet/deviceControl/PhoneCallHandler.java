package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class PhoneCallHandler {

    private Context context;

    public PhoneCallHandler(Context context) {
        this.context = context;
    }

//    public void callContact(String contactName){
//
//        ContactResolver contactResolver = new ContactResolver(this.context);
//        String phoneNumber  =  contactResolver.getPhoneNumber(contactName);
//        if(phoneNumber == null || phoneNumber.isEmpty()){
//            Toast.makeText(this.context, "Contact not found!", Toast.LENGTH_SHORT).show();
//
//            return;
//        }
//        callPhoneNumber(phoneNumber);
//    }
    public void callPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
//            Toast.makeText(context, "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checking if the app has permission to make calls
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission to make calls is not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the intent to initiate a phone call
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }
}
