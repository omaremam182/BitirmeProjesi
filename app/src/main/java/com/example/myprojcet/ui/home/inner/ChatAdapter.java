package com.example.myprojcet.ui.home.inner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myprojcet.R;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private Context context;
    private TextToSpeech tts;
    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }


    public ChatAdapter(List<Message> messages, Context context, TextToSpeech tts) {
        this.messages = messages;
        this.context = context;
        this.tts = tts;
    }


    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 0 : 1;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_bot, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Message message = messages.get(position);
        holder.messageText.setText(message.getText());

//         USER MESSAGE - hide buttons
        if (message.isUser()) {
            if (holder.copyBtn != null) holder.copyBtn.setVisibility(View.GONE);
            if (holder.voiceBtn != null) holder.voiceBtn.setVisibility(View.GONE);
            return;
        }

        // BOT MESSAGE - show buttons
        if (holder.copyBtn != null) {

            // COPY TEXT
            holder.copyBtn.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager)
                        v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText("bot_message", message.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(v.getContext(), "Copied!", Toast.LENGTH_SHORT).show();
            });
        }

        // VOICE BUTTON
        if (holder.voiceBtn != null) {
            holder.voiceBtn.setOnClickListener(v -> {
                if (tts != null) {
                    tts.speak(message.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Toast.makeText(context, "TextToSpeech not initialized", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageButton copyBtn, voiceBtn;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            copyBtn = itemView.findViewById(R.id.btn_copy);
            voiceBtn = itemView.findViewById(R.id.btn_voice);
        }
    }
}
