package com.example.myprojcet.ui.gallery;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojcet.R;
import com.example.myprojcet.databinding.FragmentGalleryBinding;
import com.example.myprojcet.ui.home.inner.ChatDatabase;
import com.example.myprojcet.ui.home.inner.ChatFragment;
import com.example.myprojcet.ui.home.inner.Conversation;
import com.example.myprojcet.ui.home.inner.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GalleryFragment extends Fragment {
    private ChatDatabase my_db;
    private FragmentGalleryBinding binding;
    private ConversationAdapter adapter;
    private List<Conversation> conversations ;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        conversations = new ArrayList<>();
        my_db = new ChatDatabase(getContext());

        loadConversations();

        adapter = new ConversationAdapter(conversations,
                new ConversationAdapter.OnConversationClickListener() {

                    @Override
                    public void onConversationClick(long convId) {
                        openChatFragment(convId);
                    }

                    @Override
                    public void onRenameConversation(long convId) {
                        showRenameDialog(convId);
                    }

                    @Override
                    public void onDeleteConversation(long convId) {
                        showDeleteConfirmDialog(convId);
                    }
                });

        RecyclerView recyclerView = binding.recyclerViewConversations;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return root;
    }

    private void loadConversations() {
        conversations.clear();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String u_email = user.getEmail();

        Cursor cr = my_db.getAllConversations(u_email);
        while (cr.moveToNext()) {
            long id = cr.getLong(cr.getColumnIndex("conv_id"));
            String title = cr.getString(cr.getColumnIndex("conv_title"));
            long time = cr.getLong(cr.getColumnIndex("last_message_sended_at"));

            conversations.add(new Conversation(id, title, time));
        }
        cr.close();
    }

    private void refreshConversations() {
        loadConversations();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void showRenameDialog(long convId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename Conversation");

        final EditText input = new EditText(getContext());
        input.setHint("Enter new name");
        input.setPadding(40, 30, 40, 30);

        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();

            if (!newName.isEmpty()) {
                my_db.updateConvTitle(convId, newName);
                refreshConversations();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showDeleteConfirmDialog(long convId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    my_db.deleteConversation(convId);
                    refreshConversations();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openChatFragment(long convId) {

        Bundle bundle = new Bundle();
        bundle.putLong("conv_id", convId);

        NavController navController = NavHostFragment.findNavController(GalleryFragment.this);
        navController.navigate(R.id.action_galleryFragment_to_chatFragment, bundle);
    }
}
