package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.Adapter.AttachmentsAdapter;
import com.sesyme.sesyme.Adapter.DoubleAdapter;
import com.sesyme.sesyme.data.DoubleClass;
import com.sesyme.sesyme.data.MessageDetails;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;
import java.util.Date;

public class ClusterAttachments extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String type, clusterId;
    private RecyclerView recyclerView;
    private AttachmentsAdapter adapter;
    private ArrayList<DoubleClass> itemsList;
    private DoubleAdapter pollsAdapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_attachments);

        recyclerView = findViewById(R.id.cluster_attachments_recycler);
        emptyText = findViewById(R.id.empty_cluster_attachment);
        emptyText.setVisibility(View.GONE);
        itemsList = new ArrayList<>();

        if (getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("attType")) {
            type = intent.getStringExtra("attType");
            clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
        }
        switch (type) {
            case "Poll":
                setUpPolls();
                break;
            case "Quiz":
                setUpQuizzes();
                break;
            default:
                setUpAttachment(type);
        }

    }

    private void setUpQuizzes() {
        db.collection(SefnetContract.QUIZZES_DETAILS).orderBy(SefnetContract.RELEASE_QUIZ)
                .whereEqualTo(SefnetContract.REFERENCE, clusterId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null){
                    if (queryDocumentSnapshots.size() > 0) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            String creator = snapshot.getString(SefnetContract.CREATOR);
                            String title = snapshot.getString(SefnetContract.TITLE);
                            Date createdAt = snapshot.getDate(SefnetContract.RELEASE_QUIZ);
                            itemsList.add(new DoubleClass(creator, "Quiz", title, snapshot.getReference().getPath(), createdAt));
                            pollsAdapter.notifyDataSetChanged();
                        }
                        emptyText.setVisibility(View.GONE);
                    }else{
                        String text = "Sorry, no Quizzes found for this cluster";
                        emptyText.setText(text);
                        emptyText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        pollsAdapter = new DoubleAdapter(this, itemsList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(pollsAdapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setUpPolls() {
        Query query = db.collection(SefnetContract.POLLS_DETAILS).orderBy(SefnetContract.CREATED_AT, Query.Direction.DESCENDING)
                .whereEqualTo(SefnetContract.REFERENCE, clusterId);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null){
                    if (queryDocumentSnapshots.size() > 0) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            String creator = snapshot.getString(SefnetContract.CREATOR);
                            String title = snapshot.getString(SefnetContract.TITLE);
                            Date createdAt = snapshot.getDate(SefnetContract.CREATED_AT);
                            itemsList.add(new DoubleClass(creator, "Poll", title, snapshot.getReference().getPath(), createdAt));
                            pollsAdapter.notifyDataSetChanged();
                        }
                        emptyText.setVisibility(View.GONE);
                    }else {
                        String text = "Sorry, no polls found for this cluster";
                        emptyText.setText(text);
                        emptyText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        pollsAdapter = new DoubleAdapter(this, itemsList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(pollsAdapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setUpAttachment(final String type) {
        Query query = db.collection(clusterId).whereEqualTo("attType", type);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null) {
                    String text = "No " + type.toLowerCase() + "s found for this cluster";
                    emptyText.setText(text);
                    if (queryDocumentSnapshots.size() > 0) {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }
        });
        FirestoreRecyclerOptions<MessageDetails> options = new FirestoreRecyclerOptions.Builder<MessageDetails>()
                .setQuery(query, MessageDetails.class).build();
        adapter = new AttachmentsAdapter(this, options);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
