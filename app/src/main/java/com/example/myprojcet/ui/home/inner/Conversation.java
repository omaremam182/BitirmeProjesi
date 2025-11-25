package com.example.myprojcet.ui.home.inner;

import java.util.Date;

public class Conversation {
    private long convId;
    private String convTitle;
    Date lastMessageTime;

    public Conversation(long convId, String convTitle,long time) {
        this.convId = convId;
        this.convTitle = convTitle;
        this.lastMessageTime = new Date(time);
    }

    public long getConvId() {
        return convId;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public String getConvTitle() {
        return convTitle;
    }
}
