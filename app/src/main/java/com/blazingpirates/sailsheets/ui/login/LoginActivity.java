package com.blazingpirates.sailsheets.ui.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.blazingpirates.sailsheets.Home;
import com.blazingpirates.sailsheets.MyApp;
import com.blazingpirates.sailsheets.R;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEdit, passwordEdit, fullnameEdit;
    private Button loginBtn;
    private ProgressBar progressBar;
    private Switch toggleSwitch;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private boolean isRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.pirate_brown_dark));
//        getWindow().getDecorView().setSystemUiVisibility(0); // white icons

//        if (Build.VERSION.SDK_INT >= 21) {
//            Window window = this.getWindow();
////            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
////            window.setStatusBarColor(ContextCompat.getColor(this, R.color.pirate_brown_dark));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }



        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginBtn = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        fullnameEdit = findViewById(R.id.fullnameEdit); // ⚠️ make sure to add this EditText in XML
        toggleSwitch = findViewById(R.id.switch1);

        mAuth = FirebaseAuth.getInstance();
        String currentSemValue = ((MyApp) getApplication()).getCurrentSem();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        TextView modeText = findViewById(R.id.modeText);

        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            fullnameEdit.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            loginBtn.setText(isChecked ? "Register" : "Login");
            modeText.setText(isChecked ? "Register" : "Login");
        });

        loginBtn.setOnClickListener(view -> doLoginOrRegister());
    }

    private void doLoginOrRegister() {
        String email = emailEdit.getText().toString().trim().toLowerCase();
        String pass = passwordEdit.getText().toString().trim();
        boolean isRegister = toggleSwitch.isChecked();
        String fullname = fullnameEdit.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || (isRegister && fullname.isEmpty())) {
            Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (!isRegister) {
            // 🔐 LOGIN
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> checkPermission(email))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 🆕 REGISTER
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        // save user data in DB with role = "waiting"
                        DatabaseReference userNode = usersRef.child(email.replace(".", ","));
                        userNode.child("role").setValue("waiting");
                        userNode.child("fullname").setValue(fullname)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registered! Waiting for approval.", Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();
                                    progressBar.setVisibility(View.GONE);
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration failed!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void checkPermission(String email) {
        DatabaseReference userNode = usersRef.child(email.replace(".", ","));

        userNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    String fullName = snapshot.child("fullname").getValue(String.class);

                    if ("admin".equals(role) || "permitted".equals(role)) {
                        // 💾 Save to MyApp
//                        goToHome();
//                        ((MyApp) getApplication()).setLoggedUserName(fullName != null ? fullName : "Unknown_login");

                        userNode.child("fullname").get().addOnSuccessListener(nameSnap -> {
                            ((MyApp) getApplication()).setLoggedUserName(nameSnap.getValue(String.class));
                            goToHome();
                        });

                    }
                    /*if ("admin".equals(role) || "permitted".equals(role)) {
                        userNode.child("fullname").get().addOnSuccessListener(nameSnap -> {
                            ((MyApp) getApplication()).setLoggedUserName(nameSnap.getValue(String.class));
                            goToHome();
                        });
                    }*/
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Not approved yet!", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Database error!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void goToHome() {
        startActivity(new Intent(this, Home.class));
        finish();
    }
}