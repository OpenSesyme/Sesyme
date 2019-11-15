package com.sesyme.sesyme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sesyme.sesyme.data.Methods;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Methods methods;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        methods = new Methods(this);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Sign In");
        }

        final ScrollView scrollView = findViewById(R.id.snackBar);

        inputEmail = findViewById(R.id.etUsername);
        inputPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progress_bar_login);
        Button btnLogin = findViewById(R.id.btLogin);
        Button btnReset = findViewById(R.id.btForgotPassword);
        GoogleSignInButton btnGoogleLogin = findViewById(R.id.btSignInGoogleCustom);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user auth state is changed - user is not null
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                }
            }
        };

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Snackbar snackbar = Snackbar
                            .make(scrollView, "Enter Email address", Snackbar.LENGTH_LONG);
                    snackbar.show();                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Snackbar snackbar = Snackbar
                            .make(scrollView, "Enter password", Snackbar.LENGTH_LONG);
                    snackbar.show();                    return;
                }

                if (!methods.isValidEmailId(email)){
                    Toast.makeText(LoginActivity.this, "InValid Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Snackbar snackbar = Snackbar
                                                .make(scrollView, "Please make sure email & password are correct", Snackbar.LENGTH_LONG);
                                        snackbar.show();                                    }
                                } else {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                        if (user.getDisplayName() != null) {
                                            Snackbar snackbar = Snackbar
                                                    .make(scrollView, "Welcome", Snackbar.LENGTH_LONG);
                                            snackbar.show();                                        } else {
                                            Snackbar snackbar = Snackbar
                                                    .make(scrollView, "Welcome", Snackbar.LENGTH_LONG);
                                            snackbar.show();                                        }
                                    }
                                    finish();
                                }
                            }
                        });
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                String message;
                if (Objects.equals(e.getMessage(), "7:")){
                    message = "Network Error";
                }else if ("12051:".equals(e.getMessage())){
                    message = "Cancelled by user";
                }else {
                    message = "Error code: " + e.getMessage();
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            boolean newUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (newUser) {
                                Intent intent = new Intent(LoginActivity.this, SignUpProcess.class);
                                intent.putExtra("edit", "Create Profile");
                                startActivity(intent);
                            } else {
                                FirebaseUser user = auth.getCurrentUser();
                                assert user != null;
                                if (user.getDisplayName() != null) {
                                    Toast.makeText(getApplicationContext(), "Welcome back " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Welcome back " + user.getEmail(), Toast.LENGTH_LONG).show();
                                }
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);

                                intent.putExtra("user", user);
                                startActivity(intent);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Sign In Failed", Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // user auth state is changed - user is not null
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            }
        }
    };

    @Override
    public void onBackPressed() {
       startActivity(new Intent(this, WelcomeActivity.class));
    }
}
