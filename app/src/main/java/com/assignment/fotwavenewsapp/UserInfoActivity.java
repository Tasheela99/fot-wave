package com.assignment.fotwavenewsapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoActivity extends BaseActivity {
    private static final String TAG = "UserInfoActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private TextView tvUsername, tvEmail;
    private Button btnUpdateInfo;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences sharedPreferences;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initViews();
        setupBottomNavigation();
        setupToolbarWithUsername();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserDataFromFirebase(userId);
        }

        setupClickListeners();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnUpdateInfo = findViewById(R.id.btn_update_info);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String username = "User";
                        if (documentSnapshot.exists()) {
                            username = documentSnapshot.getString("username");
                            if (username == null || username.isEmpty()) {
                                username = firebaseUser.getEmail().split("@")[0];
                            }
                        }

                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setTitle(username);
                        }

                        toolbar.setNavigationOnClickListener(v -> onBackPressed());
                    })
                    .addOnFailureListener(e -> {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setTitle("User");
                        }

                        toolbar.setNavigationOnClickListener(v -> onBackPressed());
                    });

        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("User");
            }

            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }


    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_academic) {
                    startActivity(new Intent(UserInfoActivity.this, AcademicNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_events) {
                    startActivity(new Intent(UserInfoActivity.this, EventsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_sports) {
                    return true;
                }
                return false;
            }
        });
    }

    private void loadUserDataFromFirebase(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_USERNAME, username != null ? username : "Guest User");
                        editor.putString(KEY_EMAIL, email != null ? email : "guest@example.com");
                        editor.apply();
                        tvUsername.setText(username != null ? username : "Guest User");
                        tvEmail.setText(email != null ? email : "guest@example.com");

                        Log.d(TAG, "User data fetched from Firebase: " + username + ", " + email);
                    } else {
                        Log.d(TAG, "No user document found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user data", e);
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnUpdateInfo.setOnClickListener(v -> showUpdateDialog());
    }

    private void showUpdateDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_user);
        dialog.setCancelable(true);

        TextInputLayout tilUsername = dialog.findViewById(R.id.til_username);
        TextInputEditText etUsername = dialog.findViewById(R.id.et_username);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        String currentUsername = sharedPreferences.getString(KEY_USERNAME, "");
        etUsername.setText(currentUsername);

        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();

            if (newUsername.isEmpty()) {
                tilUsername.setError("Username cannot be empty");
                return;
            }

            if (newUsername.length() < 3) {
                tilUsername.setError("Username must be at least 3 characters");
                return;
            }

            tilUsername.setError(null);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, newUsername);
            editor.apply();
            firestore.collection("users").document(userId)
                    .update("username", newUsername)
                    .addOnSuccessListener(aVoid -> {
                        tvUsername.setText(newUsername);
                        Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Log.d(TAG, "Username updated to: " + newUsername);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Firestore update failed", e);
                    });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    // Optional utility method to save user data
    public static void saveUserData(android.content.Context context, String username, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }
}