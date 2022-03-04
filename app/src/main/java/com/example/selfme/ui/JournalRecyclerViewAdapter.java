package com.example.selfme.ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.selfme.PostUpdateJournalActivity;
import com.example.selfme.R;
import com.example.selfme.model.Journal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalRecyclerViewAdapter extends RecyclerView.Adapter<JournalRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

    // firebase authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    // firestore database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journal");



    public JournalRecyclerViewAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Journal journal = journalList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());
        holder.name.setText(journal.getUsername());
        Picasso.get().load(journal.getImageUrl()).into(holder.imageView);

        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds()*1000);
        holder.dateAdded.setText(timeAgo);

        holder.shareButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, journal.getThought());
            intent.putExtra(Intent.EXTRA_SUBJECT, journal.getTitle());
            context.startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(v -> {
            final String[] journalId = new String[1];
            if (user != null && firebaseAuth != null) {
                collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots) {
                        Journal currentJournal = snapshot.toObject(Journal.class);
                        if (currentJournal.getImageUrl().equals(journal.getImageUrl()) && currentJournal.getTitle().equals(journal.getTitle())) {
                            journalId[0] = (String) snapshot.getId();
                            break;
                        }
                    }
                    collectionReference.document(journalId[0]).delete().addOnSuccessListener(unused -> Toast.makeText(context, "Deleted Successfully. Press back button!!", Toast.LENGTH_LONG).show());
                });
            }
        });
        holder.imageView.setOnClickListener(v -> {
            final String[] journalId = new String[1];
            final String id;
            if (user != null && firebaseAuth != null) {
                collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots) {
                        Journal currentJournal = snapshot.toObject(Journal.class);
                        if (currentJournal.getTitle().equals(journal.getTitle()) && currentJournal.getThought().equals(journal.getThought()) && currentJournal.getTimeAdded().equals(journal.getTimeAdded())) {
                            journalId[0] = (String) snapshot.getId();
                            break;
                        }
                    }
                    Log.d("MAIN_TAG", "onSuccess: " + journalId[0]);
                    Intent intent = new Intent(context, PostUpdateJournalActivity.class);
                    intent.putExtra("title", journal.getTitle());
                    intent.putExtra("thought", journal.getThought());
                    intent.putExtra("journalId", journalId[0]);
                    context.startActivity(intent);
                });
            }


        });

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView thoughts;
        public ImageView imageView;
        public TextView name;
        public TextView dateAdded;
        public  ImageButton shareButton;
        public ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            imageView = itemView.findViewById(R.id.journal_image_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            name = itemView.findViewById(R.id.journal_row_username);
            shareButton = itemView.findViewById(R.id.journal_share_button);
            deleteButton = itemView.findViewById(R.id.journal_delete_button);

        }
    }
}
