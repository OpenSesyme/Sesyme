package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sesyme.sesyme.Adapter.QuestionsAdapter;
import com.sesyme.sesyme.data.WritePostClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MyPosts extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private QuestionsAdapter myAdapter;
    private CollectionReference QuestionsRef;
    private TextView emptyText;
    private Button post;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_my_posts);
        emptyText = findViewById(R.id.empty_posts_profile);
        progressBar = findViewById(R.id.posts_progress_loading);
        post = findViewById(R.id.ask_posts);
        QuestionsRef = db.collection(SefnetContract.QUESTIONS);

        Intent intent = getIntent();
        if (intent.hasExtra("author") && intent.hasExtra("type")){
            String author = intent.getStringExtra("author");
            String type = intent.getStringExtra("type");
            if (type != null) {
                setUpPosts(type, author);
                if (getSupportActionBar() != null){
                    String title = type + "s";
                    getSupportActionBar().setTitle(title);
                }
            }
        }else{
            Toast.makeText(this, "You haven't selected what to show.", Toast.LENGTH_SHORT).show();
        }

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MyPosts.this, WritePostActivity.class);
                intent1.putExtra("type", "Question");
                startActivity(intent1);
            }
        });
    }

    private void setUpPosts(final String type, final String author){
        Query query;
        if (type.equals("Question")) {
            query = QuestionsRef.orderBy("dateTime", Query.Direction.DESCENDING)
                    .whereEqualTo("author", author).whereEqualTo("type", type);
        }else{
            query = db.collectionGroup("Replies").orderBy("dateTime", Query.Direction.DESCENDING)
                    .whereEqualTo("author", author).whereEqualTo("type", type);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() < 1 ) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        if (type.equals("Question")){
                            post.setVisibility(View.VISIBLE);
                        }
                    }else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }
            }
        });
        FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                .setQuery(query, WritePostClass.class).build();
        myAdapter = new QuestionsAdapter(this, options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);

        progressBar.setVisibility(View.GONE);

        myAdapter.setOnItemClickListener(new QuestionsAdapter.OnItemClickListener() {
            @Override
            public void onCommentClicked(DocumentSnapshot snapshot, int position) {
                String reference = snapshot.getId();
                Intent intent = new Intent(MyPosts.this, WritePostActivity.class);
                intent.putExtra(SefnetContract.REFERENCE, reference);
                intent.putExtra("type", "Reply");
                startActivity(intent);
            }

            @Override
            public void onProfileClicked(DocumentSnapshot snapshot, int position) {
                Intent intent = new Intent(MyPosts.this, OthersProfile.class);
                intent.putExtra(SefnetContract.PROFILE_REF, snapshot.getString("author"));
                startActivity(intent);
            }

            @Override
            public void onTitleClicked(DocumentSnapshot snapshot, int position) {
                if ("Question".equals(snapshot.getString("type"))) {
                    String id = snapshot.getId();
                    Intent intent = new Intent(MyPosts.this, AnswersActivity.class);
                    intent.putExtra(SefnetContract.REFERENCE, id);
                    startActivity(intent);
                }else {
                    String path = snapshot.getReference().getPath();
                    String[] segments = path.split("/");
                    String id = segments[1];
                    Intent intent = new Intent(MyPosts.this, AnswersActivity.class);
                    intent.putExtra(SefnetContract.REFERENCE, id);
                    startActivity(intent);
                }
            }
        });
        myAdapter.startListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        myAdapter.stopListening();
        MyPosts.this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        myAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myAdapter.stopListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
