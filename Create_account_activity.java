package com.example.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.journal.Util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Create_account_activity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    CollectionReference collectionReference=db.collection("Users");

    private EditText emailEditetext;
    private EditText passwordEdittext;
    private ProgressBar progressBar;
    private EditText usernameEdittext;
    private Button create_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_activity);

        firebaseAuth=FirebaseAuth.getInstance();
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);


        create_Button=findViewById(R.id.create_signUp_button);
        progressBar=findViewById(R.id.create_progress);
        usernameEdittext=findViewById(R.id.username);
        passwordEdittext=findViewById(R.id.createpassword);
        emailEditetext=findViewById(R.id.createemail);

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
             currentUser=firebaseAuth.getCurrentUser();
             if(currentUser!=null){

             }else{

             }
            }
        };

        create_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(emailEditetext.getText().toString())&&
                !TextUtils.isEmpty(usernameEdittext.getText().toString())&&
                !TextUtils.isEmpty(passwordEdittext.getText().toString())) {
                    String email=emailEditetext.getText().toString().trim();
                    String password=passwordEdittext.getText().toString().trim();
                    String username=usernameEdittext.getText().toString().trim();
                    createUserEmailAccount(email, password, username);
                }else {
                    Toast.makeText(Create_account_activity.this,"Fields can not be empty",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createUserEmailAccount(String email, String password, final String username ){
        if(!TextUtils.isEmpty(email)&&
        !TextUtils.isEmpty(password)&&
        !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                currentUser=firebaseAuth.getCurrentUser();
                                assert currentUser!=null;
                                final String currentUserId=currentUser.getUid();

                                Map<String,String> userObj=new HashMap<>();
                                userObj.put("userId",currentUserId);
                                userObj.put("username",username);

                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if(Objects.requireNonNull(task.getResult()).exists()){
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            String name=task.getResult().getString("username");

                                                            JournalApi journalApi=JournalApi.getInstance();
                                                            journalApi.setUserId(currentUserId);
                                                            journalApi.setUsername(name);


                                                            Intent intent=new Intent(Create_account_activity.this,PostJournalActivity.class);
                                                            intent.putExtra("username",name);
                                                            intent.putExtra("userId",currentUserId);
                                                            startActivity(intent);
                                                            finish();

                                                        }
                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Create_account_activity.this,"Task Failed",Toast.LENGTH_LONG).show();
                }
            });

        }else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}