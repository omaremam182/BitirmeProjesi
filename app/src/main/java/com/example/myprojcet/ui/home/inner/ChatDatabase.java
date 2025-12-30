package com.example.myprojcet.ui.home.inner;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class ChatDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "my_chatbot.db";
    private static final int DATABASE_VERSION = 1;

    public ChatDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL("CREATE TABLE users (" +
                "user_email TEXT PRIMARY KEY, " +
                "name TEXT)");


        db.execSQL("CREATE TABLE conversations (" +
                "conv_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT, " +
                "conv_title TEXT, "+
                "created_at LONG, " +
                "last_message_sended_at LONG, " +
                "FOREIGN KEY (user_email) REFERENCES users(user_email))");


        db.execSQL("CREATE TABLE messages (" +
                "msg_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "conv_id INTEGER, " +
                "sender TEXT, " +
                "message TEXT, " +
                "timestamp LONG, " +
                "FOREIGN KEY (conv_id) REFERENCES conversations(conv_id))");
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE user_email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("DROP TABLE IF EXISTS conversations");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ---------------------------------------
    // USERS
    // ---------------------------------------
    public void insertUser(String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", email);
        cv.put("name", name);
        db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // ---------------------------------------
    // CONVERSATIONS
    // ---------------------------------------
    public long createConversation(String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", userEmail);
        cv.put("conv_title", "New Conversation");
        cv.put("created_at", System.currentTimeMillis());
        cv.put("last_message_sended_at", System.currentTimeMillis());
        return db.insert("conversations", null, cv);
    }

    public void updateConvTitle(long convId, String newConvTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("conv_title", newConvTitle);

        int rowsAffected = db.update("conversations", cv, "conv_id = ?", new String[]{String.valueOf(convId)});

        if (rowsAffected > 0) {
            Log.d("DB Update", "Conversation title updated successfully!");
        } else {
            Log.d("DB Update", "No conversation found with the given ID.");
        }

        db.close();
    }
    public void updateLastMessageTimestamp(long convId, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("last_message_sended_at", timestamp);

        int rowsAffected = db.update("conversations", cv, "conv_id = ?", new String[]{String.valueOf(convId)});

        if (rowsAffected > 0) {
            Log.d("DB Update", "Last message timestamp updated successfully!");
        } else {
            Log.d("DB Update", "No conversation found with the given ID.");
        }

        db.close();
    }

    public void deleteConversation(long convId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsAffected = db.delete("conversations","conv_id = ?", new String[]{String.valueOf(convId)});


        if (rowsAffected > 0) {
            Log.d("DB Delete", "The sonversation deleted successfully");
        } else {
            Log.d("DB Delete", "No conversation found with the given ID.");
        }

        db.close();
    }
    public List<String> getLastConversationTitles(String userEmail, int limit) {

        List<String> titles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT conv_title " +
                        "FROM conversations " +
                        "WHERE user_email = ? " +
                        "AND conv_title IS NOT NULL AND conv_title != '' " +
                        "ORDER BY last_message_sended_at DESC " +
                        "LIMIT ?";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{userEmail, String.valueOf(limit)}
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                titles.add(cursor.getString(0));
            }
            cursor.close();
        }

        return titles;
    }


    // ---------------------------------------
    // MESSAGES
    // ---------------------------------------
    public void insertMessage(long convId, String sender, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("conv_id", convId);
        cv.put("sender", sender);
        cv.put("message", message);
        long myCurrentTime = System.currentTimeMillis();
        cv.put("timestamp",myCurrentTime);
        db.insert("messages", null, cv);
        updateLastMessageTimestamp(convId,myCurrentTime);
    }

    public Cursor getConversationMessages(long convId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM messages WHERE conv_id = ? ORDER BY timestamp ASC",
                new String[]{String.valueOf(convId)});
    }

    public Cursor getAllConversations(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM conversations WHERE user_email = ? ORDER BY last_message_sended_at DESC",
                new String[]{email});
    }

}
