package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.Adapter.QuestionsAdapter;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class OthersProfile extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id, userName, course, profileUrl, type;
    private TextView username, affiliation, answers, questions, emptyText, questionButton
            , answerButton, universityView, bioView, verified, helpful, answered;
    private CollectionReference questionsRef;
    private ImageView cover, profile;
    private RecyclerView recyclerViewBooks;
    private QuestionsAdapter mAdapter;
    private LinearLayout questionsLayout, answersLayout;
    private Boolean isVerified;
    private int countAnswers, countQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra(SefnetContract.PROFILE_REF)) {
            id = intent.getStringExtra(SefnetContract.PROFILE_REF);
        }

        username = findViewById(R.id.username_others_profile);
        affiliation = findViewById(R.id.course_others_profile);
        answers = findViewById(R.id.answers_others_profile);
        questions = findViewById(R.id.questions_others_profile);
        answerButton = findViewById(R.id.answers_button_others_profile);
        questionButton = findViewById(R.id.questions_button_others_profile);
        cover = findViewById(R.id.cover_image_others_profile);
        profile = findViewById(R.id.profile_pic_others_profile);
        recyclerViewBooks = findViewById(R.id.recycler_others_profile);
        emptyText = findViewById(R.id.empty_others_profile);
        answersLayout = findViewById(R.id.answers_layout_others);
        questionsLayout = findViewById(R.id.questions_layout_others);
        universityView = findViewById(R.id.university_other_profile);
        bioView = findViewById(R.id.bio_other_profile);
        verified = findViewById(R.id.verified_lecturer);
        helpful = findViewById(R.id.helpful_others_profile);
        answered = findViewById(R.id.answered_others_profile);
        questionsRef = db.collection(SefnetContract.QUESTIONS);
        type = "Question";

        if (id != null) {
            CollectionReference UserRef = db.collection(SefnetContract.USER_DETAILS);
            UserRef.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            userName = snapshot.getString(SefnetContract.FULL_NAME);
                            if (getSupportActionBar() != null){
                                getSupportActionBar().setTitle(userName);
                            }
                            String aff = snapshot.getString(SefnetContract.AFFILIATION);
                            course = aff + " ("
                                    + snapshot.getString(SefnetContract.COURSE) + ")";

                            profileUrl = snapshot.getString(SefnetContract.PROFILE_URL);
                            String university = snapshot.getString(SefnetContract.UNIVERSITY);
                            String bio = snapshot.getString(SefnetContract.BIO);
                            if (snapshot.getBoolean("verified") != null) {
                                isVerified = snapshot.getBoolean("verified");
                            }else {
                                isVerified = false;
                            }
                            universityView.setText(university);
                            if (bio != null && !bio.isEmpty()){
                                bioView.setText(bio);
                            }else {
                                String defaultBio;
                                if (userName != null) {
                                    defaultBio = userName + " did not write a bio";
                                }else {
                                    defaultBio = "This user did write a bio";
                                }
                                bioView.setText(defaultBio);
                            }
                            if (aff != null && aff.equals("Lecturer")){
                                verified.setVisibility(View.VISIBLE);
                            }else {
                                verified.setVisibility(View.GONE);
                            }

                            if (aff != null && aff.equals("Lecturer")) {
                                verified.setVisibility(View.VISIBLE);
                                if (isVerified){
                                    verified.setText(getResources().getString(R.string.verified));
                                }else {
                                    verified.setText(getResources().getString(R.string.un_verified));
                                }
                            }

                            Glide.with(OthersProfile.this).load(profileUrl)
                                        .error(R.drawable.img).centerCrop().into(profile);
                            Glide.with(OthersProfile.this).load(snapshot.getString("coverUrl"))
                                    .error(R.drawable.leee).centerCrop().into(cover);

                            username.setText(userName);
                            if (aff != null) {
                                affiliation.setText(course);
                            }
                        }
                    }
                }
            });
            db.collectionGroup("Replies")
                    .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, "Answer")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e == null && queryDocumentSnapshots != null){
                                countAnswers = queryDocumentSnapshots.size();
                                answers.setText(String.valueOf(countAnswers));
                            }
                        }
                    });

            questionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                    .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, SefnetContract.QUESTION)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e == null && queryDocumentSnapshots != null){
                                countQuestions = queryDocumentSnapshots.size();
                                questions.setText(String.valueOf(countQuestions));
                                if (countQuestions < 1 && type.equals("Question")){
                                    emptyText.setVisibility(View.VISIBLE);
                                    String message;
                                    if (userName != null) {
                                        message = userName + " has not posted any " + type + " yet";
                                    }else {
                                        message = "This user has not posted any " + type + " yet";
                                    }
                                    emptyText.setText(message);
                                    recyclerViewBooks.setVisibility(View.GONE);
                                }else {
                                    emptyText.setVisibility(View.GONE);
                                    recyclerViewBooks.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });

            db.collectionGroup("Replies")
                    .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, "Answer")
                    .whereEqualTo("accepted", true).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e == null && queryDocumentSnapshots != null){
                        int countHelpful = queryDocumentSnapshots.size();
                        if (countHelpful > 0) {
                            helpful.setText(String.valueOf(countHelpful));
                        }else{
                            helpful.setText("0");
                        }
                    }
                }
            });

            questionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                    .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, SefnetContract.QUESTION)
                    .whereEqualTo("accepted", true).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e == null && queryDocumentSnapshots != null){
                        int countAnswered = queryDocumentSnapshots.size();
                        if (countAnswered > 0) {
                            answered.setText(String.valueOf(countAnswered));
                        }else {
                            answered.setText("0");
                        }
                    }
                }
            });

            answersLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.stopListening();
                    type = SefnetContract.ANSWER;
                    if (countAnswers < 1) {
                        emptyText.setVisibility(View.VISIBLE);
                        String message;
                        if (userName != null) {
                            message = userName + " has not posted any answers yet";
                        }else {
                            message = "This user has not posted any answers yet";
                        }
                        emptyText.setText(message);
                        recyclerViewBooks.setVisibility(View.GONE);
                    }else {
                        emptyText.setVisibility(View.GONE);
                        recyclerViewBooks.setVisibility(View.VISIBLE);
                    }
                    changeButton(answersLayout, questionsLayout);
                    Query query = db.collectionGroup("Replies")
                            .orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                            .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, type);
                    setUpRecyclerView(query);
                    mAdapter.startListening();
                    answers.setTextColor(getResources().getColor(R.color.white));
                    questions.setTextColor(getResources().getColor(R.color.black));
                    answerButton.setTextColor(getResources().getColor(R.color.white));
                    questionButton.setTextColor(getResources().getColor(R.color.black));
                }
            });

            questionsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.stopListening();
                    type = SefnetContract.QUESTION;
                    if (countQuestions < 1) {
                        emptyText.setVisibility(View.VISIBLE);
                        String message;
                        if (userName != null) {
                            message = userName + " has not posted any questions yet";
                        }else {
                            message = "This user has not posted any questions yet";
                        }
                        emptyText.setText(message);
                        recyclerViewBooks.setVisibility(View.GONE);
                    }else {
                        emptyText.setVisibility(View.GONE);
                        recyclerViewBooks.setVisibility(View.VISIBLE);
                    }
                    Query query = questionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                            .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, type);
                    changeButton(questionsLayout, answersLayout);
                    setUpRecyclerView(query);
                    mAdapter.startListening();
                    answers.setTextColor(getResources().getColor(R.color.black));
                    questions.setTextColor(getResources().getColor(R.color.white));
                    answerButton.setTextColor(getResources().getColor(R.color.black));
                    questionButton.setTextColor(getResources().getColor(R.color.white));
                }
            });

        }

        Query query = questionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                .whereEqualTo("author", id).whereEqualTo(SefnetContract.TYPE, type);
        setUpRecyclerView(query);
    }

    private void setUpRecyclerView(Query query) {
        if (id != null) {
            FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                    .setQuery(query, WritePostClass.class).build();

            mAdapter = new QuestionsAdapter(this, options);

            recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewBooks.setAdapter(mAdapter);

            mAdapter.setOnItemClickListener(new QuestionsAdapter.OnItemClickListener() {
                @Override
                public void onCommentClicked(DocumentSnapshot snapshot, int position) {
                    String reference = snapshot.getId();
                    Intent intent = new Intent(OthersProfile.this, WritePostActivity.class);
                    intent.putExtra(SefnetContract.REFERENCE, reference);
                    intent.putExtra("type", "Reply");
                    startActivity(intent);
                }

                @Override
                public void onProfileClicked(DocumentSnapshot snapshot, int position) {
                    if (!id.equals(snapshot.getString("author"))) {
                        Intent intent = new Intent(OthersProfile.this, OthersProfile.class);
                        intent.putExtra(SefnetContract.PROFILE_REF, snapshot.getString("author"));
                        startActivity(intent);
                    }
                }

                @Override
                public void onTitleClicked(DocumentSnapshot snapshot, int position) {
                    if ("Question".equals(snapshot.getString("type"))) {
                        String id = snapshot.getId();
                        Intent intent = new Intent(OthersProfile.this, AnswersActivity.class);
                        intent.putExtra(SefnetContract.REFERENCE, id);
                        startActivity(intent);
                    }else {
                        String path = snapshot.getReference().getPath();
                        String[] segments = path.split("/");
                        String id = segments[1];
                        Intent intent = new Intent(OthersProfile.this, AnswersActivity.class);
                        intent.putExtra(SefnetContract.REFERENCE, id);
                        startActivity(intent);
                    }
                }
            });
            mAdapter.startListening();
        }else {
            Toast.makeText(this, "Something Wrong here!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private void changeButton(LinearLayout selected, LinearLayout unSelected ){
        selected.setBackground(getDrawable(R.drawable.bt_ui));
        unSelected.setBackground(getDrawable(R.drawable.button_follow));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
