package com.soundbox.esBox;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    // 1. Declare the TextView here so the whole class can use it
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.status);
        // Automatically check and request permission when the app starts
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Please enable Notification Access for this app.", Toast.LENGTH_LONG).show();
            // Send the user directly to the Notification Settings screen
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
//        Button btnGrantPermission = findViewById(R.id.btnGrantPermission);

//        btnGrantPermission.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isNotificationServiceEnabled()) {
//                    // Open the settings page where the user can grant access
//                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
//                } else {
//                    Toast.makeText(MainActivity.this, "Permission already granted! App is listening.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check status every time the user returns to the app
        if (isNotificationServiceEnabled()) {
            // You can update a TextView here to show the user that the service is Active
            if (isNotificationServiceEnabled()) {
                tvStatus.setText("Activated");
                // Optional: change text color to green
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Not Activated");
                // Optional: change text color to red
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }

    private boolean isNotificationServiceEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        return packageNames.contains(getPackageName());
    }
}