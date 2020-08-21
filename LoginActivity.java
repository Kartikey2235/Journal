package com.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.journal.Model.Journal;
import com.example.journal.Util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity<JournalAPi> extends AppCompatActivity {
    private Button login;
    private Button createAccount;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser firebaseUser;

    private ProgressBar progressBar;
    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login=findViewById(R.id.email_signIn_button);
        createAccount=findViewById(R.id.email_create_button);

        progressBar=findViewById(R.id.login_progress);
        emailAddress=findViewById(R.id.email);
        password=findViewById(R.id.password);
        firebaseAuth=FirebaseAuth.getInstance();

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);


        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,Create_account_activity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),password.getText().toString().trim());
            }
        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {
        if(!TextUtils.isEmpty(email)&&
                !TextUtils.isEmpty(pwd)){
            firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser user=firebaseAuth.getCurrentUser();
                    assert user!=null;
                    final String currentUserId=user.getUid();

                    collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                    finish();
                                }
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this,"Enter correct email address and password",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });

        }else{
            Toast.makeText(LoginActivity.this,"Enter Email and Password",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}