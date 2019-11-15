package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.sesyme.sesyme.Adapter.CommentsAdapter;
import com.sesyme.sesyme.data.CommentsClass;
import com.sesyme.sesyme.data.SefnetContract;

public class ReadArticle extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView title, author, description;
    private ImageView profilePic;
    private CommentsAdapter myAdapter;
    private ImageView cover;
    private EditText etComments;
    private RecyclerView commentsRecycler;
    private String articleId, authorS, email, attType, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_article);

        title = findViewById(R.id.title_read_article);
        author = findViewById(R.id.author_read_article);
        description = findViewById(R.id.description_read_article);
        profilePic = findViewById(R.id.profile_pic_read_article);
        cover = findViewById(R.id.cover_read_article);
        ImageView back = findViewById(R.id.back_button_read_article);
        etComments = findViewById(R.id.et_comment_read_article);
        commentsRecycler = findViewById(R.id.comments_recycler_read_article);
        ImageView btSend = findViewById(R.id.btn_send_read_article);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.ARTICLE_ID)) {
            articleId = intent.getStringExtra(SefnetContract.ARTICLE_ID);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etComments.clearFocus();
            }
        }, 200);

        if (articleId != null) {
            db.collection(SefnetContract.QUESTIONS).document(articleId)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        authorS = documentSnapshot.getString(SefnetContract.AUTHOR);
                        String desc = documentSnapshot.getString(SefnetContract.DESCRIPTION);
                        String titleS = documentSnapshot.getString(SefnetContract.TITLE);
                        imageUrl = documentSnapshot.getString(SefnetContract.ATTACHMENT_URL);
                        attType = documentSnapshot.getString("attType");
                        if (attType != null && attType.equals("PDF")){
                            Glide.with(ReadArticle.this.getApplicationContext())
                                    .load(R.drawable.default_pdf).into(cover);
                        }else {
                            Glide.with(ReadArticle.this.getApplicationContext())
                                    .load(imageUrl).error(R.drawable.default_pdf).into(cover);
                        }
                        title.setText(titleS);
                        description.setText(desc);

                        db.collection(SefnetContract.USER_DETAILS).document(authorS)
                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if (e == null && documentSnapshot != null) {
                                            String userName = documentSnapshot.getString(SefnetContract.FULL_NAME);
                                            String url = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                                            Glide.with(ReadArticle.this.getApplicationContext()).load(url).error(R.drawable.img)
                                                    .centerCrop().into(profilePic);
                                            author.setText(userName);
                                        }
                                    }
                                });
                        setUpComments();
                    }
                }
            });

        }

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = "Questions/" + articleId;
                Intent intent = new Intent(ReadArticle.this, ViewImage.class);
                intent.putExtra("attType", attType);
                intent.putExtra("imageUrl", imageUrl);
                intent.putExtra("path", path);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });
    }

    private void setUpComments() {
        Query query = db.collection(SefnetContract.QUESTIONS).document(articleId).collection(SefnetContract.REPLIES);

        FirestoreRecyclerOptions<CommentsClass> options = new FirestoreRecyclerOptions.Builder<CommentsClass>()
                .setQuery(query, CommentsClass.class).build();
        myAdapter = new CommentsAdapter(this, options);

        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentsRecycler.setAdapter(myAdapter);
        myAdapter.startListening();
    }

    private void sendComment() {
        String comment = etComments.getText().toString().trim();
        if (comment.length() > 0) {
            db.collection(SefnetContract.QUESTIONS).document(articleId)
                    .collection(SefnetContract.REPLIES).document(String.valueOf(System.currentTimeMillis()))
                    .set(new CommentsClass(email, comment));
            etComments.setText("");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (myAdapter != null) {
            myAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myAdapter != null) {
            myAdapter.stopListening();
        }
    }
}
