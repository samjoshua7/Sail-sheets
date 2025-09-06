package com.blazingpirates.sailsheets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blazingpirates.sailsheets.ui.login.LoginActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private static final int CURRENT_APP_VERSION = 2;

    private String currentSemValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView launchAd = findViewById(R.id.launch_ad);
        launchAd.bringToFront();

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        if (!isInternetAvailable()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Toast.makeText(this, "No Internet! Offline mode entered.", Toast.LENGTH_SHORT).show();
                goToHome();
            } else {
                showErrorDialog("Internet connection required!\n\n1. Seems like you logged out from the app.\n2. Please connect and reopen the app.");
            }
            return;
        }

        // 🌐 Internet Available → Load launcher image
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("launcher_url").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                if (url != null) {
                    Glide.with(MainActivity.this)
                            .load(url)
                            .placeholder(R.drawable.launcher)
                            .into(launchAd);
                } else {
                    launchAd.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                launchAd.setVisibility(View.GONE);
            }
        });

        new Handler().postDelayed(this::checkAppStatusAndVersion, 2000);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        }
    }

    private void checkAppStatusAndVersion() {
        rootRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();

                String appStatus = snapshot.child("app_status").getValue(String.class);
                String leastVersionStr = snapshot.child("app_least_ver").getValue(String.class);
                String semValue = snapshot.child("current_sem").getValue(String.class);

                if (semValue == null) {
                    showErrorDialog("Unable to fetch current semester info!");
                    return;
                }

                currentSemValue = semValue;

                if ("down".equalsIgnoreCase(appStatus)) {
                    showErrorDialog("App is currently down. Please try again later.\nContact the Admin \"Sam Joshua\"");
                    return;
                }

                try {
                    if (leastVersionStr != null) {
                        int leastVersion = Integer.parseInt(leastVersionStr);
                        if (CURRENT_APP_VERSION < leastVersion) {
                            showErrorDialog("Good News!\nA new version of the Sail Sheets app has been launched! Update to get new features!");
                            return;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Version check failed", Toast.LENGTH_SHORT).show();
                }

                checkUserLogin();

            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            String email = currentUser.getEmail();
            if (email != null) {
                checkPermission(email.toLowerCase());
            } else {
                Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }
    }

    private void checkPermission(String email) {
        String safeEmail = email.replace(".", ",");

        DatabaseReference usersRef = rootRef.child("users").child(safeEmail);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null && (role.equals("admin") || role.equals("permitted"))) {
                        goToHome();
                    } else {
                        showPermissionDialog();
                    }
                } else {
                    showPermissionDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading user data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPermissionDialog() {
        mAuth.signOut();
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Permission Required!")
                .setMessage("You need permission from the admin to use this app.\n\nKindly contact the admin.")
                .setCancelable(false)
                .setPositiveButton("Close App", (dialog, which) -> finishAffinity())
                .show();
    }

    private void showErrorDialog(String msg) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Sail Sheets: Error!")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Close App", (dialog, which) -> finishAffinity())
                .show();
    }

    private void goToHome() {
        startActivity(new Intent(MainActivity.this, Home.class));
        finish();
    }
}