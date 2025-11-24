package com.example.myprojcet.ui.gallery;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
//
//public class GalleryFragment extends Fragment {
//    ChatDatabase my_db;
//    private FragmentGalleryBinding binding;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        binding = FragmentGalleryBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        String u_email = user.getEmail();
//
//        my_db = new ChatDatabase(getContext());
//
//        Cursor cr = my_db.getAllConversations(u_email);
//        List<Conversation> conversations = new ArrayList<>();
//        while (cr.moveToNext()) {
//            long convId = cr.getLong(cr.getColumnIndex("conv_id"));
//            String convTitle = cr.getString(cr.getColumnIndex("conv_title"));
//            conversations.add(new Conversation(convId, convTitle));
//        }
//
//        // إعداد RecyclerView لعرض المحادثات
//        RecyclerView recyclerView = binding.recyclerViewConversations;
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        ConversationAdapter adapter = new ConversationAdapter(conversations, new ConversationAdapter.OnConversationClickListener() {
//            @Override
//            public void onConversationClick(long convId) {
//                // افتح ChatFragment مع الـ conv_id
//                openChatFragment(convId);
//            }
//        });
//        recyclerView.setAdapter(adapter);
//
//        return root;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//
//    private void openChatFragment(long convId) {
//        // إنشاء Bundle لتمرير الـ conv_id
//        Bundle bundle = new Bundle();
//        bundle.putLong("conv_id", convId);
//
//        // فتح الـ ChatFragment يدويًا باستخدام FragmentTransaction
//        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//        ChatFragment chatFragment = new ChatFragment();
//        chatFragment.setArguments(bundle); // تمرير البيانات (conv_id) للـ ChatFragment
//        transaction.replace(R.id.mobile_navigation, chatFragment); // استبدال الـ Fragment الحالي بـ ChatFragment
//        transaction.addToBackStack(null); // لضمان إمكانية الرجوع
//        transaction.commit();
//    }
//}

public class GalleryFragment extends Fragment {
    private ChatDatabase my_db;
    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String u_email = user.getEmail();

        my_db = new ChatDatabase(getContext());

        Cursor cr = my_db.getAllConversations(u_email);
        List<Conversation> conversations = new ArrayList<>();
        while (cr.moveToNext()) {
            long convId = cr.getLong(cr.getColumnIndex("conv_id"));
            String convTitle = cr.getString(cr.getColumnIndex("conv_title"));
            conversations.add(new Conversation(convId, convTitle));
        }

        RecyclerView recyclerView = binding.recyclerViewConversations;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ConversationAdapter adapter = new ConversationAdapter(conversations, new ConversationAdapter.OnConversationClickListener() {
            @Override
            public void onConversationClick(long convId) {
                openChatFragment(convId);
            }
        });
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openChatFragment(long convId) {

        Bundle bundle = new Bundle();
        bundle.putLong("conv_id", convId);

        NavController navController = NavHostFragment.findNavController(GalleryFragment.this);
        navController.navigate(R.id.action_galleryFragment_to_chatFragment, bundle);
    }
}
