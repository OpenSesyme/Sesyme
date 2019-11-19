package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import java.util.ArrayList;

public class AnswerPoll extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference pollRef;
    private Methods methods;
    private ImageView creatorImage;
    private ArrayList<String> options, option1List, option2List, option3List, option4List;
    private TextView creatorName, pollTitle, option1, option2, option3, option4, total, expires;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_poll);

        creatorImage = findViewById(R.id.poll_answer_creator_image);
        creatorName = findViewById(R.id.poll_answer_creator_name);
        ImageView backButton = findViewById(R.id.poll_answer_back);
        pollTitle = findViewById(R.id.poll_answer_title);
        option1 = findViewById(R.id.poll_answer_option_1);
        option2 = findViewById(R.id.poll_answer_option_2);
        option3 = findViewById(R.id.poll_answer_option_3);
        option4 = findViewById(R.id.poll_answer_option_4);
        total = findViewById(R.id.poll_answered_total);
        expires = findViewById(R.id.poll_answer_expires);
        methods = new Methods(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.POLL_ID) && intent.hasExtra(SefnetContract.CREATOR)) {
            String path = intent.getStringExtra(SefnetContract.POLL_ID);
            String creator = intent.getStringExtra(SefnetContract.CREATOR);
            if (path != null && creator != null) {
                pollRef = db.document(path);
                DocumentReference creatorRef = db.collection(SefnetContract.USER_DETAILS).document(creator);
                creatorRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e == null && documentSnapshot != null) {
                            String url = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                            String name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                            creatorName.setText(name);
                            Glide.with(AnswerPoll.this.getApplicationContext()).load(url)
                                    .error(R.drawable.img).into(creatorImage);
                        }
                    }
                });

                pollRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e == null && documentSnapshot != null) {
                            String title = documentSnapshot.getString(SefnetContract.TITLE);
                            String expiryDate = String.valueOf(documentSnapshot.getDate(SefnetContract.DURATION));
                            String showTime = "Expires in " + methods.covertTimeToText(expiryDate);
                            expires.setText(showTime);
                            pollTitle.setText(title);
                            options = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTIONS);
                            option1List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_1);
                            option2List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_2);
                            option3List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_3);
                            option4List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_4);

                            if (LocateString(option1List, email) || LocateString(option2List, email)
                                    || LocateString(option3List, email) || LocateString(option4List, email)) {
                                disableOptions();
                            }else {
                                if (options != null) {
                                    option1.setText(options.get(0));
                                    option2.setText(options.get(1));
                                    if (options.size() < 3) {
                                        option4.setVisibility(View.GONE);
                                        option3.setVisibility(View.GONE);
                                    } else if (options.size() < 4) {
                                        option4.setVisibility(View.GONE);
                                        option3.setVisibility(View.VISIBLE);
                                        option3.setText(options.get(2));
                                    } else {
                                        option3.setVisibility(View.VISIBLE);
                                        option4.setVisibility(View.VISIBLE);
                                        option3.setText(options.get(2));
                                        option4.setText(options.get(3));
                                    }
                                    int Total = (option1List.size() + option2List.size() + option3List.size() + option4List.size());
                                    String totalPeople;
                                    if (Total < 1){
                                        totalPeople = "Nobody have voted yet";
                                    }else if (Total == 1){
                                        totalPeople = "One person voted";
                                    }else {
                                        totalPeople = Total + " People have voted so far";
                                    }
                                    total.setText(totalPeople);
                                }
                            }
                        }
                    }
                });
            }
        }

        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option1List.add(email);
                pollRef.update(SefnetContract.OPTION_1, option1List)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                disableOptions();
                            }
                        });
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option2List.add(email);
                pollRef.update(SefnetContract.OPTION_2, option2List)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                disableOptions();
                            }
                        });
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option3List.add(email);
                pollRef.update(SefnetContract.OPTION_3, option3List)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                disableOptions();
                            }
                        });
            }
        });

        option4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option4List.add(email);
                pollRef.update(SefnetContract.OPTION_4, option4List)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                disableOptions();
                            }
                        });
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void disableOptions() {
        pollRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null && documentSnapshot != null) {
                    option1List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_1);
                    option2List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_2);
                    option3List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_3);
                    option4List = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_4);

                    assert option4List != null;
                    int Total = (option1List.size() + option2List.size() + option3List.size() + option4List.size());
                    double percentOne, percentTwo, percentThree, percentFour;
                    String totalPeople;
                    if (Total != 0) {
                        int one = option1List.size();
                        int two = option2List.size();
                        int three = option3List.size();
                        int four = option4List.size();

                        percentOne = (one / Total) * 100;
                        percentTwo = (two / Total) * 100;
                        percentThree = (three / Total) * 100;
                        percentFour = (four / Total) * 100;
                        if (Total == 1){
                            totalPeople = "One person voted";
                        }else {
                            totalPeople = Total + " People have voted";
                        }
                    }else {
                        percentOne = 0;
                        percentTwo = 0;
                        percentThree = 0;
                        percentFour = 0;
                        totalPeople = "Nobody have voted yet";
                    }

                    total.setText(totalPeople);

                        String text1 = options.get(0) + "    " + percentOne + "%";
                        String text2 = options.get(1) + "    " + percentTwo + "%";
                        option1.setText(text1);
                        option2.setText(text2);
                        if (options.size() < 3) {
                            option4.setVisibility(View.GONE);
                            option3.setVisibility(View.GONE);
                        } else if (options.size() < 4) {
                            option4.setVisibility(View.GONE);
                            option3.setVisibility(View.VISIBLE);
                            String text3 = options.get(2) + "   " + percentThree + "%";
                            option3.setText(text3);
                        } else {
                            option3.setVisibility(View.VISIBLE);
                            option4.setVisibility(View.VISIBLE);
                            String text3 = options.get(2) + "    " + percentThree + "%";
                            String text4 = options.get(3) + "    " + percentFour + "%";
                            option3.setText(text3);
                            option4.setText(text4);
                        }

                    option1.setEnabled(false);
                    option2.setEnabled(false);
                    option3.setEnabled(false);
                    option4.setEnabled(false);

                    option1.setTextColor(getResources().getColor(R.color.white));
                    option2.setTextColor(getResources().getColor(R.color.white));
                    option3.setTextColor(getResources().getColor(R.color.white));
                    option4.setTextColor(getResources().getColor(R.color.white));

                    option1.setBackground(getDrawable(R.drawable.bt_ui));
                    option2.setBackground(getDrawable(R.drawable.bt_ui));
                    option3.setBackground(getDrawable(R.drawable.bt_ui));
                    option4.setBackground(getDrawable(R.drawable.bt_ui));
                }
            }
        });
    }

    private boolean LocateString(ArrayList<String> Array, String s) {
        boolean found = false;
        for (int i = (Array.size() - 1); i > -1; i--) {
            String element = Array.get(i);
            if (element.equals(s)) {
                found = true;
            }
        }

        return found;
    }
}
