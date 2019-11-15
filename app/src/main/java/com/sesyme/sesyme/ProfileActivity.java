package com.sesyme.sesyme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.UserDetails;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private ImageView profileImage, coverImage;
    private TextView nameTextView, numQuestions, numAnswers, courseView, varsityView, helpful, answered;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DocumentReference userProfile;
    private Button saveBio;
    private EditText bioEdit;
    private boolean isBioEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        CollectionReference userInfoRef = db.collection(SefnetContract.USER_DETAILS);
        userProfile = userInfoRef.document(email);

        TextView textViewEditProfile = findViewById(R.id.edit_profile_text_view);
        TextView textViewManageInterests = findViewById(R.id.manage_interests_text_view);
        TextView textViewLogout = findViewById(R.id.logout_text_profile);
        TextView textViewSettings = findViewById(R.id.settings_text_view_profile);
        TextView textViewInvite = findViewById(R.id.invite_text_view_profile);
        TextView textViewVerify = findViewById(R.id.verify_email_text_view);
        TextView textViewFeedback = findViewById(R.id.feedback);
        ImageView back = findViewById(R.id.back_button_profile);
        bioEdit = findViewById(R.id.bio_profile);
        LinearLayout answersLayout = findViewById(R.id.answers_layout_profile);
        LinearLayout questionsLayout = findViewById(R.id.questions_layout_profile);
        CollectionReference questionsRef = db.collection(SefnetContract.QUESTIONS);
        nameTextView = findViewById(R.id.name_profile);
        profileImage = findViewById(R.id.profile_image);
        coverImage = findViewById(R.id.cover_image_profile);
        numAnswers = findViewById(R.id.number_of_answers);
        numQuestions = findViewById(R.id.number_of_questions);
        courseView = findViewById(R.id.affiliation_profile);
        varsityView = findViewById(R.id.university_profile);
        saveBio = findViewById(R.id.save_bio_profile);
        helpful = findViewById(R.id.helpful_profile);
        answered = findViewById(R.id.answered_profile);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        saveBio.setText(getResources().getString(R.string.edit_bio));
        saveBio.setVisibility(View.VISIBLE);
        bioEdit.clearFocus();
        bioEdit.setCursorVisible(false);
        bioEdit.setSelected(false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        bioEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bioEdit.setCursorVisible(true);
                bioEdit.performClick();
            }
        });

        saveBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBioEditable){
                    bioEdit.setCursorVisible(true);
                    saveBio.setText(getResources().getString(R.string.edit_bio));
                    isBioEditable = false;
                    InputMethodManager imm = (InputMethodManager) ProfileActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    }
                }else {
                    isBioEditable = true;
                    String bio = bioEdit.getText().toString().trim();
                    userProfile.update("bio", bio);
                    saveBio.setText(getResources().getString(R.string.save_bio));
                    bioEdit.clearFocus();
                    bioEdit.setCursorVisible(false);
                    bioEdit.setSelected(false);
                    InputMethodManager imm = (InputMethodManager) ProfileActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(bioEdit.getWindowToken(), 0);
                    }
                }
            }
        });

        textViewFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, SendFeedBack.class));
            }
        });

        if (user != null && !user.isEmailVerified()) {
            textViewVerify.setVisibility(View.VISIBLE);
        }
        textViewVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Verification Email sent please check your inbox", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        userProfile.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null && snapshot != null) {
                    UserDetails user = snapshot.toObject(UserDetails.class);
                    if (user != null) {
                        nameTextView.setText(user.getFullName());
                        Glide.with(ProfileActivity.this.getApplicationContext()).load(user.getProfileUrl())
                                .error(R.drawable.img).centerCrop().into(profileImage);
                        Glide.with(ProfileActivity.this.getApplicationContext()).load(user.getCoverUrl())
                                .thumbnail(0.25f).error(R.drawable.leee).centerCrop().into(coverImage);
                        String course = user.getAffiliation() +
                                " (" + user.getCourse() + ")";
                        if (user.getAffiliation() != null) {
                            courseView.setText(course);
                        } else {
                            courseView.setText(user.getCourse());
                        }
                        String university = user.getUniversity();
                        if (university != null) {
                            varsityView.setText(university);
                        }
                        String bio = snapshot.getString("bio");
                        if (bio == null || bio.isEmpty()) {
                            bioEdit.setHint(getString(R.string.write_something_about_yourself));
                        } else {
                            bioEdit.setText(bio);
                        }
                    }
                }
            }
        });

        questionsRef.orderBy("dateTime", Query.Direction.DESCENDING)
                .whereEqualTo("author", email).whereEqualTo("type", "Question")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null && queryDocumentSnapshots != null){
                            numQuestions.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    }
                });

        db.collectionGroup("Replies")
                .whereEqualTo("author", email).whereEqualTo("type", "Answer")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null && queryDocumentSnapshots != null){
                            numAnswers.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    }
                });

        questionsRef.orderBy("dateTime", Query.Direction.DESCENDING)
                .whereEqualTo("author", email).whereEqualTo("type", "Question")
                .whereEqualTo("accepted", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null && queryDocumentSnapshots != null){
                            answered.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    }
                });

        db.collectionGroup("Replies")
                .whereEqualTo("author", email).whereEqualTo("type", "Answer")
                .whereEqualTo("accepted", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null && queryDocumentSnapshots != null){
                            helpful.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    }
                });

        textViewEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(ProfileActivity.this, SignUpProcess.class);
                editProfileIntent.putExtra("edit", "profile");
                startActivity(editProfileIntent);

            }
        });

        answersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent answersIntent = new Intent(ProfileActivity.this, MyPosts.class);
                answersIntent.putExtra("author", email);
                answersIntent.putExtra("type", "Answer");
                startActivity(answersIntent);
            }
        });

        questionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent questionsIntent = new Intent(ProfileActivity.this, MyPosts.class);
                questionsIntent.putExtra("author", email);
                questionsIntent.putExtra("type", "Question");
                startActivity(questionsIntent);
            }
        });

        textViewManageInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent InterestsIntent = new Intent(ProfileActivity.this, SignUpProcess.class);
                InterestsIntent.putExtra("edit", "interests");
                startActivity(InterestsIntent);
            }
        });

        textViewInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder msg = new StringBuilder();
                msg.append("Hey, I'm using Sesyme to ask and answer study related questions and share " +
                        "knowledge with students from different universities. Join me now by following the link bellow.");
                msg.append("\n");
                msg.append("https://play.google.com/store/apps/details?id=");
                msg.append(ProfileActivity.this.getPackageName());

                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sesyme");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        textViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, Setting.class));
            }
        });

        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    //sign out method
    private void signOut() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
