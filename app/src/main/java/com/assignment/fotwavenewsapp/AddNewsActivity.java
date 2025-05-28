package com.assignment.fotwavenewsapp;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddNewsActivity extends AppCompatActivity {

    private TextInputEditText titleInput, contentInput, dateInput;
    private Spinner spinnerNewsType;
    private ImageView imageView;
    private Button btnSelectImage, btnSubmit;

    private Uri selectedImageUri;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imageView.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        titleInput = findViewById(R.id.input_news_title);
        contentInput = findViewById(R.id.input_news_content);
        dateInput = findViewById(R.id.input_news_date);
        spinnerNewsType = findViewById(R.id.spinner_news_type);
        imageView = findViewById(R.id.image_news);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSubmit = findViewById(R.id.btn_submit_news);

        // Setup spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.news_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNewsType.setAdapter(adapter);

        dateInput.setOnClickListener(v -> showDatePicker());

        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSubmit.setOnClickListener(v -> submitNews());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateInput.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void submitNews() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";
        String date = dateInput.getText() != null ? dateInput.getText().toString().trim() : "";
        String newsType = spinnerNewsType.getSelectedItem() != null ? spinnerNewsType.getSelectedItem().toString() : "";

        if (title.isEmpty() || content.isEmpty() || date.isEmpty() || selectedImageUri == null || newsType.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload image first
        String fileName = "news_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        btnSubmit.setEnabled(false);
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Prepare news data map
                            Map<String, Object> newsData = new HashMap<>();
                            newsData.put("title", title);
                            newsData.put("content", content);
                            newsData.put("date", date);
                            newsData.put("imageUrl", imageUrl);
                            newsData.put("newsType", newsType);

                            // Save news document to Firestore
                            firestore.collection("news")
                                    .add(newsData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "News submitted successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to submit news: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        btnSubmit.setEnabled(true);
                                    });
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                });
    }
}
