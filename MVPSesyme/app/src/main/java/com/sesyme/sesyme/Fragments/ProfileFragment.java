package com.sesyme.sesyme.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.LoginActivity;
import com.sesyme.sesyme.MyPosts;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.SendFeedBack;
import com.sesyme.sesyme.Setting;
import com.sesyme.sesyme.SignUpProcess;
import com.sesyme.sesyme.data.SefnetContract;
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

@SuppressWarnings("ConstantConditions")
public class ProfileFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private ImageView profileImage, coverImage;
    private TextView nameTextView, numQuestions, numAnswers, courseView, varsityView;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Button saveBio;
    private EditText bioEdit;

    public ProfileFragment() {
        //Required empty Constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View profileView = inflater.inflate(R.layout.activity_profile, container, false);

        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        CollectionReference userInfoRef = db.collection(SefnetContract.USER_DETAILS);
        final DocumentReference userProfile = userInfoRef.document(email);

        TextView textViewEditProfile = profileView.findViewById(R.id.edit_profile_text_view);
        TextView textViewManageInterests = profileView.findViewById(R.id.manage_interests_text_view);
        TextView textViewLogout = profileView.findViewById(R.id.logout_text_profile);
        TextView textViewSettings = profileView.findViewById(R.id.settings_text_view_profile);
        TextView textViewInvite = profileView.findViewById(R.id.invite_text_view_profile);
        TextView textViewVerify = profileView.findViewById(R.id.verify_email_text_view);
        TextView textViewFeedback = profileView.findViewById(R.id.feedback);
        bioEdit = profileView.findViewById(R.id.bio_profile);
        LinearLayout answersLayout = profileView.findViewById(R.id.answers_layout_profile);
        LinearLayout questionsLayout = profileView.findViewById(R.id.questions_layout_profile);
        CollectionReference questionsRef = db.collection(SefnetContract.QUESTIONS);
        nameTextView = profileView.findViewById(R.id.name_profile);
        profileImage = profileView.findViewById(R.id.profile_image);
        coverImage = profileView.findViewById(R.id.cover_image_profile);
        numAnswers = profileView.findViewById(R.id.number_of_answers);
        numQuestions = profileView.findViewById(R.id.number_of_questions);
        courseView = profileView.findViewById(R.id.affiliation_profile);
        varsityView = profileView.findViewById(R.id.university_profile);
        saveBio = profileView.findViewById(R.id.save_bio);

        bioEdit.clearFocus();
        saveBio.setVisibility(View.GONE);
        bioEdit.setCursorVisible(false);
        bioEdit.setSelected(false);

        bioEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBio.setVisibility(View.VISIBLE);
                bioEdit.setCursorVisible(true);
            }
        });

        saveBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bio = bioEdit.getText().toString().trim();
                userProfile.update("bio", bio);
                saveBio.setVisibility(View.GONE);
                bioEdit.clearFocus();
                bioEdit.setCursorVisible(false);
                bioEdit.setSelected(false);
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(bioEdit.getWindowToken(), 0);
            }
        });

        textViewFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SendFeedBack.class));
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
                        Toast.makeText(getActivity(), "Verification Email sent please check your inbox", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        userProfile.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    String fName = (snapshot.getString(SefnetContract.FULL_NAME));
                    nameTextView.setText(fName);
                    String profile = snapshot.getString(SefnetContract.PROFILE_URL);
                    String cover = snapshot.getString(SefnetContract.COVER_URL);
                    Glide.with(getActivity().getApplicationContext()).load(profile).error(R.drawable.img).centerCrop().into(profileImage);
                    Glide.with(getActivity().getApplicationContext()).load(cover).error(R.drawable.leee).centerCrop().into(coverImage);
                    String course = snapshot.getString("affiliation") +
                            " (" + snapshot.getString("course") + ")";
                    if (snapshot.getString("affiliation") != null) {
                        courseView.setText(course);
                    } else {
                        courseView.setText(snapshot.getString("course"));
                    }
                    String university = snapshot.getString("university");
                    if (university != null){
                        varsityView.setText(university);
                    }
                    String bio = snapshot.getString("bio");
                    if (bio == null || bio.isEmpty()){
                        bioEdit.setHint(getString(R.string.write_something_about_yourself));
                    }else {
                        bioEdit.setText(bio);
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


        textViewEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(getActivity(), SignUpProcess.class);
                editProfileIntent.putExtra("edit", "profile");
                startActivity(editProfileIntent);
            }
        });

        answersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent answersIntent = new Intent(getActivity(), MyPosts.class);
                answersIntent.putExtra("author", email);
                answersIntent.putExtra("type", "Answer");
                startActivity(answersIntent);
            }
        });

        questionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent questionsIntent = new Intent(getActivity(), MyPosts.class);
                questionsIntent.putExtra("author", email);
                questionsIntent.putExtra("type", "Question");
                startActivity(questionsIntent);
            }
        });

        textViewManageInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent InterestsIntent = new Intent(getActivity(), SignUpProcess.class);
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
                msg.append(getActivity().getPackageName());

                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sesyme");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        textViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Setting.class));
            }
        });

        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        return profileView;
    }

    //sign out method
    private void signOut() {
        auth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }
}
