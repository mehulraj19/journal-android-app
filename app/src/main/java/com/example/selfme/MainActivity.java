package com.example.selfme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.selfme.util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if(currentUser != null) {
                currentUser = firebaseAuth.getCurrentUser();
                String currentUserId = currentUser.getUid();

                collectionReference.whereEqualTo("userId", currentUserId).addSnapshotListener((value, error) -> {
                    if (error != null) {return;}
                    assert value != null;
                    if (!value.isEmpty()) {
                        for (QueryDocumentSnapshot snapshot : value) {
                            JournalApi journalApi = JournalApi.getInstance();
                            journalApi.setUserId(snapshot.getString("userId"));
                            journalApi.setUsername(snapshot.getString("username"));
                            startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                            finish();
                        }
                    }
                });
            }
        };


        findViewById(R.id.getStartedButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}