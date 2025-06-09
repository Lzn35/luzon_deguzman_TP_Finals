package com.example.luzonfinalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Arrays;
import java.util.List;
import android.provider.MediaStore;

public class CreatePostActivity extends AppCompatActivity {

    // UI Elements
    private EditText editTextTitle, editTextDescription, editTextAuthorName, editTextContactInfo;
    private Spinner spinnerCategory;
    private Button buttonCreatePost, btnSelectImage;
    private ImageView imagePreview;

    private DatabaseReference databaseReference;

    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private boolean hasImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        initializeViews();

        setupCategorySpinner();

        btnSelectImage.setOnClickListener(v -> openImageChooser());
        buttonCreatePost.setOnClickListener(v -> createPost());
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextAuthorName = findViewById(R.id.editTextAuthorName);
        editTextContactInfo = findViewById(R.id.editTextContactInfo);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonCreatePost = findViewById(R.id.buttonCreatePost);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imagePreview = findViewById(R.id.imagePreview);
        imagePreview.setVisibility(View.GONE); // Hide initially
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList(
                "Select Category",
                "Announcements",
                "Services",
                "Lost and Found",
                "Buy and Sell"
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                imagePreview.setImageURI(imageUri);
                imagePreview.setVisibility(View.VISIBLE);
                hasImage = true;
            }
        }
    }

    private void createPost() {
        // Get input values
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String authorName = editTextAuthorName.getText().toString().trim();
        String contactInfo = editTextContactInfo.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        // Validate inputs
        if (!validateInputs(title, description, authorName, contactInfo, category)) {
            return;
        }

        if (hasImage) {
            uploadImageAndCreatePost(title, description, category, authorName, contactInfo);
        } else {
            createPostInDatabase(title, description, category, authorName, contactInfo, "");
        }
    }

    private boolean validateInputs(String title, String description, String authorName,
                                   String contactInfo, String category) {
        if (title.isEmpty()) {
            editTextTitle.setError("Title is required!");
            return false;
        }
        if (description.isEmpty()) {
            editTextDescription.setError("Description is required!");
            return false;
        }
        if (authorName.isEmpty()) {
            editTextAuthorName.setError("Author name is required!");
            return false;
        }
        if (contactInfo.isEmpty()) {
            editTextContactInfo.setError("Contact info is required!");
            return false;
        }
        if (category.equals("Select Category")) {
            Toast.makeText(this, "Please select a category!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadImageAndCreatePost(String title, String description, String category,
                                          String authorName, String contactInfo) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ImgBBUploader.uploadImage(this, imageUri, new ImgBBUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                progressDialog.dismiss();
                createPostInDatabase(title, description, category, authorName, contactInfo, imageUrl);
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                Toast.makeText(CreatePostActivity.this,
                        "Image upload failed: " + error,
                        Toast.LENGTH_LONG).show();
                //allow posting without image
                createPostInDatabase(title, description, category, authorName, contactInfo, "");
            }
        });
    }

    private void createPostInDatabase(String title, String description, String category,
                                      String authorName, String contactInfo, String imageUrl) {
        String postId = databaseReference.push().getKey();
        if (postId == null) {
            Toast.makeText(this, "Failed to create post!", Toast.LENGTH_SHORT).show();
            return;
        }

        Post post = new Post(title, description, category, authorName, contactInfo, imageUrl);
        post.setPostId(postId);

        databaseReference.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePostActivity.this,
                            "Post created successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(CreatePostActivity.this,
                                "Failed to create post: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}