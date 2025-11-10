package com.example.myprojcet.ui.home.inner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myprojcet.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AssistantFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 101;

    private TextToSpeech tts;
    private ImageButton listenButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_assistant, container, false);

        listenButton = root.findViewById(R.id.assistant_listen_button);

        initTextToSpeech();

        listenButton.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startSpeechToText();
            } else {
                requestAudioPermission();
                startSpeechToText();
            }
        });

        return root;
    }

    private void initTextToSpeech() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("tr", "TR"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getContext(), "Türkçe dili desteklenmiyor!", Toast.LENGTH_SHORT).show();
                } else {

                    // speakText("Merhaba, size nasıl yardımcı olabilirim?");
                }
            }
        });
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşmaya başlayabilirsiniz...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cihaz konuşma tanımayı desteklemiyor!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == getActivity().RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                // Toast.makeText(getContext(), "Duydum: " + recognizedText, Toast.LENGTH_LONG).show();

               // speakText("Söyledikleriniz şu : " + recognizedText);
                handleCommand(recognizedText);
            }
        }
    }
    private void handleCommand(String recognizedText) {
        recognizedText = recognizedText.toLowerCase(Locale.ROOT);

        if (recognizedText.contains("aç") || recognizedText.contains("başlat")) {
            openAppByName(recognizedText);
        } else {
            speakText("Üzgünüm, bu komutu anlayamadım.");
        }
    }
    private void openAppByName(String spokenText) {
        PackageManager pm = requireContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(
                PackageManager.GET_META_DATA | PackageManager.MATCH_ALL);

        String lowerText = spokenText.toLowerCase(Locale.ROOT);

        for (ApplicationInfo appInfo : apps) {

            String appName = pm.getApplicationLabel(appInfo).toString().toLowerCase(Locale.ROOT);

            if (lowerText.contains(appName)) {
                Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    speakText(appName + " açılıyor.");
                    return;
                }
            }
        }

        speakText("Uygulama bulunamadı: " + spokenText);
    }

    private void speakText(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText();
            } else {
                Toast.makeText(getContext(), "Audio permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
