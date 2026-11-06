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
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.*;
import android.content.res.ColorStateList;
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
        root.setPadding(8, 5, 8, 5);

        TextView title = new TextView(this);
        title.setText("✦ Shoaib AI");
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setPadding(10, 8, 10, 8);
        root.addView(title);

        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(6, 6, 6, 6);
        scroll.addView(messages);

        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        addMessage("Hello! I am Shoaib AI. How can I help?", false);

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(2, 4, 2, 4);

        input = new EditText(this);
        input.setHint("Ask anything...");
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setPadding(8, 5, 8, 5);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setBackgroundTintList(
            ColorStateList.valueOf(Color.CYAN)
        );

        controls.addView(input, new LinearLayout.LayoutParams(
            0, 42, 1
        ));

        Button mic = new Button(this);
        mic.setText("🎤");
        mic.setTextSize(12);
        controls.addView(mic, new LinearLayout.LayoutParams(
            48, 42
        ));

        Button send = new Button(this);
        send.setText("➤");
        send.setTextSize(16);
        controls.addView(send, new LinearLayout.LayoutParams(
            48, 42
        ));

        Button voice = new Button(this);
        voice.setText("🔊");
        voice.setTextSize(12);
        controls.addView(voice, new LinearLayout.LayoutParams(
            48, 42
        ));

        root.addView(controls);
        setContentView(root);

        send.setOnClickListener(v -> {
            String message = input.getText().toString().trim();

            if (!message.isEmpty()) {
                addMessage(message, true);
                input.setText("");
                addMessage(
                    "OpenAI connection will be configured next.",
                    false
                );
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
        bubble.setTextSize(14);
        bubble.setTextColor(Color.WHITE);
        bubble.setPadding(12, 8, 12, 8);

        bubble.setBackgroundColor(user
            ? Color.rgb(35, 95, 170)
            : Color.rgb(35, 48, 70));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-2, -2);

        params.setMargins(3, 4, 3, 4);
        params.gravity = user ? Gravity.RIGHT : Gravity.LEFT;

        messages.addView(bubble, params);
    }

    void startVoice() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition unavailable",
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
            public void onReadyForSpeech(Bundle b) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float r) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() {}

            public void onError(int error) {
                Toast.makeText(MainActivity.this,
                    "Voice error: " + error,
                    Toast.LENGTH_SHORT).show();
            }

            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                );

                if (data != null && !data.isEmpty()) {
                    input.setText(data.get(0));
                }
            }

            public void onPartialResults(Bundle b) {}
            public void onEvent(int e, Bundle b) {}
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
