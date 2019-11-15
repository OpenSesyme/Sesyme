package com.sesyme.sesyme;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    Button backToLoginBtn, btSubmit;
    private FirebaseAuth auth;
    private EditText inputEmail;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        //screen orientation

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final RelativeLayout relativeLayout = findViewById(R.id.forgot_reset);
        backToLoginBtn = findViewById(R.id.backToLoginBtn);
        btSubmit = findViewById(R.id.btSubmit);
        inputEmail = findViewById(R.id.registered_email_id);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        backToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Please Enter Registered  Email address", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Snackbar snackbar = Snackbar.make(relativeLayout,
                                            "We have sent you instructions to reset your password!", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(relativeLayout,
                                            "Failed to reset password, Please try again", Snackbar.LENGTH_LONG);
                                    snackbar.show();                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
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

