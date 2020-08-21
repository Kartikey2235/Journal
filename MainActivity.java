package com.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.journal.Util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button getStarted;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser user;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    private CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getStarted=findViewById(R.id.startButton);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth=FirebaseAuth.getInstance();

        listener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    user=firebaseAuth.getCurrentUser();
                    final String currentUserId = user.getUid();

                    collectionReference.whereEqualTo("userId", currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                            }
                            assert value != null;
                            if (!value.isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : value) {
                                    JournalApi journalApi = JournalApi.getInstance();
                                    journalApi.setUsername(snapshot.getString("username"));
                                    journalApi.setUserId(currentUserId);

                                    startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                                    finish();
                                }
                            }
                        }
                    });
                }
            }
        };
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(listener);
        }
    }
}