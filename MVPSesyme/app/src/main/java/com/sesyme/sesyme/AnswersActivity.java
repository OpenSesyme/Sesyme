package com.sesyme.sesyme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.Adapter.QuestionsAdapter;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class AnswersActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference RepliesRef;
    private String imageUrl, descriptionString, tags;
    private String titleString, userImageUrl;
    private String userNameString, id, author;
    private String timeString;
    private TextView description, userName, timeView, tagsView, likesView, repliesView;
    private ImageView qImageView;
    private ImageView userImage;
    private QuestionsAdapter repliesAdapter;
    private RecyclerView repliesRecyclerView;
    private CollectionReference questionsRef;
    private TextView title, emptyText;
    private int currentVisPosition = 0;
    private Methods methods;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers);

        questionsRef = db.collection(SefnetContract.QUESTIONS);

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            List<String> params = deepLink.getPathSegments();
                            String type = params.get(params.size() - 3);
                            if (type.equals(SefnetContract.QUESTIONS)) {
                                id = params.get(params.size() - 2);
                                RepliesRef = db.collection(SefnetContract.QUESTIONS)
                                        .document(id).collection(SefnetContract.REPLY_REF);
                            } else {
                                id = params.get(params.size() - 5);
                                String path = params.get(params.size() - 6) + "/" + params.get(params.size() - 5) + "/"
                                        + params.get(params.size() - 4);
                                RepliesRef = db.collection(path);
                                currentVisPosition = Integer.parseInt(params.get(params.size() - 2));
                            }
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("AnswersActivity", "getDynamicLink:onFailure", e);
                    }
                });

        Intent intent = getIntent();
        if (intent.hasExtra(SefnetContract.REPLY_REF)) {
            id = intent.getStringExtra(SefnetContract.REPLY_REF);
        }

        Uri deep = intent.getData();

        methods = new Methods(this);

        FloatingActionButton floatingActionButton = findViewById(R.id.write_review_floating_button_book_view);
        floatingActionButton.setAlpha(0.5f);

        if (id != null) {
            RepliesRef = questionsRef.document(id).collection(SefnetContract.REPLIES);
        }

        repliesRecyclerView = findViewById(R.id.reviews_recycler_view_book_view);
        title = findViewById(R.id.title_question_answers);
        description = findViewById(R.id.description_answers);
        qImageView = findViewById(R.id.ratting_image_answers);
        userImage = findViewById(R.id.user_image_answers);
        userName = findViewById(R.id.user_name_answers);
        timeView = findViewById(R.id.date_time_answers);
        tagsView = findViewById(R.id.tags_post_answers);
        likesView = findViewById(R.id.txt_like_count_answers);
        repliesView = findViewById(R.id.txt_comment_count_answers);
        emptyText = findViewById(R.id.empty_answers);
        progress = findViewById(R.id.replies_progress_loading);
        ImageView back = findViewById(R.id.back_button);
        LinearLayout share = findViewById(R.id.share_answers_activity);
        Toolbar toolbar = findViewById(R.id.toolbar_answer);

        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle("");

        progress.setVisibility(View.VISIBLE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (deep != null) {
            List<String> params = deep.getPathSegments();
            String type = params.get(params.size() - 3);
            if (type.equals(SefnetContract.QUESTIONS)) {
                id = params.get(params.size() - 2);
                RepliesRef = db.collection(SefnetContract.QUESTIONS)
                        .document(id).collection(SefnetContract.REPLY_REF);
            } else {
                id = params.get(params.size() - 5);
                String path = params.get(params.size() - 6) + "/" + params.get(params.size() - 5) + "/"
                        + params.get(params.size() - 4);
                RepliesRef = db.collection(path);
                currentVisPosition = Integer.parseInt(params.get(params.size() - 2));
            }
        }

        if (id != null)
            questionsRef.document(id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();
                                if (snapshot != null) {
                                    imageUrl = snapshot.getString("imageUrl");
                                    descriptionString = snapshot.getString("description");
                                    titleString = snapshot.getString("title");
                                    author = snapshot.getString("author");
                                    String time = String.valueOf(snapshot.getDate("dateTime"));
                                    timeString = covertTimeToText(time);
                                    Long like = snapshot.getLong(SefnetContract.LIKES);
                                    if (like != null) {
                                        if (like.intValue() > 1) {
                                            String likes = snapshot.getLong(SefnetContract.LIKES) + " Likes";
                                            likesView.setText(likes);
                                        } else {
                                            String likes = snapshot.getLong(SefnetContract.LIKES) + " Like";
                                            likesView.setText(likes);
                                        }
                                    }
                                    Long repliesLong = snapshot.getLong(SefnetContract.COMMENTS);
                                    if (repliesLong != null) {
                                        if (repliesLong.intValue() > 1) {
                                            String replies = snapshot.getLong(SefnetContract.COMMENTS) + " Replies";
                                            repliesView.setText(replies);
                                        } else {
                                            String replies = snapshot.getLong(SefnetContract.COMMENTS) + " Reply";
                                            repliesView.setText(replies);
                                        }
                                    }
                                    if (snapshot.get("category") != null) {
                                        //noinspection unchecked
                                        ArrayList<String> list = (ArrayList<String>) snapshot.get("category");
                                        tags = methods.prepareTags(list);
                                        tagsView.setText(tags);
                                    }

                                    if (author != null) {
                                        DocumentReference user = db.collection(SefnetContract.USER_DETAILS).document(author);
                                        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                userNameString = documentSnapshot.getString(SefnetContract.FULL_NAME);
                                                userImageUrl = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                                                userName.setText(userNameString);
                                                Glide.with(AnswersActivity.this).load(userImageUrl)
                                                        .error(R.drawable.img).centerCrop().into(userImage);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AnswersActivity.this, "Failed to get Author details",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    title.setText(titleString);
                                    description.setText(descriptionString);
                                    if (descriptionString == null || descriptionString.length() == 0) {
                                        description.setVisibility(View.GONE);
                                    }
                                    timeView.setText(timeString);
                                    if (imageUrl != null) {
                                        qImageView.setVisibility(View.VISIBLE);
                                        Glide.with(AnswersActivity.this.getApplicationContext()).load(imageUrl)
                                                .error(R.drawable.pdf_icon).fitCenter().into(qImageView);
                                    }
                                }
                            }
                        }
                    });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.generateDeepLinkQuestion(questionsRef.document(id).getPath(), titleString, userNameString);
            }
        });

        qImageView.setVisibility(View.GONE);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent writeReviewIntent = new Intent(AnswersActivity.this, WritePostActivity.class);
                writeReviewIntent.putExtra(SefnetContract.REPLY_REF, id);
                writeReviewIntent.putExtra("type", "Reply");
                startActivity(writeReviewIntent);
            }
        });

        if (RepliesRef != null) {
            setUpRecyclerView();
        }
    }

    private void setUpRecyclerView() {
        Query query = RepliesRef.orderBy("dateTime", Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null) {
                    if (queryDocumentSnapshots.size() < 1) {
                        emptyText.setVisibility(View.VISIBLE);
                        repliesRecyclerView.setVisibility(View.GONE);
                    }
                    progress.setVisibility(View.GONE);
                }
            }
        });

        FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                .setQuery(query, WritePostClass.class).build();

        repliesAdapter = new QuestionsAdapter(this, options);

        repliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        repliesRecyclerView.setAdapter(repliesAdapter);

        repliesAdapter.setOnItemClickListener(new QuestionsAdapter.OnItemClickListener() {
            @Override
            public void onCommentClicked(DocumentSnapshot snapshot, int position) {
                String reference = snapshot.getId();
                Intent intent = new Intent(AnswersActivity.this, WritePostActivity.class);
                intent.putExtra(SefnetContract.REPLY_REF, reference);
                intent.putExtra("type", "Reply");
                startActivity(intent);
            }

            @Override
            public void onProfileClicked(DocumentSnapshot snapshot, int position) {
                Intent intent = new Intent(AnswersActivity.this, OthersProfile.class);
                intent.putExtra(SefnetContract.PROFILE_REF, snapshot.getString("author"));
                startActivity(intent);
            }

            @Override
            public void onTitleClicked(DocumentSnapshot snapshot, int position) {
            }
        });
        progress.setVisibility(View.GONE);
        repliesAdapter.startListening();
    }

    private String covertTimeToText(String dataDate) {

        String convTime = "Unknown";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            assert pasTime != null;
            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + "sec ";
            } else if (minute < 60) {
                convTime = minute + "min ";
            } else if (hour < 24) {
                convTime = hour + "h ";
            } else if (day >= 7) {
                if (day > 29 && day < 360) {
                    convTime = (day / 30) + "month/s ";
                } else if (day > 359) {
                    convTime = (day / 360) + "y ";
                } else {
                    convTime = (day / 7) + "w ";
                }
            } else {
                convTime = day + "d ";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvertTimeE", Objects.requireNonNull(e.getMessage()));
        }
        return convTime;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (repliesAdapter != null) {
            repliesAdapter.startListening();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentVisPosition != 0) {
                        if (repliesRecyclerView.getLayoutManager() != null) {
                            repliesRecyclerView.getLayoutManager().scrollToPosition(currentVisPosition);
                        }
                    }
                }
            }, 300);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (repliesAdapter != null)
            repliesAdapter.stopListening();
    }
}
