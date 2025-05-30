package com.assignment.fotwavenewsapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.assignment.fotwavenewsapp.model.News;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EventsNewsActivity extends BaseActivity {
    private static final String TAG = "EventsNewsActivity";
    private BottomNavigationView bottomNavigationView;
    private LinearLayout newsContainer;
    private FirebaseFirestore db;
    private List<News> eventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_news);

        initializeViews();
        setupBottomNavigation();
        setupFirestore();
        fetchEventsNews();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_events);

        MaterialToolbar toolbar = findViewById(R.id.dropdown_anchor);
        setSupportActionBar(toolbar);

        // Find the LinearLayout inside ScrollView where cards will be added
        newsContainer = findViewById(R.id.news_container);
        if (newsContainer == null) {
            Log.e(TAG, "news_container not found in layout");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_sports) {
                    startActivity(new Intent(EventsNewsActivity.this, SportsNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_academic) {
                    startActivity(new Intent(EventsNewsActivity.this, AcademicNewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_events) {
                    return true;
                }

                return false;
            }
        });
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        eventsList = new ArrayList<>();
    }

    private void fetchEventsNews() {
        db.collection("news")
                .whereEqualTo("newsType", "Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            News news = document.toObject(News.class);
                            eventsList.add(news);
                            Log.d(TAG, "News fetched: " + news.getTitle());
                        }
                        displayNews();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(EventsNewsActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events news", e);
                    Toast.makeText(EventsNewsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayNews() {
        if (newsContainer == null) {
            Log.e(TAG, "newsContainer is null, cannot display news");
            return;
        }
        newsContainer.removeAllViews();
        for (News news : eventsList) {
            createNewsCard(news);
        }
        if (eventsList.isEmpty()) {
            showNoNewsMessage();
        }
    }

    private void createNewsCard(News news) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16));
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(dpToPx(4));
        cardView.setRadius(dpToPx(12));
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Create ImageView
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
        LinearLayout contentLayout = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentLayout.setLayoutParams(contentParams);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        contentLayout.setBackgroundColor(0xFFFDF0F6);
        TextView titleTextView = createTextView(news.getTitle(), 16, true, 0xFF000000);
        TextView contentTextView = createTextView(news.getContent(), 14, false, 0xFF800080);
        TextView dateTextView = createTextView(news.getDate(), 12, false, 0xFF666666);
        TextView descriptionTextView = createTextView(news.getDescription(), 14, false, 0xFF333333);

        MaterialButton readButton = new MaterialButton(this);
        MaterialButton deleteButton = new MaterialButton(this);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, dpToPx(12), 0, 0);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);

        readButton.setLayoutParams(buttonParams);
        readButton.setText("Read News");
        readButton.setCornerRadius(dpToPx(16));
        readButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        readButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        readButton.setIconPadding(dpToPx(8));
        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventsNewsActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_title", news.getTitle());
            intent.putExtra("news_content", news.getContent());
            intent.putExtra("news_description", news.getDescription());
            intent.putExtra("news_date", news.getDate());
            intent.putExtra("news_image_url", news.getImageUrl());
            startActivity(intent);
        });


        buttonLayout.addView(readButton);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String loggedInUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if ("tasheelajay1999@gmail.com".equalsIgnoreCase(loggedInUserEmail)) {
                deleteButton.setLayoutParams(buttonParams);
                deleteButton.setText("Delete News");
                deleteButton.setCornerRadius(dpToPx(16));
                deleteButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                deleteButton.setIconPadding(dpToPx(8));

                deleteButton.setOnClickListener(v -> {
                    db.collection("news")
                            .whereEqualTo("title", news.getTitle())
                            .whereEqualTo("date", news.getDate())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    snapshot.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "News deleted", Toast.LENGTH_SHORT).show();
                                                fetchEventsNews(); // Refresh news
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                });

                buttonLayout.addView(deleteButton);
            }
        }

        contentLayout.addView(titleTextView);
        contentLayout.addView(contentTextView);
        contentLayout.addView(dateTextView);
        contentLayout.addView(descriptionTextView);
        contentLayout.addView(buttonLayout);

        // Add views to main layout
        mainLayout.addView(imageView);
        mainLayout.addView(contentLayout);

        // Add main layout to card
        cardView.addView(mainLayout);

        // Add card to news container
        newsContainer.addView(cardView);
    }

    private TextView createTextView(String text, int textSize, boolean bold, int color) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(4), 0, 0);
        textView.setLayoutParams(params);
        textView.setText(text != null ? text : "");
        textView.setTextSize(textSize);
        textView.setTextColor(color);
        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return textView;
    }

    private void showNoNewsMessage() {
        TextView noNewsText = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(32), 0, 0);
        noNewsText.setLayoutParams(params);
        noNewsText.setText("No Events news available at the moment.");
        noNewsText.setTextSize(16);
        noNewsText.setTextColor(0xFF666666);
        noNewsText.setGravity(android.view.Gravity.CENTER);
        newsContainer.addView(noNewsText);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}