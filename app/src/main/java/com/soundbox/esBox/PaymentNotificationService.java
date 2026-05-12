package com.soundbox.esBox;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import android.os.Handler;
import android.os.Looper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentNotificationService extends NotificationListenerService implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    // Target app package name to listen
    private final String targetApps = "com.tngdigital.ewallet";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Text-to-Speech
        tts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US); // Set to your preferred language/locale
            tts.setSpeechRate(0.6f);
        } else {
            Log.e("PaymentNotification", "TTS Initialization Failed!");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        // 1. Check if the notification is from our target e-wallet apps
        if (targetApps.equals("com.tngdigital.ewallet")) {
            Bundle extras = sbn.getNotification().extras;

            // Safely get title and text
            CharSequence titleChars = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence textChars = extras.getCharSequence(Notification.EXTRA_TEXT);

            String title = titleChars != null ? titleChars.toString() : "";
            String text = textChars != null ? textChars.toString() : "";

            Log.d("PaymentNotification", "App: " + packageName + ", Title: " + title + ", Text: " + text);

            // 2. Check if the notification indicates money received
//            if (text.toLowerCase().contains("received") || text.toLowerCase().contains("has transferred") || title.toLowerCase().contains("received")) {
              if (text.toLowerCase().contains("已收到") || title.toLowerCase().contains("支入")) {
                // 3. Extract the amount using Regex
//                String amount = extractAmount(text);
//                Log.d("PaymentNotification", "Total amount extracted: " + amount);
//
//                if (amount != null) {
//                    String speakText = "Payment received: " + amount;
//                    speak(speakText);
//                }

                // 3. Extract and format as Ringgit and Sen (new)
                String spokenAmount = extractAmount(text);

                if (spokenAmount != null) {
                    // This will now say: "Payment received: 15 Ringgit and 50 Sen"
                    String speakText = "Payment received: " + spokenAmount;
//                    speak(speakText);
                    // Create a Handler to delay the action
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // This code will run after the delay
//                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PaymentAnnouncement");
                            speak(speakText);
                        }
                    }, 1000); // 1000 milliseconds = 1 second delay
                    cancelNotification(sbn.getKey());
                }
            }
        }
    }

//    private String extractAmount(String text) {
//        // Example Regex: Looks for "RM" followed by spaces and numbers/decimals
//        // Modify "RM" to match your currency if needed.
//        Pattern pattern = Pattern.compile("(RM|\\$)\\s?\\d+(\\.\\d{1,2})?");
//        Matcher matcher = pattern.matcher(text);
//        if (matcher.find()) {
//            return matcher.group();
//        }
//        return null;
//    }

    private String extractAmount(String text) {
        // Regex looks for "RM", an optional space, then captures the whole numbers (Group 1)
        // and optionally captures the decimal numbers (Group 3)
        Pattern pattern = Pattern.compile("RM\\s?(\\d+)(\\.(\\d{1,2}))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String ringgitPart = matcher.group(1); // The number before the decimal
            String senPart = matcher.group(3);     // The number after the decimal

            StringBuilder spokenAmount = new StringBuilder();

            // 1. Handle the Ringgit part (Ignore if it's 0)
            if (ringgitPart != null && !ringgitPart.equals("0")) {
                spokenAmount.append(ringgitPart).append(" Ringgit");
            }

            // 2. Handle the Sen part (Ignore if it's null, "0", or "00")
            if (senPart != null && !senPart.equals("00") && !senPart.equals("0")) {

                // Fix for single-digit decimals (e.g., .5 means 50 sen)
                if (senPart.length() == 1) {
                    senPart += "0";
                }

                // Add "and" if we already have Ringgit
                if (spokenAmount.length() > 0) {
                    spokenAmount.append(" and ");
                }

                // Convert to integer to drop leading zeros (e.g., "05" becomes 5)
                int senValue = Integer.parseInt(senPart);
                spokenAmount.append(senValue).append(" Sen");
            }

            // Fallback just in case it matched RM 0.00
            if (spokenAmount.length() == 0) {
                return "0 Ringgit";
            }

            return spokenAmount.toString();
        }
        return null; // Return null if "RM" wasn't found in the text
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PaymentAnnouncement");
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if necessary
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