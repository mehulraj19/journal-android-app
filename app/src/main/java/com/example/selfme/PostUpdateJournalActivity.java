package com.example.selfme;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import java.util.Date;

public class PostUpdateJournalActivity extends AppCompatActivity {

    // Widgets
    private ImageView postUpdateImageView, postUpdateCameraButton;
    private Button postUpdateSaveButton;
    private TextView postUpdateUsernameTextview;
    private ProgressBar postUpdateProgressbar;
    private EditText postUpdateTitle, postUpdateThought;

    // firebase authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journal");

    // storage reference
    private StorageReference storageReference;

    // supporting part
    private String currentUserId;
    private String currentUserName;
    private Uri imageUri;
    public static final int GALLERY_CODE = 1;
    private String journalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_update_journal);

        storageReference = FirebaseStorage.getInstance().getReference();;

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null){}
            else{}
        };

        Intent updateIntent = getIntent();

        postUpdateImageView = findViewById(R.id.post_update_imageView);
        postUpdateCameraButton = findViewById(R.id.postupdateCameraButton);
        postUpdateSaveButton = findViewById(R.id.postUpdatesaveButton);
        postUpdateUsernameTextview = findViewById(R.id.post_update_username_textview);
        postUpdateProgressbar = findViewById(R.id.post_update_progressBar);
        postUpdateTitle = findViewById(R.id.post_update_title_et);
        postUpdateThought = findViewById(R.id.post_update_thoughts_et);
        // already added data
        postUpdateTitle.setText(updateIntent.getStringExtra("title"));
        postUpdateThought.setText(updateIntent.getStringExtra("thought"));

        journalId = updateIntent.getStringExtra("journalId");
        Log.d("MAIN_TAG", "onCreate: " +journalId);

        if (JournalApi.getInstance() != null){
            currentUserName = JournalApi.getInstance().getUsername();
            currentUserId = JournalApi.getInstance().getUserId();
            postUpdateUsernameTextview.setText(currentUserName);
        }
        postUpdateCameraButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_CODE);
        });
        postUpdateSaveButton.setOnClickListener(v -> {
            updateJournal();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            postUpdateImageView.setImageURI(imageUri);
        }
    }
    private void updateJournal() {
        postUpdateProgressbar.setVisibility(View.VISIBLE);
        final String title = postUpdateTitle.getText().toString();
        final String thought = postUpdateThought.getText().toString();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null) {
            final StorageReference filepath = storageReference.child("journal_images").child("my_image_"+ Timestamp.now().getSeconds());
            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Journal journal = new Journal();
                        journal.setImageUrl(imageUrl);
                        journal.setThought(thought);
                        journal.setTitle(title);
                        journal.setTimeAdded(new Timestamp(new Date()));
                        journal.setUsername(currentUserName);
                        journal.setUserId(currentUserId);

                        collectionReference.document(journalId)
                                .set(journal)
                                .addOnSuccessListener(unused -> {
                                    postUpdateProgressbar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(PostUpdateJournalActivity.this, JournalListActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> postUpdateProgressbar.setVisibility(View.INVISIBLE));
                    }).addOnFailureListener(e -> {
                        postUpdateProgressbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PostUpdateJournalActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        postUpdateProgressbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PostUpdateJournalActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
                    });
        }else {
            postUpdateProgressbar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "fill in all the details!!", Toast.LENGTH_SHORT).show();
        }
    }
}