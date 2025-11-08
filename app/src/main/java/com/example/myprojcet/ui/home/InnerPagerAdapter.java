
package com.example.myprojcet.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myprojcet.ui.home.inner.ChatFragment;
import com.example.myprojcet.ui.home.inner.AssistantFragment;

public class InnerPagerAdapter extends FragmentStateAdapter {

    public InnerPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0)
            return new ChatFragment();
        else
            return new AssistantFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
