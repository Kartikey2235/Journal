package com.example.journal;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.journal.Model.Journal;
import com.example.journal.Util.JournalApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int GALLERY_CODE = 1;
    private static final String TAG = "TAG";
    private Button saveButton;
    private ProgressBar post_progressbar;
    private ImageView post;
    private EditText postTitle;
    private EditText postDetail;
    private TextView currentUser;
    private ImageView imageView;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");
    private DocumentReference documentReference= db.collection("Journal").document();
    private Uri imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        post_progressbar = findViewById(R.id.postprogressBar);
        postTitle = findViewById(R.id.post_title_text);
        postDetail = findViewById(R.id.post_detail_text);
        currentUser = findViewById(R.id.post_text);
        imageView = findViewById(R.id.imageView2);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);


        saveButton = findViewById(R.id.post_button);
        post = findViewById(R.id.postImage);

        saveButton.setOnClickListener(this);
        post.setOnClickListener(this);

        post_progressbar.setVisibility(View.INVISIBLE);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();

            currentUser.setText(currentUserName);
        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_button:
                saveJournal();
                break;
            case R.id.postImage:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {
        final String title = postTitle.getText().toString().trim();
        final String details = postDetail.getText().toString().trim();

        post_progressbar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(details) &&
                imageUrl != null) {

            final StorageReference filepath = storageReference.child("journal_images")
                    .child("myimage" + Timestamp.now().getSeconds());
            filepath.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String url = uri.toString();

                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThought(details);
                            journal.setImageUrl(url);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUsername(currentUserName);
                            journal.setUserId(currentUserId);

                            collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    post_progressbar.setVisibility(View.INVISIBLE);
                                    Intent intent=new Intent(PostJournalActivity.this, JournalListActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                    Toast.makeText(PostJournalActivity.this,"Task Failed",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    post_progressbar.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            if (imageUrl==null) {
                Toast.makeText(PostJournalActivity.this, "Add an Image", Toast.LENGTH_LONG).show();
                post_progressbar.setVisibility(View.INVISIBLE);
            }else if(TextUtils.isEmpty(title)){
                    Toast.makeText(PostJournalActivity.this,"Add a Title",Toast.LENGTH_LONG).show();
                post_progressbar.setVisibility(View.INVISIBLE);
            }else if (TextUtils.isEmpty(details)){
                Toast.makeText(PostJournalActivity.this,"Add a Thought",Toast.LENGTH_LONG).show();
                post_progressbar.setVisibility(View.INVISIBLE);
            }

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUrl = data.getData();
                imageView.setImageURI(imageUrl);

            }
        }
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
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
        finish();
    }
}