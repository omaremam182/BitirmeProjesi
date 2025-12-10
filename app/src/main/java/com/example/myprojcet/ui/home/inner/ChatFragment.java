package com.example.myprojcet.ui.home.inner;

import static java.security.AccessController.getContext;

import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.myprojcet.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.ExpandableListView;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;

public class ChatFragment extends Fragment {

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private RecyclerView recyclerView;
    private TextToSpeech tts;
    private EditText input;
    private ImageButton sendButton;
    private ImageButton attachButton;
    private ChatAdapter adapter;
    private ChatDatabase my_db;
    long conversation_id = -3;
   private Cursor cursor;
    private boolean isUser;
    private RelativeLayout editTextContainer;
    private List<Message> messages = new ArrayList<>();
    private List<JSONObject> conversationHistory = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    ExpandableListView expandableListView ;
    List<Map<String, String>> groupData;
    List<List<Map<String, String>>> childData;
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
        editTextContainer = view.findViewById(R.id.chat_edittext_container);

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

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                do {
                    String messageText = cursor.getString(cursor.getColumnIndex("message"));
                    String sender = cursor.getString(cursor.getColumnIndex("sender"));
                    long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));

                    isUser = sender.equals("user");

                    messages.add(new Message(messageText, isUser,timestamp));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        expandableListView = view.findViewById(R.id.expandableListView);
        groupData = new ArrayList<>();
        childData = new ArrayList<>();




        if(messages.isEmpty()){
            Map<String, String> group1 = new HashMap<>();
            group1.put("Group", "Spor");
            groupData.add(group1);

            List<Map<String, String>> group1Children = new ArrayList<>();

            Map<String, String> child1 = new HashMap<>();
            child1.put("Item", "Futbol");
            group1Children.add(child1);

            Map<String, String> child2 = new HashMap<>();
            child2.put("Item", "Dünya kupası");
            group1Children.add(child2);

            childData.add(group1Children);

            Map<String, String> group2 = new HashMap<>();
            group2.put("Group", "Teknoloji");
            groupData.add(group2);

            List<Map<String, String>> group2Children = new ArrayList<>();

            Map<String, String> childA = new HashMap<>();
            childA.put("Item", "Yapay Zeka");
            group2Children.add(childA);

            Map<String, String> childB = new HashMap<>();
            childB.put("Item", "Yazılım");
            group2Children.add(childB);

            Map<String, String> group3 = new HashMap<>();
            group3.put("Group", "Tıp");
            groupData.add(group3);
            childData.add(group2Children);


            List<Map<String, String>> group3Children = new ArrayList<>();

            Map<String, String> child3A = new HashMap<>();
            child3A.put("Item", "Kardiyoloji");
            group3Children.add(child3A);

            Map<String, String> child3B = new HashMap<>();
            child3B.put("Item", "Nöroloji");
            group3Children.add(child3B);
            childData.add(group3Children);

            ExpandableListAdapter expandableListAdapter =
                    new SimpleExpandableListAdapter(
                            getContext(),
                            groupData,
                            android.R.layout.simple_expandable_list_item_1,
                            new String[]{"Group"},
                            new int[]{android.R.id.text1},
                            childData,
                            android.R.layout.simple_list_item_1,
                            new String[]{"Item"},
                            new int[]{android.R.id.text1}
                    );

            expandableListView.setAdapter(expandableListAdapter);

        }


        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                Map<String, String> selectedChild = childData.get(groupPosition).get(childPosition);
                String itemName = selectedChild.get("Item");

                //Toast.makeText(getContext(), "You clicked: " + itemName, Toast.LENGTH_SHORT).show();

                if(conversation_id == -3)
                    conversation_id = my_db.createConversation(u_email);

                String text =  itemName+ " alanindaki son gelişmeler nelerdir";
                addMessage(text, true);

                my_db.insertMessage(conversation_id,"user",text);
                sendMessageToGroq(text,conversation_id);

                return true;
            }
        });

        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("tr", "TR"));
            }
        });

        adapter = new ChatAdapter(messages, getContext(), tts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        attachButton.setOnClickListener(v -> {

            View popupView = getLayoutInflater().inflate(R.layout.attach_popup, null);

            int width = editTextContainer.getWidth();

            int heightInDp = 180;
            int heightInPx = (int) (heightInDp * getResources().getDisplayMetrics().density);

            popupView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(heightInPx, View.MeasureSpec.EXACTLY)
            );

            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    width,
                    heightInPx,
                    true
            );

            popupWindow.setElevation(12);

            int[] location = new int[2];
            editTextContainer.getLocationOnScreen(location);

            int popupY = location[1] - heightInPx - 10;

            popupWindow.showAtLocation(editTextContainer, Gravity.NO_GRAVITY,
                    location[0], popupY);

            LinearLayout open_ai_model = popupView.findViewById(R.id.open_ai);
            LinearLayout meta_model  = popupView.findViewById(R.id.meta);

            open_ai_model.setOnClickListener(e -> {
                Toast.makeText(getContext(), "Open AI", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            meta_model.setOnClickListener(e -> {
                Toast.makeText(getContext(), "META", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
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
        expandableListView.setVisibility(View.GONE);

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
    private void startNewConversation() {
        conversation_id = -3;
        messages.clear();
        conversationHistory.clear();
        adapter.notifyDataSetChanged();
        if (cursor != null) cursor.close();
    }

}
