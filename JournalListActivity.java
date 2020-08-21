package com.example.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.example.journal.Model.Journal;
import com.example.journal.Util.JournalApi;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private JournalRecyclerViewAdapter journalRecyclerAdapter;

    private CollectionReference collectionReference=db.collection("Journal");
    private TextView noJournalEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        noJournalEntry=findViewById(R.id.listNoThoughts);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_id:
                if(user!=null&&firebaseAuth!=null){
                    startActivity(new Intent(JournalListActivity.this,PostJournalActivity.class));
                    finish();
                }
                break;
            case R.id.signout:
                if(user!=null&&firebaseAuth!=null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(JournalListActivity.this,LoginActivity.class));
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void show(){
        Query query=collectionReference.whereEqualTo("userId",JournalApi.getInstance().getUserId()).orderBy("title", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Journal> options=new FirestoreRecyclerOptions.Builder<Journal>().setQuery(query,Journal.class).build();
        journalRecyclerAdapter =new JournalRecyclerViewAdapter(options,this);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(journalRecyclerAdapter);
        journalRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(journalRecyclerAdapter!=null){
            journalRecyclerAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(journalRecyclerAdapter!=null){
            journalRecyclerAdapter.stopListening();

        }
}
}