
package com.shoaib.ai;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    LinearLayout messages;
    EditText input;
    SpeechRecognizer recognizer;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12, 18, 35));

        TextView title = new TextView(this);
        title.setText("✦ Shoaib AI");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setPadding(20, 25, 20, 20);
        root.addView(title);

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(15, 15, 15, 15);
        scroll.addView(messages);

        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        addMessage("Hello! I am Shoaib AI. How can I help you?", false);

        LinearLayout controls = new LinearLayout(this);
        controls.setPadding(10, 10, 10, 15);
        controls.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setHint("Ask me anything...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setSingleLine(true);
        input.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(Color.CYAN)
        );

        controls.addView(input, new LinearLayout.LayoutParams(
            0, -2, 1
        ));

        Button mic = new Button(this);
        mic.setText("🎤");
        controls.addView(mic);

        Button send = new Button(this);
        send.setText("Send");
        controls.addView(send);

        Button voice = new Button(this);
        voice.setText("🔊");
        controls.addView(voice);

        root.addView(controls);
        setContentView(root);

        send.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(message, true);
                input.setText("");
                addMessage("OpenAI connection will be configured next.", false);
            }
        });

        mic.setOnClickListener(v -> startVoice());

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        voice.setOnClickListener(v -> {
            if (tts != null) {
                tts.speak(
                    "Hello, I am Shoaib AI.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "hello"
                );
            }
        });
    }

    void addMessage(String text, boolean user) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(16);
        bubble.setTextColor(Color.WHITE);
        bubble.setPadding(18, 14, 18, 14);
        bubble.setBackgroundColor(user ?
            Color.rgb(35, 95, 170) :
            Color.rgb(35, 48, 70));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                -2, -2
            );
        params.setMargins(5, 8, 5, 8);
        params.gravity = user ?
            Gravity.RIGHT : Gravity.LEFT;

        messages.addView(bubble, params);
    }

    void startVoice() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this,
                "Speech recognition unavailable",
                Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 100
            );
            return;
        }

        if (recognizer != null) recognizer.destroy();

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Toast.makeText(MainActivity.this,
                    "Voice recognition error: " + error,
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data =
                    results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    );

                if (data != null && !data.isEmpty()) {
                    input.setText(data.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle results) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        );

        recognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
