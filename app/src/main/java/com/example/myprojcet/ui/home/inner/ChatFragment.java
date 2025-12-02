package com.example.myprojcet.ui.home.inner;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myprojcet.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.myprojcet.BuildConfig;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChatFragment extends Fragment {

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private RecyclerView recyclerView;
    private EditText input;
    private ImageButton sendButton;
    private ImageButton attachButton;
    private ChatAdapter adapter;
    private ChatDatabase my_db;
    long conversation_id = -3;
    Cursor cursor;
    boolean isUser;
    private List<Message> messages = new ArrayList<>();
    private List<JSONObject> conversationHistory = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.chatRecyclerView);
        input = view.findViewById(R.id.edittext_home);
        sendButton = view.findViewById(R.id.sendButton);
        attachButton = view.findViewById(R.id.attach_btn);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String u_email = user.getEmail();
        my_db = new ChatDatabase(getContext());

        if(!my_db.isEmailExists(u_email)){
            my_db.insertUser(u_email, "");
        }

        if (getArguments() != null) {
            conversation_id = getArguments().getLong("conv_id");
            getArguments().clear();
            cursor = my_db.getConversationMessages(conversation_id);
        }


        if (cursor != null && cursor.moveToFirst()) {
            do {
                String messageText = cursor.getString(cursor.getColumnIndex("message"));
                String sender = cursor.getString(cursor.getColumnIndex("sender"));
                long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));

                isUser = sender.equals("user");

                messages.add(new Message(messageText, isUser,timestamp));

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        attachButton.setOnClickListener(v3->{
            Toast.makeText(getContext(),"It is working",Toast.LENGTH_LONG);
            BottomSheetDialog bottomSheet = new BottomSheetDialog(getContext());
            View sheetView = getLayoutInflater().inflate(R.layout.attach_menu, null);

            bottomSheet.setContentView(sheetView);
            bottomSheet.show();

            LinearLayout itemGallery = sheetView.findViewById(R.id.item_gallery);
            LinearLayout itemCamera = sheetView.findViewById(R.id.item_camera);

            itemGallery.setOnClickListener(mview -> {
                bottomSheet.dismiss();
            });

            itemCamera.setOnClickListener(view2 -> {
                bottomSheet.dismiss();
            });
        });
        sendButton.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                addMessage(text, true);
                Log.d("Conversation ID", "your Conversation ID before sending any message is :  "+conversation_id);

                if(conversation_id == -3)
                    conversation_id = my_db.createConversation(u_email);

                my_db.insertMessage(conversation_id,"user",text);
                input.setText("");
                Log.d("Conversation ID", "your Conversation ID after sending the message is :  "+conversation_id);

                sendMessageToGroq(text,conversation_id);
            }
        });

        return view;
    }

    private void addMessage(String text, boolean isUser) {
        messages.add(new Message(text, isUser,System.currentTimeMillis()));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void sendMessageToGroq(String userMessage,long conv_id) {
        new Thread(() -> {
            try {
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                conversationHistory.add(userMsg);

                JSONObject json = new JSONObject();
                json.put("model", "openai/gpt-oss-20b");
                JSONArray messagesArray = new JSONArray(conversationHistory);
                json.put("messages", messagesArray);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(ENDPOINT)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JSONObject jsonResponse = new JSONObject(responseBody);
                String reply = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");


                JSONObject botMsg = new JSONObject();
                botMsg.put("role", "assistant");
                botMsg.put("content", reply);

                my_db.insertMessage(conv_id,"bot",reply);

                conversationHistory.add(botMsg);

                requireActivity().runOnUiThread(() -> addMessage(reply, false));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        addMessage("Error: " + e.getMessage(), false));
            }
        }).start();
    }
}
