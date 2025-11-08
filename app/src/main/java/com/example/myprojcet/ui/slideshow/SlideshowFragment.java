package com.example.myprojcet.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myprojcet.R;
import com.example.myprojcet.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private RadioGroup theme_radioGroup;
    private RadioButton lRadioBtn,dRadioBtn,sys_RadioBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        theme_radioGroup = root.findViewById(R.id.radio_group);

        lRadioBtn = root.findViewById(R.id.radio_light);
        dRadioBtn = root.findViewById(R.id.radio_dark);
        sys_RadioBtn = root.findViewById(R.id.radio_system);

        int night_mode = AppCompatDelegate.getDefaultNightMode();

        if(night_mode == AppCompatDelegate.MODE_NIGHT_NO)
            lRadioBtn.setChecked(true);
        else if (night_mode == AppCompatDelegate.MODE_NIGHT_YES){

            dRadioBtn.setChecked(true);

        }
        else
            sys_RadioBtn.setChecked(true);

        theme_radioGroup.setOnCheckedChangeListener( (RadioGroup group, int checkedId) -> {
                if(checkedId == R.id.radio_light)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                else if(checkedId == R.id.radio_dark)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if(checkedId == R.id.radio_system)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });


//        final TextView textView = binding.textSlideshow;
//        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
         return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}