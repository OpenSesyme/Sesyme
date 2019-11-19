package com.sesyme.sesyme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.sesyme.sesyme.DashboardActivity;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.SignUpProcess;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class SignUpFragment extends Fragment {

    private EditText inputEmail, inputPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private CheckBox chk;
    private int currPos;
    private ViewPager pager;
    private GoogleSignInClient mGoogleSignInClient;
    private Methods methods;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View signUpView = inflater.inflate(R.layout.activity_sign_up, container, false);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        methods = new Methods(getActivity());

        final RelativeLayout relativeLayout = signUpView.findViewById(R.id.snackBar_sign_up);

        Button btnSignUp = signUpView.findViewById(R.id.sign_up_button);
        inputEmail = signUpView.findViewById(R.id.email);
        inputPassword = signUpView.findViewById(R.id.password);
        progressBar = signUpView.findViewById(R.id.progress_bar_sign_up);
        chk = signUpView.findViewById(R.id.check_box_t_and_c);
        GoogleSignInButton btnGoogleLogin = signUpView.findViewById(R.id.btSignUpGoogleCustom);

        pager = ((SignUpProcess) getActivity()).findViewById(R.id.pager);
        if (pager.getCurrentItem() == 0) {
            getActivity().setTitle("Sign Up");
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() { // if save btn_free_trial clicked

            @Override
            public void onClick(View v) {
                currPos = pager.getCurrentItem();
                if (currPos == 0) {
                    if (chk.isChecked()) {
                        final String email = inputEmail.getText().toString().trim();
                        String password = inputPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(email)) {
                            Snackbar snackbar = Snackbar.make(relativeLayout, "Enter Email address", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }

                        if (TextUtils.isEmpty(password)) {
                            Snackbar snackbar = Snackbar.make(relativeLayout, "Enter  password",
                                    Snackbar.LENGTH_LONG);
                            snackbar.show();                            return;
                        }

                        if (password.length() < 6) {
                            Snackbar snackbar = Snackbar.make(relativeLayout, "Password too short,enter 6 minimum characters",
                                    Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }

                        if (methods.isValidEmailId(email)){
                            Snackbar snackbar = Snackbar.make(relativeLayout, "InValid Email address", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            return;
                        }

                        progressBar.setVisibility(View.VISIBLE);
                        //create user
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar.setVisibility(View.GONE);
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                Snackbar snackbar = Snackbar.make(relativeLayout, "User with this  Email address,already exists", Snackbar.LENGTH_LONG);
                                                snackbar.show();                                            }
                                        } else {
                                            Snackbar snackbar = Snackbar.make(relativeLayout, "User Created With This Email Address", Snackbar.LENGTH_LONG);
                                            snackbar.show();                                            pager.setCurrentItem(1);
                                            getActivity().setTitle("Create Profile");
                                        }
                                    }
                                });
                    } else {
                        Snackbar snackbar = Snackbar.make(relativeLayout, "Please Accept The Terms&Conditions Above", Snackbar.LENGTH_LONG);
                        snackbar.show();                    }
                }
            }
        });

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

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        return signUpView;
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
                Toast.makeText(getActivity(), "Sign In Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {

                            boolean newUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (newUser) {
                                pager.setCurrentItem(1);
                                getActivity().setTitle("Create Profile");
                            } else {
                                FirebaseUser user = auth.getCurrentUser();
                                assert user != null;
                                if (user.getDisplayName() != null) {
                                    Toast.makeText(getActivity(), "Welcome back " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getActivity(), "Welcome back " + user.getEmail(), Toast.LENGTH_LONG).show();
                                }
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent = new Intent(getActivity(), DashboardActivity.class);

                                intent.putExtra("user", user);
                                startActivity(intent);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getActivity(), "Sign In Failed", Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }
}
