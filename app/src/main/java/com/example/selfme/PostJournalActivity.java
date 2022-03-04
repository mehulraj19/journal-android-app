package com.example.selfme;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.selfme.model.Journal;
import com.example.selfme.util.JournalApi;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity {


    // firebase authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // firebase firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journal");

    // Storage reference
    private StorageReference storageReference;

    // widgets
    private EditText titleEditText;
    private EditText thoughtEditText;
    private ImageView imageView;
    private ProgressBar progressBar;

    // Supporting part
    private String currentUserId;
    private String currentUsername;
    private Uri imageUri;
    public static final int GALLERY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null){}
            else {}
        };

        ImageView addPhotoButton = findViewById(R.id.postupdateCameraButton);
        Button saveButton = findViewById(R.id.postUpdatesaveButton);
        titleEditText = findViewById(R.id.post_update_title_et);
        thoughtEditText = findViewById(R.id.post_update_thoughts_et);
        imageView = findViewById(R.id.post_update_imageView);
        progressBar = findViewById(R.id.post_update_progressBar);
        TextView usernameTextView = findViewById(R.id.post_update_username_textview);

        if (JournalApi.getInstance() != null) {
            currentUsername = JournalApi.getInstance().getUsername();
            currentUserId = JournalApi.getInstance().getUserId();

            usernameTextView.setText(currentUsername);
        }

        addPhotoButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_CODE);
        });
        saveButton.setOnClickListener(v -> saveJournal());
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void saveJournal() {
        progressBar.setVisibility(View.VISIBLE);
        final String title = titleEditText.getText().toString().trim();
        final String thought = thoughtEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null) {
            final StorageReference filepath = storageReference.child("journal_images").child("my_image_" + Timestamp.now().getSeconds());
            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                Journal journal = new Journal();
                                journal.setImageUrl(imageUrl);
                                journal.setThought(thought);
                                journal.setTitle(title);
                                journal.setTimeAdded(new Timestamp(new Date()));
                                journal.setUsername(currentUsername);
                                journal.setUserId(currentUserId);

                                collectionReference.add(journal)
                                        .addOnSuccessListener(documentReference -> {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(PostJournalActivity.this, "Something went wrong!!", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> progressBar.setVisibility(View.INVISIBLE)))
                    .addOnFailureListener(e -> Toast.makeText(PostJournalActivity.this, "Something went wrong while adding file!!", Toast.LENGTH_SHORT).show());
        }else {
            Toast.makeText(this, "Enter in all the fields!!", Toast.LENGTH_SHORT).show();
        }

    }

}