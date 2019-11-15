package com.sesyme.sesyme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sesyme.sesyme.data.SendMail;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SendFeedBack extends AppCompatActivity {

    private   EditText subject;
    private EditText description;
    private String email, type, doc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_feedback);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            email = user.getEmail();
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        final RelativeLayout relativeLayout = findViewById(R.id.relative);
        Button send = findViewById(R.id.feedback_submit);
        Button cancel = findViewById(R.id.cancel_feedback);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        subject = findViewById(R.id.etSendFeedbackSubject);
        description = findViewById(R.id.etProvideDetails);

        Intent intent = getIntent();
        if (intent.hasExtra("type") && intent.hasExtra("postRef")){
            type = intent.getStringExtra("type");
            doc = intent.getStringExtra("postRef");
            if (type != null && type.equals("Report")) {
                subject.setHint("What Would you like to report.(Optional)");
                TextView heading = findViewById(R.id.compose_feedback);
                heading.setText("Report");
                if (getSupportActionBar() != null){
                    getSupportActionBar().setTitle("Report");
                }
            }else{
                subject.setHint("Title(Optional).");
                if (getSupportActionBar() != null){
                    getSupportActionBar().setTitle("Feedback");
                }
            }
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sub = subject.getText().toString().trim();
                String des = description.getText().toString().trim();
                if (email != null && !des.isEmpty()) {
                    String body;
                    if (type != null && type.equals("Report")) {
                        body = "Email: " + email + "\n\n" + "Reporting: " + doc + "\n\n" + des;
                    }else {
                        body = "Email: " + email + "\n\n" + des;
                    }

                    SendMail sm = new SendMail(SendFeedBack.this, "all@innet.co.za", sub, body);
                    sm.execute();
                }else{
                    if (des.isEmpty()){
                        description.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        }
                    }
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Please provide more details", Snackbar.LENGTH_LONG);
                    snackbar.show();                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
