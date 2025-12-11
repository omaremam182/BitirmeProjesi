package com.example.myprojcet.deviceControl;

import static androidx.core.app.ActivityCompat.requestPermissions;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myprojcet.MainActivity;

public class ContactResolver {

    private static final int READ_CONTACTS_REQUEST = 103;
    private Context context;
    private Activity activity;

    public ContactResolver(Context context , Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public String findNumberByContact(String contactName){

        String phoneNumber  =  this.getPhoneNumber(contactName);
        if(phoneNumber == null || phoneNumber.trim().isEmpty()){
//            Toast.makeText(this.context, "Contact not found!", Toast.LENGTH_SHORT).show();
            return null;
        }
        return phoneNumber;
    }
    public String getPhoneNumber(String contactName) {
        if (ContextCompat.checkSelfPermission(this.context,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.context, "You have to active the contact permission first", Toast.LENGTH_SHORT).show();

            requestReadContactsPermission();
            return null;
        }

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = { "%" + contactName + "%" };

        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            );

            int hasPhone = cursor.getInt(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            );

            if (hasPhone > 0) {
                Cursor phoneCursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    String phoneNumber = phoneCursor.getString(
                            phoneCursor.getColumnIndexOrThrow(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER)
                    );
                    phoneCursor.close();
                    cursor.close();
                    return phoneNumber;
                }
            }
        }

        if (cursor != null) cursor.close();
        return null;
    }

    private void requestReadContactsPermission() {
        requestPermissions(
                this.activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                READ_CONTACTS_REQUEST
        );
    }
}
