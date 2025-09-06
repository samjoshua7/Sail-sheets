package com.blazingpirates.sailsheets;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blazingpirates.sailsheets.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HomeworkAdapter adapter;
    private List<Homework> homeworkList = new ArrayList<>();
    private DatabaseReference dbRef;
    private TextView dateTextView;

    private final SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    private Calendar selectedDate = Calendar.getInstance();
    String currentSemValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle("Sail Sheets");
//        }

        // ✅ ADD THIS SNIPPET TO FIX THE OVERLAP
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply the system bar insets as padding to the view
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);

            // Return CONSUMED to signal that we've handled the insets
            return WindowInsetsCompat.CONSUMED;
        });

        recyclerView = findViewById(R.id.homeworkRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new HomeworkAdapter(homeworkList);
        adapter = new HomeworkAdapter(this, homeworkList); // ✅ Pass context properly
        recyclerView.setAdapter(adapter);

        dateTextView = findViewById(R.id.dateTextView);
        updateDateTextView();

        currentSemValue = ((MyApp) getApplication()).getCurrentSem();

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        fabAdd.setEnabled(isInternetAvailable());
        dateTextView.setEnabled(isInternetAvailable());

        if (!isInternetAvailable()) {
            fabAdd.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            dateTextView.setBackgroundColor(Color.GRAY);
        }

        fabAdd.setOnClickListener(view -> {
            if (isInternetAvailable()) {
                showAddTaskDialog();
            } else {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        });

        dbRef = FirebaseDatabase.getInstance().getReference();

        if (isInternetAvailable()) {
            dbRef.keepSynced(false);
            dbRef.keepSynced(true);
            loadHomeworkForDate(selectedDate);
        } else {
            Toast.makeText(this, "You're offline. Showing cached data.", Toast.LENGTH_SHORT).show();
            loadHomeworkForDate(selectedDate);
        }

        dateTextView.setOnClickListener(view -> showDatePickerDialog());

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean online = isInternetAvailable();
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        TextView dateTextView = findViewById(R.id.dateTextView);

        fabAdd.setEnabled(online);
        dateTextView.setEnabled(online);

        if (!online) {
            fabAdd.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            dateTextView.setBackgroundColor(Color.LTGRAY);
            dateTextView.setTextColor(Color.DKGRAY);
        } else {
            fabAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.pirate_brown_dark));
            dateTextView.setBackgroundColor(Color.TRANSPARENT);
            dateTextView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.info_btn) {
            showUserInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//    private int getStatusBarHeight() {
//        int result = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }

    private void showUserInfoDialog() {
        String name = ((MyApp) getApplication()).getLoggedUserName();
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Unknown";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("About Sail Sheets")
                .setMessage("Name: " + name + "\nEmail: " + email + "\n\nIf you face any error's or come with some suggestions feel free to share with me!")
                .setNegativeButton("Close", null)
                .setPositiveButton("Logout", (d, w) -> {
                    ((MyApp) getApplication()).clearLoggedUserName();
                    FirebaseAuth.getInstance().signOut();

                    startActivity(new Intent(Home.this, LoginActivity.class));
                    finish(); // 👈 prevent back to Home
                })
                .create();

        dialog.show();

        // 🎨 Customize buttons after showing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);

        // Optional: Change background of dialog
        dialog.getWindow().setBackgroundDrawableResource(R.color.pirate_gold_light); // Or your own color
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void updateDateTextView() {
        String dateText = "Works of " + displayDateFormat.format(selectedDate.getTime());
        dateTextView.setText(dateText);
    }

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate.set(year1, month1, dayOfMonth);
                    updateDateTextView();
                    loadHomeworkForDate(selectedDate);
                },
                year, month, day
        );

        Calendar minDate = Calendar.getInstance();
        minDate.set(2025, Calendar.JUNE, 24);
        Calendar maxDate = Calendar.getInstance();

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void loadHomeworkForDate(Calendar date) {
        String dateKey = firebaseDateFormat.format(date.getTime());

        dbRef.child(currentSemValue).child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot subjectsSnapshot) {

                dbRef.child(currentSemValue).child("works").child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot worksSnapshot) {
                        homeworkList.clear();

                        for (DataSnapshot subjectSnap : subjectsSnapshot.getChildren()) {
                            String subjectId = subjectSnap.getKey();
                            String subjectName = subjectSnap.getValue(String.class);

                            String task = "No work today!";
                            String editedBy = "", editedTime = "";

                            DataSnapshot subjectWork = worksSnapshot.child(subjectId);
                            if (subjectWork.exists()) {
                                task = subjectWork.child("task").getValue(String.class);
                                editedBy = subjectWork.child("edited_by").getValue(String.class);
                                editedTime = subjectWork.child("edited_time").getValue(String.class);
                            }
                            homeworkList.add(new Homework(subjectName, task, editedBy, editedTime));
                        }

                        adapter.setOnHomeworkClickListener((position, hw) -> {
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_view_task, null);
                            ((TextView) dialogView.findViewById(R.id.taskText)).setText(hw.getTask());
                            ((TextView) dialogView.findViewById(R.id.editorInfo)).setText("Last edited by: " + hw.getEditedBy());
                            ((TextView) dialogView.findViewById(R.id.editTime)).setText("Edited on: " + hw.getEditedTime());

                            ((TextView) dialogView.findViewById(R.id.subjectTitle)).setText(hw.getSubject());

                            AlertDialog dialog = new AlertDialog.Builder(Home.this)
                                    .setView(dialogView)
                                    .create();

                            dialogView.findViewById(R.id.closeBtn).setOnClickListener(v -> dialog.dismiss());
                            dialog.show();
                        });

                        // Sort by subject name
                        Collections.sort(homeworkList, (h1, h2) -> h1.getSubject().compareToIgnoreCase(h2.getSubject()));
                        adapter.updateData(homeworkList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddTaskDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        Spinner subjectSpinner = dialogView.findViewById(R.id.subjectSpinner);
        EditText taskInput = dialogView.findViewById(R.id.taskInput);

        List<String> subjectIds = new ArrayList<>();
        List<String> subjectNames = new ArrayList<>();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(spinnerAdapter);

        dbRef.child(currentSemValue).child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot subject : snapshot.getChildren()) {
                    subjectIds.add(subject.getKey());
                    subjectNames.add(subject.getValue(String.class));
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    String subjectKey = subjectIds.get(position);
                    String dateKey = firebaseDateFormat.format(selectedDate.getTime());

                    // 🧼 Clear task box first!
                    taskInput.setText("");

                    dbRef.child(currentSemValue).child("works").child(dateKey).child(subjectKey).child("task")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String prevTask = snapshot.getValue(String.class);
                                    if (prevTask != null) {
                                        taskInput.setText(prevTask);
                                        taskInput.requestFocus();
                                        taskInput.setSelection(taskInput.getText().length());
                                        Toast.makeText(Home.this, "Task saved!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Add / Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    int index = subjectSpinner.getSelectedItemPosition();
                    if (index >= 0) {
                        String subjectKey = subjectIds.get(index);
                        String task = taskInput.getText().toString();
                        String dateKey = firebaseDateFormat.format(selectedDate.getTime());

                        dbRef.child(currentSemValue).child("works").child(dateKey).child(subjectKey).child("task").setValue(task);
                        dbRef.child(currentSemValue).child("works").child(dateKey).child(subjectKey).child("edited_by").setValue(((MyApp) getApplication()).getLoggedUserName());
                        dbRef.child(currentSemValue).child("works").child(dateKey).child(subjectKey).child("edited_time").setValue(new SimpleDateFormat("dd-MM-yyyy h:mma", Locale.getDefault()).format(Calendar.getInstance().getTime()));

                        loadHomeworkForDate(selectedDate);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}