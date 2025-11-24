package com.example.myprojcet.ui.home.inner;

import java.util.Date;

public class Message {
    private String text;
    private boolean isUser;
    private Date messageTime;

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public Message(String text, boolean isUser, long time) {
        this.text = text;
        this.isUser = isUser;
        this.messageTime = new Date(time);

    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}
