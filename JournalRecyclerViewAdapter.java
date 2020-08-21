package com.example.journal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journal.Model.Journal;
import com.example.journal.Util.JournalApi;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalRecyclerViewAdapter extends FirestoreRecyclerAdapter<Journal,JournalRecyclerViewAdapter.ViewHolder>{
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Journal");

    private String imageUrl;
    private DocumentReference documentReference=db.collection("Journal").document();
    private Context context;
    private List<Journal> JournalList;

    public JournalRecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<Journal> options,Context context) {
        super(options);
        this.context=context;
//        this.JournalList=journalList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.journal_row,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Journal model) {
        holder.title.setText(model.getTitle());
        holder.thoughts.setText(model.getThought());
        holder.name.setText(String.valueOf(model.getUsername()));
        imageUrl=model.getImageUrl();
        String timeAgo=(String) DateUtils.getRelativeTimeSpanString(model.getTimeAdded().getSeconds()*1000);
        holder.dateAdded.setText(timeAgo);

        Picasso.get().load(imageUrl).placeholder(R.drawable.road).fit().into(holder.image);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView thoughts;
        public TextView title;
        public TextView dateAdded;
        public TextView name;
        public ImageView image;
        public ImageButton shareButton;
        public ImageButton deleteButton;
        public ImageButton editButton;
//        public ImageButton editImageButton;

        String userId;
        String username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shareButton=itemView.findViewById(R.id.journal_row_share);
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thoughts_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp);
            image = itemView.findViewById(R.id.journal_image_list);
            name=itemView.findViewById(R.id.jornal_row_username);
            deleteButton=itemView.findViewById(R.id.deleteButton);
            editButton=itemView.findViewById(R.id.editButton);
//            editImageButton=itemView.findViewById(R.id.editImageButton);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = thoughts.getText().toString();
                    String shareTitle=title.getText().toString();
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Title= "+shareTitle + "\n"+"Thought= "+ shareBody);
                    context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                       deleteItem(getAdapterPosition());
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editItem(getAdapterPosition());
                }
            });

//            editImageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    editImage();
//                }
//            });
        }
        private void editItem(final int postion){
            builder=new AlertDialog.Builder(context);
            inflater=LayoutInflater.from(context);
            View view=inflater.inflate(R.layout.edit_journal,null);

            Button editsaveButton;
            ImageButton cancel;
            final EditText editpostTitle;
            final EditText editpostDetail;

            editsaveButton=view.findViewById(R.id.edit_post_button);
            editpostTitle=view.findViewById(R.id.edit_title_text);
            editpostDetail=view.findViewById(R.id.edit_detail_text);
            cancel=view.findViewById(R.id.cancel_journal_dialog);

            builder.setView(view);
            dialog=builder.create();
            dialog.show();
            editpostTitle.setText(title.getText());
            editpostDetail.setText(thoughts.getText());

            editsaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String editTitle=editpostTitle.getText().toString().trim();
                    String editThought=editpostDetail.getText().toString().trim();

                    Map<String,Object> data=new HashMap<>();
                    data.put("title",editTitle);
                    data.put("thought",editThought);

                    getSnapshots().getSnapshot(postion).getReference().update(data);
                    notifyItemChanged(postion);
                    dialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

        }
//
//        private void editImage(){
//
//            builder=new AlertDialog.Builder(context);
//            inflater=LayoutInflater.from(context);
//            View view=inflater.inflate(R.layout.edit_image,null);
//
//            ImageView showImage=view.findViewById(R.id.showImage);
//            ImageView showImageButton=view.findViewById(R.id.showImageButton);
//            ImageButton cancelImage=view.findViewById(R.id.cancel_image_dialog);
//            Button saveImage=view.findViewById(R.id.saveImageEdited);
//
//            builder.setView(view);
//            dialog=builder.create();
//            dialog.show();
//
//            Picasso.get().load(imageUrl).placeholder(R.drawable.road).fit().into(showImage);
//
//            showImageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//
//            cancelImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                }
//            });
//
//            saveImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//        }
    }

    private void deleteItem(final int position) {
        builder=new AlertDialog.Builder(context);
        inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.delete_popup,null);

        Button yesButton=view.findViewById(R.id.yesButton);
        Button noButton=view.findViewById(R.id.noButton);

        builder.setView(view);
        dialog=builder.create();
        dialog.show();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSnapshots().getSnapshot(position).getReference().delete();
                dialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
