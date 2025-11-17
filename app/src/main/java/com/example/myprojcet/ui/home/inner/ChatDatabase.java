package com.example.myprojcet.ui.home.inner;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class ChatDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    public ChatDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Users table
        db.execSQL("CREATE TABLE users (" +
                "user_email TEXT PRIMARY KEY, " +
                "name TEXT)");

        // Conversations table
        db.execSQL("CREATE TABLE conversations (" +
                "conv_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT, " +
                "created_at LONG, " +
                "FOREIGN KEY (user_email) REFERENCES users(user_email))");

        // Messages table
        db.execSQL("CREATE TABLE messages (" +
                "msg_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "conv_id INTEGER, " +
                "sender TEXT, " +
                "message TEXT, " +
                "timestamp LONG, " +
                "FOREIGN KEY (conv_id) REFERENCES conversations(conv_id))");
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
        cv.put("created_at", System.currentTimeMillis());
        return db.insert("conversations", null, cv);
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
        cv.put("timestamp", System.currentTimeMillis());
        db.insert("messages", null, cv);
    }

    public Cursor getConversationMessages(long convId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM messages WHERE conv_id = ? ORDER BY timestamp ASC",
                new String[]{String.valueOf(convId)});
    }
}
