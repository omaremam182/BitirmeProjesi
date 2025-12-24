package com.example.myprojcet.ui.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojcet.R;
import com.example.myprojcet.ui.home.inner.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public ConversationAdapter(List<Conversation> conversations,
                               OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.convTitle.setText(conversation.getConvTitle());

        String formattedTime = formatTime(conversation.getLastMessageTime());
        holder.lastMessageSendAt.setText(formattedTime);

        holder.itemView.setOnClickListener(v ->
                listener.onConversationClick(conversation.getConvId())
        );

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.btnMore);
            popupMenu.inflate(R.menu.conversation_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_rename) {
                    listener.onRenameConversation(conversation.getConvId());
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDeleteConversation(conversation.getConvId());
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    // ================= ViewHolder =================

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView convTitle;
        TextView lastMessageSendAt;
        ImageButton btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            convTitle = itemView.findViewById(R.id.convTitle);
            lastMessageSendAt = itemView.findViewById(R.id.lastMessageSendTime);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

    // ================= Listener =================

    public interface OnConversationClickListener {
        void onConversationClick(long convId);
        void onRenameConversation(long convId);
        void onDeleteConversation(long convId);
    }

    // ================= Utils =================

    private String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}

