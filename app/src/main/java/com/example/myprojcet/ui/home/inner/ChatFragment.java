package com.example.myprojcet.ui.home.inner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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


public class ChatFragment extends Fragment {

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private RecyclerView recyclerView;
    private EditText input;
    private ImageButton sendButton;
    private ChatAdapter adapter;
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

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                addMessage(text, true);
                input.setText("");
                sendMessageToGroq(text);
            }
        });

        return view;
    }

    private void addMessage(String text, boolean isUser) {
        messages.add(new Message(text, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void sendMessageToGroq(String userMessage) {
        new Thread(() -> {
            try {
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                conversationHistory.add(userMsg);

                JSONObject json = new JSONObject();
                json.put("model", "qwen/qwen3-32b");
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
