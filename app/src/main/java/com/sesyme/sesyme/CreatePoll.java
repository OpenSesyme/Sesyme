package com.sesyme.sesyme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.PollDetails;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreatePoll extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText etTitle, etOption;
    private List<String> options;
    private LinearLayout layout;
    private Methods methods;
    private String clusterId, email;
    private Date duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

        etTitle = findViewById(R.id.et_poll_title);
        etOption = findViewById(R.id.et_poll_option);
        ImageView addOption = findViewById(R.id.bt_poll_add_option);
        Button create = findViewById(R.id.bt_poll_create);
        Spinner durationSpinner = findViewById(R.id.poll_duration_spinner);
        layout = findViewById(R.id.poll_options_layout);
        options = new ArrayList<>();
        methods = new Methods(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.REFERENCE)) {
            clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (options.size() < 4) {
                    String option = etOption.getText().toString().trim();
                    if (option.length() > 0) {
                        options.add(option);
                        TextView optionText = new TextView(CreatePoll.this);
                        optionText.setBackground(getResources().getDrawable(R.drawable.button_follow));
                        optionText.setText(option);
                        LinearLayout.LayoutParams params = new LinearLayout
                                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.bottomMargin = methods.dpToPx(8);
                        optionText.setLayoutParams(params);
                        optionText.setGravity(Gravity.CENTER);
                        int vertical = methods.dpToPx(6);
                        int horizontal = methods.dpToPx(12);
                        optionText.setPadding(horizontal, vertical, horizontal, vertical);
                        layout.addView(optionText);
                        etOption.setText("");
                    } else {
                        methods.showToast("Please provide an option");
                    }
                } else {
                    methods.showToast("You can only add four options");
                }
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (options.size() > 1) {
                    createPoll();
                } else {
                    methods.showToast("Please add at least two options");
                }
            }
        });

        ArrayAdapter<CharSequence> affiliationAdapter = ArrayAdapter.
                createFromResource(this, R.array.poll_duration, android.R.layout.simple_spinner_item);
        affiliationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(affiliationAdapter);
        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String d = adapterView.getItemAtPosition(i).toString().trim();

                Calendar c = Calendar.getInstance();
                switch (d) {
                    case "2 Days":
                        c.add(Calendar.DATE, 2);
                        break;
                    case "3 Days":
                        c.add(Calendar.DATE, 3);
                        break;
                    case "4 Days":
                        c.add(Calendar.DATE, 4);
                        break;
                    case "5 Days":
                        c.add(Calendar.DATE, 5);
                        break;
                    case "6 Days":
                        c.add(Calendar.DATE, 6);
                        break;
                    case "7 Days":
                        c.add(Calendar.DATE, 7);
                        break;
                    default:
                        c.add(Calendar.DATE, 1);
                }
                duration = c.getTime();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void createPoll() {
        String title = etTitle.getText().toString().trim();
        if (title.length() > 6) {
            if (clusterId != null && duration != null && email != null) {
                CollectionReference pollsRef = db.collection(SefnetContract.POLLS_DETAILS);
                pollsRef.document(String.valueOf(System.currentTimeMillis()))
                        .set(new PollDetails(clusterId, email, title, options, duration))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                methods.showToast("Poll Created");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 1000);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        methods.showToast("Something went wrong, please try again");
                    }
                });
            }
        } else {
            methods.showToast("Please provide more detailed question");
        }
    }
}
