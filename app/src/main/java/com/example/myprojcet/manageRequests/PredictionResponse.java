package com.example.myprojcet.manageRequests;

public class PredictionResponse {
    private String text;
    private int class_id;
    private String label;

    public int getClass_id() {
        return class_id;
    }

    public String getLabel() {
        return label;
    }

    public String getText() {
        return text;
    }
}
