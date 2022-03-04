package com.example.selfme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.selfme.util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    // firebase authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firestore Database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    // Widgets
    private EditText usernameEditText;
    private EditText passwordEditText;
    private AutoCompleteTextView emailAddressEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.username_account);
        passwordEditText = findViewById(R.id.password_account);
        emailAddressEditText = findViewById(R.id.email_account);
        progressBar = findViewById(R.id.account_progress);
        Button createAccountButton = findViewById(R.id.createAccountButton_account);
        createAccountButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailAddressEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            createUserEmailAccount(username, email, password);
        });

        authStateListener = firebaseAuth -> currentUser = firebaseAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    private void createUserEmailAccount(String username, String email, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            // getting UId
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            // create User map to create user in the user collection
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", currentUserId);
                            userObj.put("username", username);

                            // save to firebase firestore
                            collectionReference.add(userObj)
                                    .addOnSuccessListener(documentReference -> documentReference.get().addOnCompleteListener(task1 -> {

                                        if (task1.getResult().exists()) {
                                            progressBar.setVisibility(View.INVISIBLE);

                                            // saving the name for furthe usage in front end
                                            String name = task1.getResult().getString("username");
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserId(currentUserId);
                                            journalApi.setUsername(name);

                                            // next activity...
                                            Intent intent = new Intent(CreateAccountActivity.this, JournalListActivity.class);
                                            intent.putExtra("username", name);
                                            intent.putExtra("userId", currentUserId);
                                            startActivity(intent);
                                        }else {
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }))
                                    .addOnFailureListener(e -> Toast.makeText(CreateAccountActivity.this, "Something went wrong!!", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CreateAccountActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Fill in All the fields!!", Toast.LENGTH_SHORT).show();
        }
    }
}