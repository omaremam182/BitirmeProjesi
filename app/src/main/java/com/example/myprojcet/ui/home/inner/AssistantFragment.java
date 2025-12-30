package com.example.myprojcet.ui.home.inner;

import android.Manifest;
import java.util.function.Consumer;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myprojcet.BuildConfig;
import com.example.myprojcet.R;
import com.example.myprojcet.deviceControl.ContactResolver;
import com.example.myprojcet.deviceControl.DeviceAlarm;
import com.example.myprojcet.deviceControl.FlashHandler;
import com.example.myprojcet.deviceControl.MapsHandler;
import com.example.myprojcet.deviceControl.PhoneCallHandler;
import com.example.myprojcet.deviceControl.SmsSender;
import com.example.myprojcet.deviceControl.WhatsAppOperationsHandler;
import com.example.myprojcet.deviceControl.ileriZamanliIslem;
import com.example.myprojcet.deviceControl.BluetoothControl;
import com.example.myprojcet.deviceControl.WifiControl;
import com.example.myprojcet.manageRequests.*;

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
import retrofit2.Call;

public class AssistantFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 101;
    private static final int SMS_PERMISSION_REQUEST = 102;
    private static final int READ_CONTACTS_REQUEST = 103;
    private static final int PERMISSION_REQUEST_CALL = 105;
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = BuildConfig.GROQ_API_KEY;

    private OkHttpClient client = new OkHttpClient();
    private String CALL_systemPrompt = "Sen bir Android çağrı asistanısın.\n" +
            "Görevin, kullanıcının sesli arama komutundan aranacak kişi adını veya telefon numarasını çıkarmaktır.\n" +
            "\n" +
            "Kurallar:\n" +
            "- Eğer aranacak kişi adı açıkça geçiyorsa, \"contact\" alanını doldur.\n" +
            "- Eğer ambulans, polis, itfaiye gibi sabit bir numara isteniyorsa, \"number\" alanını doldur.\n" +
            "- Eğer kişi adı veya numara tespit edilemiyorsa her iki alanı da null yap.\n" +
            "- Açıklama, yorum veya ek metin ekleme.\n" +
            "- Sadece JSON çıktısı üret.\n" +
            "\n" +
            "Çıktı formatı (zorunlu):\n" +
            "{\n" +
            "  \"contact\": string | null,\n" +
            "  \"number\": string | null\n" +
            "}\n" +
            "\n" +
            "Aşağıdaki örnekler sadece açıklama amaçlıdır, çıktı olarak ASLA tekrar edilmemelidir.\n" +
            "\n" +
            "Örnekler:\n" +
            "\n" +
            "Metin: Annemi ara\n" +
            "Cevap:\n" +
            "{\n" +
            "  \"contact\": \"Anne\",\n" +
            "  \"number\": null\n" +
            "}\n" +
            "\n" +
            "Metin: Ambulansı ara\n" +
            "Cevap:\n" +
            "{\n" +
            "  \"contact\": null,\n" +
            "  \"number\": \"112\"\n" +
            "}\n" +
            "\n" +
            "Metin: Ali'yi ara\n" +
            "Cevap:\n" +
            "{\n" +
            "  \"contact\": \"Ali\",\n" +
            "  \"number\": null\n" +
            "}\n" +
            "\n" +
            "Metin: Birini ara\n" +
            "Cevap:\n" +
            "{\n" +
            "  \"contact\": null,\n" +
            "  \"number\": null\n" +
            "}\n";
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
                sendCommandToAssistantAPI(recognizedText.toLowerCase(Locale.ROOT).strip());
            }
        }
    }
    private void sendCommandToAssistantAPI(String recognizedText){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        TextRequest request = new TextRequest(recognizedText);

        apiService.predict(request).enqueue(new retrofit2.Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call,
                                   retrofit2.Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    PredictionResponse result = response.body();
                    String commandClass = result.getLabel().toLowerCase(Locale.ROOT);
                    int classId = result.getClass_id();
                    excuteCommand(classId,recognizedText);

                    Toast.makeText(requireContext(), commandClass, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                Log.e("API", "Error", t);
            }
        });

    }
    private void toggle_Bluetooth(boolean enabled){
        BluetoothControl bC = new BluetoothControl(getContext());
        bC.toggleBluetooth(enabled);
    }
    public void excuteCommand(int predictedClassNumber, String text) {
        switch (predictedClassNumber) {

            case 0:
                // BT_OFF
                toggle_Bluetooth(false);
                break;

            case 1:
                // BT_ON
                toggle_Bluetooth(true);
                break;

            case 2:
                // CALL
                new Thread(() -> {
                    Map<String, String> result = getTheContactOrNumber(text);

                    String contact = result.get("contact");
                    String number = result.get("number");
                    Log.d("Contact","Contact : " +contact);

                    if (contact != null) {
                        makePhoneCall(contact,false);
                    } else if (number != null) {
                        makePhoneCall(number, true);
                    }
                }).start();
                break;

            case 3:
                // FLASH_OFF
                toggleFlash(false);
                break;

            case 4:
                // FLASH_ON
                toggleFlash(true);
                break;

            case 5:
                // MAPS_SEARCH
                String dest = getTheDestinaton(text.split(" "));
                searchInMap(dest);
                break;

            case 6:
                // OPEN_APP
                openApp(text);
                break;

            case 7:
                // REMOVE_ALARM
                toggleAlarm(false);
                break;

            case 8:
                // REMOVE_TIMER
                toggleTimer(false);
                break;

            case 9:
                // SET_ALARM
                toggleAlarm(true);
                break;

            case 10:
                // SET_TIMER
                toggleTimer(true);
                break;

            case 11:
                // SMS_SEND
                sendCommandToGroq(text, "msg", result -> {
                    // This code runs on the main thread
                    System.out.println("Result: " + result);

                    if (result.containsKey("alici") && result.containsKey("metin")) {
                        String myContact = result.get("alici");

                        if (myContact != null) {
                            sendSmsToContact(myContact, result.get("metin"));
                        }else
                            Toast.makeText(requireContext(), "Kişi bulunamadı", Toast.LENGTH_LONG).show();
                }});

                break;

            case 12:
                // TOGGLE_WIFI
                toggleWi_Fi(true);
                break;

            case 13:
                // WHATSAPP_SEND

                sendCommandToGroq(text, "msg", result -> {
                    // This code runs on the main thread
                    System.out.println("Result Whatsapp: " + result);

                    if (result.containsKey("alici") && result.containsKey("metin")) {
                        String myContact = result.get("alici");

                        if (myContact != null) {
                            sendWhatsappMessage(myContact, result.get("metin"));
                        }else
                            Toast.makeText(requireContext(), "Kişi bulunamadı", Toast.LENGTH_LONG).show();
                    }});
                break;

            default:
                // UNKNOWN_COMMAND
                break;
        }
    }

    private Map<String,String> getTheContactOrNumber(String userMessage){
        Map<String, String> aranacakKisi = new HashMap<>();
        aranacakKisi.put("contact", null);
        aranacakKisi.put("number", null);

        try {

                JSONObject json = new JSONObject();
                json.put("model", "openai/gpt-oss-20b");

                JSONArray messages = new JSONArray();

                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", CALL_systemPrompt);
                messages.put(systemMsg);

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                messages.put(userMsg);

                json.put("messages", messages);

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
                if (!response.isSuccessful() || response.body() == null) {
                    return aranacakKisi; // default null'larla döner
                }

                String responseBody = response.body().string();

                JSONObject jsonResponse = new JSONObject(responseBody);
                String reply = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();

                JSONObject result = new JSONObject(reply);

                if (!result.isNull("contact")) {
                    aranacakKisi.put("contact", result.getString("contact"));
                }

                if (!result.isNull("number")) {
                    aranacakKisi.put("number", result.getString("number"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return aranacakKisi;
        }

    private void sendCommandToGroq(String userMessage, String type, OnSmsParsedListener listener) {
        new Thread(() -> {
            Map<String, String> sonCevap = new HashMap<>();
            try {
                // 1️⃣ System prompt
                String SMS_systemPrompt = "Kullanıcı bir SMS gönderme komutu verdi.\n" +
                        "Aşağıdaki metinden alıcıyı ve mesaj içeriğini çıkar.\n\n" +
                        "Şema:\n" +
                        "{\n" +
                        "  \"recipient\": string | null,\n" +
                        "  \"message\": string | null\n" +
                        "}\n\n" +
                        "Kurallar:\n" +
                        "- Çıktı SADECE geçerli JSON olsun.\n" +
                        "- Eğer bilgi yoksa null yaz.\n" +
                        "- Yorum, açıklama veya ek metin ekleme.\n" +
                        "- message alanı, alıcıya gönderilecek DOĞRUDAN mesaj metni olmalı.\n" +
                        "- \"yaz\", \"söyle\", \"ilet\", \"de\" gibi fiiller message alanına dahil edilmemeli.\n" +
                        "- Dolaylı anlatım varsa (\"… olduğunu / … yapacağını\"), message alanında doğrudan cümleye dönüştür.\n\n" +
                        "Örnekler:\n\n" +
                        "Metin: Anneme eve geç geleceğimi yaz\n" +
                        "{\n" +
                        "  \"recipient\": \"Anne\", \n" +
                        "  \"message\": \"Eve geç geleceğim\"\n" +
                        "}\n\n" +
                        "Metin: Ali’ye toplantının iptal olduğunu söyle\n" +
                        "{\n" +
                        "  \"recipient\": \"Ali\",\n" +
                        "  \"message\": \"Toplantı iptal oldu\"\n" +
                        "}\n\n" +
                        "Metin: Ahmede whatsapptan yaz\n" +
                        "{\n" +
                        "  \"recipient\": \"Ahmet\",\n" +
                        "  \"message\": \"null\"\n" +
                        "}";

                // 2️⃣ Model için JSON oluştur
                JSONObject json = new JSONObject();
                json.put("model", "openai/gpt-oss-20b");

                JSONArray messages = new JSONArray();

                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", SMS_systemPrompt);
                messages.put(systemMsg);

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                messages.put(userMsg);

                json.put("messages", messages);

                // 3️⃣ Request body
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

                // 4️⃣ Send request
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JSONObject jsonResponse = new JSONObject(responseBody);
                String reply = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 5️⃣ Parse JSON
                JSONObject smsData = new JSONObject(reply);
                String recipient = smsData.optString("recipient", "null");
                String message = smsData.optString("message", "null");

                if (!recipient.equals("null")) sonCevap.put("alici", recipient);
                if (!message.equals("null")) sonCevap.put("metin", message);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            // 6️⃣ Return result on main thread
            new Handler(Looper.getMainLooper()).post(() -> listener.onResult(sonCevap));

        }).start();
    }

    private void toggleFlash(boolean enable){
        FlashHandler flashHandler = new FlashHandler(requireContext());
        if (enable) {
            flashHandler.turnFlashlightOn();
        } else {
            flashHandler.turnFlashlightOff();
        }
    }
    private void searchInMap(String dest){
        if(dest != null){
            MapsHandler mapsHandler = new MapsHandler(requireContext());
            mapsHandler.search(dest);
        }
    }
    private void openApp(String text){
        PackageManager pm = requireContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(
                PackageManager.GET_META_DATA | PackageManager.MATCH_ALL);

        for (ApplicationInfo appInfo : apps) {
            String appName = pm.getApplicationLabel(appInfo).toString().toLowerCase(Locale.ROOT);

            if (text.contains(appName)) {
                Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    return;
                }
            }
        }
        speakText("Uygulama bulunamadı: " + text);
    }

    private void toggleAlarm(boolean enable){
        if(!enable) {
            Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            getContext().startActivity(intent);
        }else {
            DeviceAlarm deviceAlarm = new DeviceAlarm(getContext());
            deviceAlarm.setDeviceAlarm(6, 20, "This alarm has been created by myproject");
        }
    }
    private void toggleTimer(boolean enable){
        if(enable){
            DeviceAlarm timer = new DeviceAlarm(getContext());
            timer.setDeviceTimer(90, "My Project's Timer");
        }else{
            Intent intent = new Intent(AlarmClock.ACTION_SHOW_TIMERS);
            getContext().startActivity(intent);
        }
    }
    private void toggleWi_Fi(boolean enable){
        WifiControl wifiControl = new WifiControl(getContext());
        wifiControl.toggleWifi(enable);
    }

    private String getTheDestinaton(String[] arr) {
        for(int i = 0 ;i < arr.length;i++){
            if(arr[i].contains("konum") || arr[i].contains("lokasyon")){
                return arr[i-2]+arr[i-1];
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
            return;
        }

        sendSmsDirect(phone, message);
    }


    private void makePhoneCall(String contact,boolean isNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it.
            requestCallPermission();
            return;
        }
            // Permission is already granted, proceed with the call.
        PhoneCallHandler phoneCallHandler = new PhoneCallHandler(requireContext());
        if(!isNumber)
        phoneCallHandler.callPhoneNumber(contactResolver.findNumberByContact(contact));
        else
            phoneCallHandler.callPhoneNumber(contact);
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

    private void requestCallPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CALL_PHONE},
                PERMISSION_REQUEST_CALL);
    }
    public interface OnSmsParsedListener {
        void onResult(Map<String, String> result);
    }
}
