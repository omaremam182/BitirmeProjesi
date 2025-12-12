package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class GoogleHandler {
    private Context context;
    public GoogleHandler(Context context){
        this.context = context;
    }
    public void googleSearch(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra("query", query);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // OR open a browser:
    public void googleBrowserSearch(String query) {
        String url = "https://www.google.com/search?q=" + Uri.encode(query);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
