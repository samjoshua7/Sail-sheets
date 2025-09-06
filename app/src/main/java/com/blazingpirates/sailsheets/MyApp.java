package com.blazingpirates.sailsheets;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.firebase.database.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApp extends Application {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_LOGGED_USERNAME = "logged_user_name";
    private String currentSem = "default";
    private String loggedUserName = "Unknown"; // default value

    @Override
    public void onCreate() {
        super.onCreate();

        // Light mode always
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Load saved username from SharedPrefs
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loggedUserName = prefs.getString(KEY_LOGGED_USERNAME, "Unknown");

        // Load current_sem from Firebase
        FirebaseDatabase.getInstance().getReference("current_sem")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentSem = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public String getCurrentSem() {
        return currentSem;
    }

    public String getLoggedUserName() {
        return loggedUserName;
    }

    public void setLoggedUserName(String name) {
        this.loggedUserName = name;

        // Save to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LOGGED_USERNAME, name);
        editor.apply();
    }

    public void clearLoggedUserName() {
        loggedUserName = "Unknown";
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.remove(KEY_LOGGED_USERNAME);
        editor.apply();
    }
}