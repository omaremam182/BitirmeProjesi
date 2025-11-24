package com.example.myprojcet.ui.home.inner;

public class Conversation {
    private long convId;
    private String convTitle;

    public Conversation(long convId, String convTitle) {
        this.convId = convId;
        this.convTitle = convTitle;
    }

    public long getConvId() {
        return convId;
    }

    public String getConvTitle() {
        return convTitle;
    }
}
