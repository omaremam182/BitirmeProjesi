package com.example.myprojcet.ui.home.inner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.text.method.LinkMovementMethod;
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
import java.util.Locale;

import android.text.Html;
import android.widget.TextView;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

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

//         USER MESSAGE - hide buttons
        if (message.isUser()) {
            holder.messageText.setText(message.getText());

            if (holder.copyBtn != null) holder.copyBtn.setVisibility(View.GONE);
            if (holder.voiceBtn != null) holder.voiceBtn.setVisibility(View.GONE);
            return;
        }

        String htmlText= convertMarkdownToHtml(message.getText());



        holder.messageText.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
        holder.messageText.setMovementMethod(LinkMovementMethod.getInstance());

//        holder.messageText.setText(message.getText());


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
                    String text = message.getText();
                    LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();

                    languageIdentifier.identifyLanguage(text)
                            .addOnSuccessListener(languageCode -> {
                                // languageCode is "en", "tr", "fr", etc.
                                if (!languageCode.equals("und")) {
                                    Locale locale = new Locale(languageCode);
                                    tts.setLanguage(locale);
                                    Toast.makeText(context, "Language is :  "+locale.toString(), Toast.LENGTH_SHORT).show();
                                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                                } else {
                                    tts.setLanguage(Locale.getDefault());
                                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                                }
                            })
                            .addOnFailureListener(e -> {
                                tts.setLanguage(Locale.getDefault());
                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                            });
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

    private String convertMarkdownToHtml(String md) {

        for(int i=6; i>=1; i--) {
            String hashes = new String(new char[i]).replace("\0", "#");
            md = md.replaceAll("(?m)^" + hashes + " (.+)$", "<h" + i + ">$1</h" + i + ">");
        }

        // Bold, Italic, Strikethrough
        md = md.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        md = md.replaceAll("\\*(.+?)\\*", "<i>$1</i>");
        md = md.replaceAll("~~(.+?)~~", "<strike>$1</strike>");

        // Inline code
        md = md.replaceAll("`([^`]+?)`", "<code>$1</code>");

        // Code blocks
        md = md.replaceAll("(?s)```(.+?)```", "<pre>$1</pre>");


        md = md.replaceAll("(?m)^([-*]{3,})$", "<hr>");

        md = md.replaceAll("\\[([^\\]]+)]\\(([^)]+)\\)", "<a href=\"$2\">$1</a>");

        md = md.replaceAll("(?m)^\\- (.+)$", "<li>$1</li>");
        md = md.replaceAll("(?m)^\\d+\\. (.+)$", "<li>$1</li>");

        if(md.contains("<li>")) {
            if(md.matches("(?s).*<li>.*\\d+\\. .*<li>.*")) {
                md = md.replaceAll("(?s)((?:<li>.*</li>\\s*)+)", "<ol>$1</ol>");
            } else {
                md = md.replaceAll("(?s)((?:<li>.*</li>\\s*)+)", "<ul>$1</ul>");
            }
        }

        String[] lines = md.split("\n");
        StringBuilder sb = new StringBuilder();
        boolean inTable = false;
        boolean oddRow = true;

        for(String line : lines) {
            if(line.matches("^\\|.*\\|$")) {
                if(!inTable) {
                    sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");
                    inTable = true;
                }
                if(line.matches("^\\|[- ]+\\|$")) continue;
                String[] cells = line.split("\\|");
                sb.append("<tr style=\"background-color:")
                        .append(oddRow ? "#f9f9f9" : "#ffffff")
                        .append(";\">");
                for(int i = 1; i < cells.length; i++) {
                    sb.append("<td>").append(cells[i].trim()).append("</td>");
                }
                sb.append("</tr>");
                oddRow = !oddRow;
            } else {
                if(inTable) {
                    sb.append("</table>");
                    inTable = false;
                }
                sb.append(line).append("<br>");
            }
        }
        if(inTable) sb.append("</table>");

        return sb.toString();
    }
}
