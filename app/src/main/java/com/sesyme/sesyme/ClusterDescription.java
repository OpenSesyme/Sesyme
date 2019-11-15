package com.sesyme.sesyme;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sesyme.sesyme.Adapter.MembersAdapter;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.UserDetails;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ClusterDescription extends AppCompatActivity {

    private static final int REQUEST_CODE_ICON = 555;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef, groupsRef;
    private MembersAdapter adminAdapter, membersAdapter;
    private StorageReference groupIconRef;
    private Methods methods;
    private ImageView icon, edit;
    private TextView privacy;
    private TextView createdAt;
    private EditText clusterName;
    private EditText clusterDes;
    private TextView adminNumber;
    private TextView memberNumber;
    private TextView progressView, btRequests;
    private RecyclerView membersRecycler, adminsRecycler;
    private String clusterId, email, url;
    private Uri iconUri;
    private Button save;
    private ProgressBar progressBar;
    private ArrayList<String> members, admins, requests;
    private ArrayList<UserDetails> adminsList, membersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_description);

        ImageView addPeople = findViewById(R.id.bt_add_clustered);
        ImageView changeIcon = findViewById(R.id.bt_change_icon_clustered);
        icon = findViewById(R.id.icon_clustered);
        edit = findViewById(R.id.edit_name_clustered);
        privacy = findViewById(R.id.privacy_text_clustered);
        createdAt = findViewById(R.id.created_text_clustered);
        clusterName = findViewById(R.id.title_clustered);
        clusterDes = findViewById(R.id.description_clustered);
        adminNumber = findViewById(R.id.admin_number_clustered);
        memberNumber = findViewById(R.id.members_number_clustered);
        progressBar = findViewById(R.id.progress_clustered);
        progressView = findViewById(R.id.progress_view_clustered);
        LinearLayout polls = findViewById(R.id.poll_layout_clustered);
        LinearLayout quizzes = findViewById(R.id.quiz_layout_clustered);
        LinearLayout docs = findViewById(R.id.docs_layout_clustered);
        LinearLayout images = findViewById(R.id.images_layout_clustered);
        membersRecycler = findViewById(R.id.recycler_members_clustered);
        save = findViewById(R.id.bt_save_clustered);
        adminsRecycler = findViewById(R.id.recycler_admin_clustered);
        ImageView backButton = findViewById(R.id.back_button_clustered);
        btRequests = findViewById(R.id.requests_clustered);
        usersRef = db.collection(SefnetContract.USER_DETAILS);
        groupsRef = db.collection(SefnetContract.CLUSTERS_DETAILS);
        methods = new Methods(this);
        members = new ArrayList<>();
        membersList = new ArrayList<>();
        admins = new ArrayList<>();
        adminsList = new ArrayList<>();
        requests = new ArrayList<>();

        progressBar.setVisibility(View.GONE);

        clusterName.setEnabled(false);
        clusterDes.setEnabled(false);
        save.setVisibility(View.GONE);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clusterName.setEnabled(true);
                clusterDes.setEnabled(true);
                clusterName.requestFocus();
                clusterName.performClick();
                edit.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
            }
        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                String name = clusterName.getText().toString().trim();
                String description = clusterDes.getText().toString().trim();
                db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                        .update("clusterName", name, "description", description);
                clusterName.setEnabled(false);
                clusterDes.setEnabled(false);
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null){
            email = user.getEmail();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.REFERENCE)) {
            clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
        }

        if (clusterId != null) {
            db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        String cluster = documentSnapshot.getString(SefnetContract.CLUSTER_NAME);
                        String clusterDesc = documentSnapshot.getString(SefnetContract.DESCRIPTION);
                        String iconUrl = documentSnapshot.getString(SefnetContract.CLUSTER_ICON);
                        String privacyString = documentSnapshot.getString(SefnetContract.PRIVACY);
                        Date created = documentSnapshot.getDate(SefnetContract.CREATED_AT);
                        createdAt.setText(parseDateToddMMyyyy(String.valueOf(created)));
                        members = (ArrayList<String>) documentSnapshot.get(SefnetContract.MEMBERS);
                        admins = (ArrayList<String>) documentSnapshot.get(SefnetContract.ADMINS);
                        if (documentSnapshot.get("requests") != null) {
                            requests = (ArrayList<String>) documentSnapshot.get("requests");
                        }
                        if (admins != null && members != null) {
                            String adminNum;
                            String memberNum;
                            if (privacyString != null && privacyString.equals("Private")) {
                                adminNum = admins.size() + "/15";
                                memberNum = members.size() + "/200";
                            } else {
                                adminNum = admins.size() + "/10";
                                memberNum = members.size() + "/500";
                            }
                            memberNumber.setText(memberNum);
                            adminNumber.setText(adminNum);
                            setUpAdmins();
                            setUpMembers();
                        } else {
                            memberNumber.setVisibility(View.INVISIBLE);
                            adminNumber.setVisibility(View.INVISIBLE);
                        }
                        privacy.setText(privacyString);
                        clusterName.setText(cluster);
                        clusterDes.setText(clusterDesc);
                        Glide.with(ClusterDescription.this.getApplicationContext())
                                .load(iconUrl).error(R.drawable.img).into(icon);

                        if (requests != null && requests.size() > 0){
                            btRequests.setVisibility(View.VISIBLE);
                        }else {
                            btRequests.setVisibility(View.GONE);
                        }
                    }
                }
            });

            polls.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(ClusterDescription.this, ClusterAttachments.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    intent1.putExtra("attType", "Poll");
                    startActivity(intent1);
                }
            });

            quizzes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(ClusterDescription.this, ClusterAttachments.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    intent1.putExtra("attType", "Quiz");
                    startActivity(intent1);
                }
            });

            docs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(ClusterDescription.this, ClusterAttachments.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    intent1.putExtra("attType", "PDF");
                    startActivity(intent1);
                }
            });

            images.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(ClusterDescription.this, ClusterAttachments.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    intent1.putExtra("attType", "IMAGE");
                    startActivity(intent1);
                }
            });

            addPeople.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (admins.contains(email)) {
                        Intent intent1 = new Intent(ClusterDescription.this, CreateCluster.class);
                        intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                        startActivity(intent1);
                    }
                }
            });

            btRequests.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(ClusterDescription.this, RequestsActivity.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    startActivity(intent1);
                }
            });

            changeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGallery();
                }
            });
        }
    }

    private void setUpMembers() {
        for (int i = 0; i < members.size(); i++) {
            String id = members.get(i);
            if (id != null) {
                usersRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserDetails userDetails = documentSnapshot.toObject(UserDetails.class);
                        membersList.add(userDetails);
                    }
                });
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                membersAdapter = new MembersAdapter(ClusterDescription.this, membersList);
                membersRecycler.setLayoutManager(new LinearLayoutManager
                        (ClusterDescription.this, RecyclerView.HORIZONTAL, false));
                membersRecycler.setAdapter(membersAdapter);
            }
        }, 2000);
    }

    private void setUpAdmins() {
        for (int i = 0; i < admins.size(); i++) {
            String id = admins.get(i);
            if (id != null) {
                usersRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserDetails userDetails = documentSnapshot.toObject(UserDetails.class);
                        adminsList.add(userDetails);
                    }
                });
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adminAdapter = new MembersAdapter(ClusterDescription.this, adminsList);
                adminsRecycler.setLayoutManager(new LinearLayoutManager(
                        ClusterDescription.this, RecyclerView.HORIZONTAL, false));
                adminsRecycler.setAdapter(adminAdapter);
            }
        }, 3000);
    }

    public String parseDateToddMMyyyy(String time) {
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.getDefault());

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(time);
            if (date != null) {
                str = outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    private void openGallery(){
        if (methods.checkIfAlreadyHavePermission()) {
            ActivityCompat.requestPermissions(ClusterDescription.this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ICON);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_ICON);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_ICON && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            iconUri = data.getData();

            Glide.with(this.getApplicationContext()).load(iconUri)
                    .error(R.drawable.img).centerCrop().into(icon);
            if (iconUri != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.VISIBLE);
                uploadImage();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage(){
        groupIconRef = FirebaseStorage.getInstance()
                .getReference(clusterName + " " + privacy + " Icon" + ".jpg");
        if (iconUri != null) {
            groupIconRef.putFile(iconUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double percent = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    percent = methods.RoundOff(percent, 1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress((int) percent, true);
                    } else {
                        progressBar.setProgress((int) percent);
                    }
                    progressView.setVisibility(View.VISIBLE);
                    String progressText = percent + "%";
                    progressView.setText(progressText);

                }
            }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        if (task.getException() != null)
                            throw task.getException();
                    }
                    return groupIconRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        url = String.valueOf(downloadUri);
                        groupsRef.document(clusterId).update(SefnetContract.CLUSTER_ICON, url);
                        methods.showToast("Icon Changed");
                    } else {
                        methods.showToast("uploading icon failed: "
                                + Objects.requireNonNull(task.getException()).getMessage());
                    }
                    progressView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }else {
            progressBar.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
        }
    }
}
