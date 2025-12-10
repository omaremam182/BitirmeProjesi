package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactResolver {

    private Context context;

    public ContactResolver(Context context) {
        this.context = context;
    }

    public String getPhoneNumber(String contactName) {
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
}
