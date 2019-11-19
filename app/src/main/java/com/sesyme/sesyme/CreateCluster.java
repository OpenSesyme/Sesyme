package com.sesyme.sesyme;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sesyme.sesyme.Adapter.ClusterMembers;
import com.sesyme.sesyme.Adapter.MentionAdapter;
import com.sesyme.sesyme.Fragments.PaymentDialog;
import com.sesyme.sesyme.data.ClusterDetails;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.UserDetails;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateCluster extends AppCompatActivity {

    private static final int REQUEST_CODE_ICON = 909;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef;
    private MentionAdapter adaptor;
    private ListView listView;
    private ArrayList<UserDetails> usersList, addedUser;
    private ArrayList<String> admins, members;
    private ClusterMembers membersAdapter;
    private StorageReference groupIconRef;
    private ImageView icon, add;
    private Methods methods;
    private EditText groupMembers, groupName, description;
    private String privacy;
    private String name;
    private String url;
    private String clusterId;
    private RadioGroup privacyGroup;
    private RadioButton rPrivate, rPublic;
    private LinearLayout privacyLayout;
    private Uri iconUri;
    private TextView progressView, btCreate;
    private ProgressBar progressBar;
    private PaymentDialog dialog;
    private RecyclerView membersRecycler;
    private Boolean isPaid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupName = findViewById(R.id.et_group_name);
        description = findViewById(R.id.et_group_description);
        groupMembers = findViewById(R.id.et_group_members);
        privacyGroup = findViewById(R.id.group_privacy_radio);
        rPrivate = findViewById(R.id.group_private_button);
        rPublic = findViewById(R.id.group_public_button);
        progressBar = findViewById(R.id.create_group_progress);
        progressView = findViewById(R.id.create_group_progress_view);
        membersRecycler = findViewById(R.id.group_members_recycler);
        listView = findViewById(R.id.list_view_group);
        icon = findViewById(R.id.group_icon);
        privacyLayout = findViewById(R.id.group_privacy_layout);
        add = findViewById(R.id.add_icon_group);
        btCreate = findViewById(R.id.bt_create_cluster);
        ImageView back = findViewById(R.id.back_button_create_cluster);
        CollectionReference userDetails = db.collection(SefnetContract.USER_DETAILS);
        groupsRef = db.collection(SefnetContract.CLUSTERS_DETAILS);
        methods = new Methods(this);
        usersList = new ArrayList<>();
        addedUser = new ArrayList<>();
        admins = new ArrayList<>();
        members = new ArrayList<>();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressBar.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(SefnetContract.REFERENCE)) {
                clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
                if (clusterId != null) {
                    loadClusterDetails();
                }
            }
            Uri url = intent.getData();
            if (url != null) {
                String[] segments = url.toString().split("/");
                if (segments[segments.length - 1].equals("accepted")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Your payment was successful.");
                    builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    isPaid = true;
                } else if (segments[segments.length - 1].equals("cancelled")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Your payment was cancelled, you can still create a private cluster " +
                            "and upgrade it later.");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }

        userDetails.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                usersList.clear();
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    UserDetails user = snapshot.toObject(UserDetails.class);
                    usersList.add(user);
                }
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            admins.add(email);
            members.add(email);
        }

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        membersAdapter = new ClusterMembers(CreateCluster.this, addedUser);
        GridLayoutManager gm = new GridLayoutManager(this, 3);
        membersRecycler.setLayoutManager(gm);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        membersRecycler.addItemDecoration(itemDecoration);
        membersRecycler.setAdapter(membersAdapter);

        membersAdapter.setOnItemClickListener(new ClusterMembers.OnItemClickListener() {
            @Override
            public void onRemoveClicked(int position) {
                addedUser.remove(position);
                membersAdapter.notifyDataSetChanged();
            }
        });

        adaptor = new MentionAdapter(CreateCluster.this, usersList);
        listView.setAdapter(adaptor);
        adaptor.setOnItemClickListener(new MentionAdapter.onItemClicked() {
            @Override
            public void onUserClicked(UserDetails userDetails) {
                String fullName = userDetails.getFullName();
                if (!addedUser.contains(userDetails)) {
                    addedUser.add(userDetails);
                    groupMembers.setText("");
                    membersRecycler.scrollToPosition(addedUser.size() - 1);
                    membersAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CreateCluster.this, fullName + " is already added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        groupMembers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    adaptor.getFilter().filter(editable.toString().trim());
                } else {
                    listView.setVisibility(View.GONE);
                }
            }
        });

        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = groupName.getText().toString().trim();
                String groupDescription = description.getText().toString().trim();
                if (!name.isEmpty()) {
                    if (addedUser.size() > 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        saveGroup(name, groupDescription, onRadioButtonClicked(privacyGroup), url, admins, members);
                    } else {
                        methods.showToast("Please add at least one member");
                    }
                } else {
                    methods.showToast("Please insert group name");
                }
            }
        });
    }

    private void loadClusterDetails() {
        db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            String cluster = documentSnapshot.getString(SefnetContract.CLUSTER_NAME);
                            String clusterDesc = documentSnapshot.getString(SefnetContract.DESCRIPTION);
                            url = documentSnapshot.getString(SefnetContract.CLUSTER_ICON);
                            String privacyString = documentSnapshot.getString(SefnetContract.PRIVACY);
                            members = (ArrayList<String>) documentSnapshot.get(SefnetContract.MEMBERS);
                            isPaid = documentSnapshot.getBoolean("paid");
                            groupName.setText(cluster);
                            description.setText(clusterDesc);
                            groupName.setVisibility(View.GONE);
                            description.setVisibility(View.GONE);
                            privacyGroup.setVisibility(View.GONE);
                            icon.setVisibility(View.GONE);
                            add.setVisibility(View.GONE);
                            btCreate.setText(getResources().getString(R.string.update));
                            Glide.with(CreateCluster.this.getApplicationContext()).load(url).centerCrop().into(icon);
                            if (privacyString != null && privacyString.equals("Private")) {
                                rPrivate.setChecked(true);
                                privacyLayout.setVisibility(View.GONE);
                                privacyGroup.setVisibility(View.GONE);
                            } else {
                                rPublic.setChecked(true);
                            }
                            for (int i = 0; i < members.size(); i++) {
                                if (members.get(i) != null) {
                                    db.collection(SefnetContract.USER_DETAILS).document(members.get(i)).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot != null) {
                                                        UserDetails user = documentSnapshot.toObject(UserDetails.class);
                                                        if (user != null) {
                                                            addedUser.add(user);
                                                            membersAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent,
                                   @NotNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    public String onRadioButtonClicked(RadioGroup view) {
        // Is the button now checked?
        RadioButton button = findViewById(view.getCheckedRadioButtonId());
        boolean checked = button.isChecked();

        // Check which radio button was clicked
        switch (button.getId()) {
            case R.id.group_private_button:
                if (checked)
                    privacy = "Private";
                break;
            case R.id.group_public_button:
                if (checked)
                    privacy = "Public";
                break;
        }
        return privacy;
    }

    private void saveGroup(final String name, final String description, final String privacy, final String icon,
                           final List<String> admins, final ArrayList<String> members) {
        for (int i = 0; i < addedUser.size(); i++) {
            UserDetails userDetails = addedUser.get(i);
            String userEmail = userDetails.getuID();
            if (!LocateString(members, userEmail)) {
                members.add(userEmail);
            }
        }
        if (clusterId != null) {
            groupsRef.document(clusterId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e == null && documentSnapshot != null) {
                        isPaid = documentSnapshot.getBoolean("paid");
                    }
                }
            });
        }
        if (privacy.equals("Private")) {
            if (members.size() > 15 && !isPaid) {
                methods.showToast("Free private group cannot have more than 15 members, please remove some members");
                return;
            }
            dialog = new PaymentDialog();
            dialog.onAttach(this.getApplicationContext());
            dialog.show(getSupportFragmentManager(), "");
            progressBar.setVisibility(View.GONE);

            dialog.setOnClickListener(new PaymentDialog.paymentDialogListener() {
                @Override
                public void OnButtonClicked(String subscription) {
                    if (subscription == null) {
                        methods.showToast("Please select an option above");
                    } else {
                        if (subscription.equals("Free")) {
                            saveDetails(name, description, privacy, icon, admins, members);
                            dialog.dismiss();
                        } else {
                            if (clusterId == null) {
                                clusterId = name + " " + System.currentTimeMillis();
                            }
                            String url = "https://www.payfast.co.za/eng/process?cmd=_paynow&receiver" +
                                    "=14284627&item_name=Sesyme+Premium+Subscription&amount=60.00&return" +
                                    "_url=http%3A%2F%2Fwww.sesyme.com%2Fpayment%2Faccepted" +
                                    "&cancel_url=http%3A%2F%2Fwww.sesyme.com%2Fpayment%2Fcancelled";
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    }
                }
            });
        } else {
            if (privacy.equals("Public") && members.size() > 500) {
                methods.showToast("you can only have up to 500 members in public cluster, please remove some members");
                return;
            }
            saveDetails(name, description, privacy, icon, admins, members);
        }
    }

    private void saveDetails(String name, String description, String privacy, String icon,
                             List<String> admins, ArrayList<String> members) {
        if (clusterId == null) {
            clusterId = name + " " + System.currentTimeMillis();
            groupsRef.document(clusterId).set(new ClusterDetails(name, description, icon, admins,
                    members, privacy, clusterId, isPaid))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadImage();
                            }
                        }
                    });
        } else {
            groupsRef.document(clusterId).update("clusterName", name, "description", description,
                    "clusterIcon", icon, "admins", admins, "members", members, "privacy", privacy, "paid", isPaid)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadImage();
                            }
                        }
                    });
        }
    }

    private void openGallery() {
        if (methods.checkIfAlreadyHavePermission()) {
            ActivityCompat.requestPermissions(CreateCluster.this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ICON);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_ICON);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ICON && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                    .error(R.drawable.img).circleCrop().into(icon);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage() {
        groupIconRef = FirebaseStorage.getInstance()
                .getReference(name + " " + privacy + " Icon" + ".jpg");
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
                        progressView.setVisibility(View.GONE);
                        methods.showToast("Cluster Created");
                        finish();
                    } else {
                        methods.showToast("uploading icon failed: "
                                + Objects.requireNonNull(task.getException()).getMessage());
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            finish();
        }
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