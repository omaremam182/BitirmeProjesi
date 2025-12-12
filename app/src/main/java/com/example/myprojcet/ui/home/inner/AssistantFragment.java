package com.example.myprojcet.ui.home.inner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myprojcet.R;
import com.example.myprojcet.deviceControl.ContactResolver;
import com.example.myprojcet.deviceControl.DeviceAlarm;
import com.example.myprojcet.deviceControl.FlashHandler;
import com.example.myprojcet.deviceControl.GoogleHandler;
import com.example.myprojcet.deviceControl.MapsHandler;
import com.example.myprojcet.deviceControl.PhoneCallHandler;
import com.example.myprojcet.deviceControl.SmsSender;
import com.example.myprojcet.deviceControl.WhatsAppOperationsHandler;
import com.example.myprojcet.deviceControl.ileriZamanliIslem;
import com.example.myprojcet.deviceControl.BluetoothControl;
import com.example.myprojcet.deviceControl.WifiControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AssistantFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 101;
    private static final int SMS_PERMISSION_REQUEST = 102;
    private static final int READ_CONTACTS_REQUEST = 103;
    private static final int PERMISSION_REQUEST_CALL = 105;



    private TextToSpeech tts;
    private ImageButton listenButton;
    private SmsSender smsSender;
    private ContactResolver contactResolver;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_assistant, container, false);

        listenButton = root.findViewById(R.id.assistant_listen_button);

        initTextToSpeech();

        smsSender = new SmsSender(requireContext());
        contactResolver = new ContactResolver(requireContext(),requireActivity());
        listenButton.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startSpeechToText();
            } else {
                requestAudioPermission();

            }
        });

        return root;
    }

    public void initTextToSpeech() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(new Locale("tr", "TR"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getContext(), "Telefonunuz Türkçe dilini desteklemiyor!", Toast.LENGTH_SHORT).show();
                } else {

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

         if (recognizedText.contains("alarm")) {
            if(recognizedText.contains("iptal") || recognizedText.contains("sil") || recognizedText.contains("kapat")){
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                getContext().startActivity(intent);
                return;
            }
            DeviceAlarm  deviceAlarm = new DeviceAlarm(getContext());
            deviceAlarm.setDeviceAlarm(6,20,"This alarm has been created by myproject");


        } else if (recognizedText.contains("google") || recognizedText.contains("tarayıcı")) {
//             GoogleHandler googleHandler = new GoogleHandler(requireContext());
//             googleHandler.googleBrowserSearch(getTheQuery(recognizedText.split(" ")));
             return;

         } else if (recognizedText.contains("zamanlayici") || recognizedText.contains("zamanlayıcı") || recognizedText.contains("timer") ) {
            if(recognizedText.contains("kapat") || recognizedText.contains("durdur")
                    || recognizedText.contains("iptal") || recognizedText.contains("sil")) {
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_TIMERS);
                getContext().startActivity(intent);
                return;
            }
            DeviceAlarm timer = new DeviceAlarm(getContext());
            timer.setDeviceTimer(90, "My Project's Timer");

        } else if (recognizedText.contains("konum") || recognizedText.contains("lokasyon") ) {
             MapsHandler mapsHandler = new MapsHandler(requireContext());
             String dest = getTheDestinaton(recognizedText.split(" "));
             if(dest != null){
                 mapsHandler.search(dest);
             }

         } else if (recognizedText.contains("flaş")||recognizedText.contains("fener")||recognizedText.contains("flash")) {
             FlashHandler flashHandler = new FlashHandler(requireContext());
             if (recognizedText.contains("yak")||recognizedText.contains("aç")|| recognizedText.contains("çalıştır")){
                 flashHandler.turnFlashlightOn();
             }else if(recognizedText.contains("söndür")||recognizedText.contains("kapat")){
                 flashHandler.turnFlashlightOff();
             }

         } else if ((recognizedText.contains("mesaj")||recognizedText.contains("sms")) && recognizedText.contains("kişi")) {
             String contact = getTheContact((recognizedText.split(" ")));
             if(recognizedText.contains("whatsapp")){
                 sendWhatsappMessage(contact,"This is a test , please do not reply");
             }
             else {
                 if (contact != null)
                     sendSmsToContact(contact, "This is a test");
                 else
                     Toast.makeText(requireContext(), "Kişi bulunamadı", Toast.LENGTH_LONG).show();
             }
         } else if (recognizedText.contains("ara") && recognizedText.contains("kişi")) {
             String contact = getTheContact((recognizedText.split(" ")));

             if(contact != null) {
//                 if(recognizedText.contains("whatsapp")){
//                     boolean isVideoCall= recognizedText.contains("video");
//                     callByWhatsapp(contact,isVideoCall);
//                      return;

                 makePhoneCall(contact);

             }else
                 Toast.makeText(requireContext(),"Kişi bulunamadı",Toast.LENGTH_LONG).show();


         } else if (recognizedText.contains("aç") || recognizedText.contains("başlat")) {
            openAppByName(recognizedText);
        }
         else {
            speakText("Üzgünüm, bu komutu anlayamadım.");
        }
    }

    private String getTheQuery(String[] s) {
        for (int i = 0; i < s.length; i++) {
            if(s[i].contains("Google") ){
                return s[++i] + s[++i];
            }
        }
        return null;
    }

    private String getTheDestinaton(String[] arr) {
        for(int i = 0 ;i < arr.length;i++){
            if(arr[i].contains("konum") || arr[i].contains("lokasyon")){
                return arr[i-2]+arr[i-1]
                        ;
            }
        }
        return null;
    }

    private void sendWhatsappMessage(String contact, String message) {

        String phone = contactResolver.findNumberByContact(contact);
        if (!(phone == null || phone.isEmpty())) {
            WhatsAppOperationsHandler whatsAppOperationsHandler = new WhatsAppOperationsHandler(requireContext());
            whatsAppOperationsHandler.sendWhatsAppMessage(phone, message);
        }
    }
    private void callByWhatsapp(String contact, boolean isVideoCall) {

        String phone = contactResolver.findNumberByContact(contact);
        if (!(phone == null || phone.isEmpty())) {
            WhatsAppOperationsHandler whatsAppOperationsHandler = new WhatsAppOperationsHandler(requireContext());
            whatsAppOperationsHandler.makeWhatsAppCall(phone, isVideoCall);
        }
    }

    private String getTheContact( String[] words) {
        for(int i = 0;i<words.length;i++){
            if(words[i].contains("kişi")){
                return words[--i];
            }
        }
        return null;
    }


    private void openAppByName(String spokenText) {
        PackageManager pm = requireContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(
                PackageManager.GET_META_DATA | PackageManager.MATCH_ALL);



        if(spokenText.contains("wi-fi")) {
            WifiControl wifiControl = new WifiControl(getContext());
            wifiControl.toggleWifi(true);
            return;

            // Turn Wi-Fi OFF
//            wifiControl.toggleWifi(false);
        } else if (spokenText.contains("bluetooth")) {
            BluetoothControl bC = new BluetoothControl(getContext());
            bC.toggleBluetooth(true);
            return;

        } else if (spokenText.contains("ileri dogru islem")) {
            ileriZamanliIslem alarmReceiver = new ileriZamanliIslem(getContext());
            alarmReceiver.setExactAlarm(4,49);
            return;

        }
        for (ApplicationInfo appInfo : apps) {
            String appName = pm.getApplicationLabel(appInfo).toString().toLowerCase(Locale.ROOT);

            if (spokenText.contains(appName)) {
                Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
//                    speakText(appName + " açılıyor.");
                    return;
                }
            }
        }

        speakText("Uygulama bulunamadı: " + spokenText);
    }


    private void sendSmsDirect(String number, String message) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            requestSmsPermission();
            return;
        }

        smsSender.sendSms(number, message);
    }
    private void sendSmsToContact(String contactName, String message) {

        String phone = contactResolver.findNumberByContact(contactName);

        if (phone == null) {
//            Toast.makeText(getContext(), "Contact not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        sendSmsDirect(phone, message);
    }


    private void makePhoneCall(String contact) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it.
            requestCallPermission();
            return;
        }
            // Permission is already granted, proceed with the call.
        PhoneCallHandler phoneCallHandler = new PhoneCallHandler(requireContext());
        phoneCallHandler.callPhoneNumber(contactResolver.findNumberByContact(contact));
    }


    public void speakText(String text) {
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
        else if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS permission granted!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getContext(), "SMS permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
       else if (requestCode == READ_CONTACTS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Contacts permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Contacts permission denied!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission denied. Inform the user or disable the feature.
                Toast.makeText(requireContext(), "Call permission denied. Cannot make calls.", Toast.LENGTH_SHORT).show();
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
    private void requestSmsPermission() {
        requestPermissions(
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST
        );
    }
//    private void requestReadContactsPermission() {
//        requestPermissions(
//                new String[]{Manifest.permission.READ_CONTACTS},
//                READ_CONTACTS_REQUEST
//        );
//    }

    private void requestCallPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CALL_PHONE},
                PERMISSION_REQUEST_CALL);
    }

}
