package com.example.myprojcet.ui.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojcet.R;
import com.example.myprojcet.ui.home.inner.Conversation;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.convTitle.setText(conversation.getConvTitle());
        holder.itemView.setOnClickListener(v -> listener.onConversationClick(conversation.getConvId()));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView convTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            convTitle = itemView.findViewById(R.id.convTitle);
        }
    }

    public interface OnConversationClickListener {
        void onConversationClick(long convId);
    }
}
