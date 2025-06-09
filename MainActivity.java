package com.example.luzonfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        // Setup RecyclerView
        postsRecyclerView = findViewById(R.id.recentPostsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        postsRecyclerView.setAdapter(postAdapter);

        // Load all posts initially
        loadPosts("all");

        // Category Buttons
        Button btnAnnouncements = findViewById(R.id.btnAnnouncements);
        Button btnServices = findViewById(R.id.btnServices);
        Button btnLostAndFound = findViewById(R.id.btnLostAndFound);
        Button btnBuyAndSell = findViewById(R.id.btnBuyAndSell);

        btnAnnouncements.setOnClickListener(v -> loadPosts("Announcements"));
        btnServices.setOnClickListener(v -> loadPosts("Services"));
        btnLostAndFound.setOnClickListener(v -> loadPosts("Lost and Found"));
        btnBuyAndSell.setOnClickListener(v -> loadPosts("Buy and Sell"));

        // Create Post Button
        Button btnCreatePost = findViewById(R.id.btnCreatePost);
        btnCreatePost.setOnClickListener(v -> {
            // Check and request storage permission before opening CreatePostActivity
            if (checkStoragePermission()) {
                startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
            }
        });
    }

    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with creating post
                startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPosts(String category) {
        databaseReference.removeEventListener(valueEventListener);

        if (!category.equals("all")) {
            databaseReference.orderByChild("category")
                    .equalTo(category)
                    .addValueEventListener(valueEventListener);
        } else {
            databaseReference.addValueEventListener(valueEventListener);
        }
    }

    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            postList.clear();
            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);
                if (post != null) {
                    post.setPostId(postSnapshot.getKey());
                    postList.add(post);
                }
            }
            postAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(MainActivity.this, "Error loading posts: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
    }
}