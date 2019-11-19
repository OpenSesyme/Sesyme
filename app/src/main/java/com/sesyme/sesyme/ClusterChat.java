package com.sesyme.sesyme;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sesyme.sesyme.Adapter.DoubleAdapter;
import com.sesyme.sesyme.Adapter.GeneralSearchAdapter;
import com.sesyme.sesyme.Adapter.MessagesAdapter;
import com.sesyme.sesyme.Fragments.AttachmentDialog;
import com.sesyme.sesyme.data.DoubleClass;
import com.sesyme.sesyme.data.MessageDetails;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import developer.semojis.Helper.EmojiconEditText;
import developer.semojis.actions.EmojIconActions;

public class ClusterChat extends AppCompatActivity implements AttachmentDialog.attachmentDialogListener {

    private static final int REQUEST_CODE_ANSWER_PDF = 121;
    private static final int REQUEST_CODE_ANSWER = 212;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView send, attach, emoji, btSearch, closeSearch, icon, inputImage, addPoll;
    private LinearLayout inputAttachment;
    private CollectionReference clusterRef, NotificationsRef, ClusterNotifsRef;
    private StorageReference ImageRef;
    private AttachmentDialog dialog;
    private TextView clusterName, emptyText;
    private Methods methods;
    private MessagesAdapter adapter;
    private DoubleAdapter pollsAdapter;
    private String clusterId, attachmentType;
    private Uri imageUri;
    private String email, imageUrl, privacy;
    private EmojiconEditText etMessage;
    private ArrayList<String> mentionNotification;
    private EditText etSearch;
    private ArrayList<DoubleClass> itemsList;
    private ArrayList<MessageDetails> messages;
    private RecyclerView messageRecycler, pollsRecycler;
    private ProgressBar loading;
    private List<String> members;
    private List<String> requests;
    private GeneralSearchAdapter sAdapter;
    private ListView messagesList;
    private Button btJoin;
    private Query query;
    private Boolean isPaid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        ImageView backButton = findViewById(R.id.chat_back_button);
        send = findViewById(R.id.chat_send);
        attach = findViewById(R.id.chat_attach);
        emoji = findViewById(R.id.chat_emoji);
        addPoll = findViewById(R.id.chat_add_poll);
        btSearch = findViewById(R.id.bt_search_chat);
        closeSearch = findViewById(R.id.chat_close_search);
        inputImage = findViewById(R.id.chat_input_image);
        inputAttachment = findViewById(R.id.chat_input_attachment_layout);
        icon = findViewById(R.id.chat_cluster_icon);
        etSearch = findViewById(R.id.et_chat_search);
        clusterName = findViewById(R.id.group_chat_name);
        emptyText = findViewById(R.id.empty_chat);
        etMessage = findViewById(R.id.chat_bottom_message_edit_text);
        messageRecycler = findViewById(R.id.chat_recycler_view);
        pollsRecycler = findViewById(R.id.chat_poll_recycler_view);
        loading = findViewById(R.id.progress_cluster_chat);
        messagesList = findViewById(R.id.chat_list_view);
        btJoin = findViewById(R.id.chat_bt_join);
        RelativeLayout rootLayout = findViewById(R.id.chat_root_view);
        NotificationsRef = db.collection(SefnetContract.NOTIFICATIONS);
        ClusterNotifsRef = db.collection(SefnetContract.NOTIFICATIONS_CLUSTER);
        methods = new Methods(this);
        mentionNotification = new ArrayList<>();
        itemsList = new ArrayList<>();
        messages = new ArrayList<>();
        requests = new ArrayList<>();

        btJoin.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.REFERENCE)) {
            clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
        }

        ImageRef = FirebaseStorage.getInstance()
                .getReference(clusterId + "/" + System.currentTimeMillis());

        inputAttachment.setVisibility(View.GONE);
        messageRecycler.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.GONE);
                messageRecycler.setVisibility(View.VISIBLE);
            }
        }, 3000);

        EmojIconActions emojIconActions = new EmojIconActions(this, rootLayout, etMessage, emoji);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "Open");
                emoji.setImageDrawable(getDrawable(R.mipmap.ic_mood));
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "Close");
                emoji.setImageDrawable(getDrawable(R.mipmap.ic_mood));
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        btJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (privacy.equals("Public")) {
                    if (members.size() < 500) {
                        members.add(email);
                        db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                                .update("members", members);
                        btJoin.setVisibility(View.GONE);
                        setUpMessages();
                    } else {
                        methods.showToast("This cluster is full");
                    }
                } else {
                    if (members.size() < 200) {
                        if (!LocateString(requests, email)) {
                            requests.add(email);
                            db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                                    .update("requests", requests);
                            methods.showToast("Your request has been sent");
                        } else {
                            methods.showToast("You have already requested to join this cluster");
                        }
                    } else {
                        methods.showToast("This cluster is full");
                    }
                }
            }
        });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0) {
                    addPoll.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);
                } else {
                    addPoll.setVisibility(View.VISIBLE);
                    send.setVisibility(View.GONE);
                }
            }
        });

        if (imageUri != null) {
            addPoll.setVisibility(View.GONE);
            send.setVisibility(View.VISIBLE);
        }

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSearch.setVisibility(View.VISIBLE);
                btSearch.setVisibility(View.GONE);
                etSearch.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                clusterName.setVisibility(View.GONE);
            }
        });

        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSearch.setVisibility(View.GONE);
                btSearch.setVisibility(View.VISIBLE);
                etSearch.setText("");
                etSearch.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                clusterName.setVisibility(View.VISIBLE);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAttachmentDialog();
            }
        });

        if (clusterId != null) {
            clusterRef = db.collection(clusterId);
            db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                methods.showToast("Something went wrong");
                                e.printStackTrace();
                            }
                            if (documentSnapshot != null && e == null) {
                                String cluster = documentSnapshot.getString(SefnetContract.CLUSTER_NAME);
                                String iconUrl = documentSnapshot.getString(SefnetContract.CLUSTER_ICON);
                                privacy = documentSnapshot.getString(SefnetContract.PRIVACY);
                                members = (List<String>) documentSnapshot.get("members");
                                requests = (List<String>) documentSnapshot.get("requests");
                                isPaid = documentSnapshot.getBoolean("paid");
                                clusterName.setText(cluster);
                                Glide.with(ClusterChat.this.getApplicationContext())
                                        .load(iconUrl).error(R.drawable.img).into(icon);
                            }
                        }
                    });

            db.collection(clusterId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        if (privacy.equals("Private")
                                && !isPaid && queryDocumentSnapshots.size() > 500){
                            etMessage.setEnabled(false);
                            attach.setEnabled(false);
                            addPoll.setEnabled(false);
                            emptyText.setText(getResources().getString(R.string.limit_reached));
                            emptyText.setVisibility(View.GONE);
                        }
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            MessageDetails message = snapshot.toObject(MessageDetails.class);
                            if (message != null) {
                                message.setMessagePath(snapshot.getReference().getPath());
                            }
                            messages.add(message);
                        }
                        if (clusterId.equals("General Cluster")) {
                            setUpMessages();
                        } else if (clusterId.equals("Help Cluster")) {
                            setUpMessages();
                        } else {
                            if (members.contains(email)) {
                                setUpMessages();
                            } else {
                                if (privacy.equals("Public")) {
                                    setUpMessages();
                                    btJoin.setVisibility(View.VISIBLE);
                                } else {
                                    btJoin.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }
            });

            sAdapter = new GeneralSearchAdapter(ClusterChat.this, messages);
            messagesList.setAdapter(sAdapter);

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.toString().length() > 0) {
                        sAdapter.getFilter().filter(editable.toString().toLowerCase());
                        messagesList.setVisibility(View.VISIBLE);
                        messageRecycler.setVisibility(View.GONE);
                    } else {
                        messagesList.setVisibility(View.GONE);
                        messageRecycler.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        clusterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clusterId.equals("General Cluster")) {
                    methods.showToast("This is a default cluster");
                } else if (clusterId.equals("Help Cluster")) {
                    methods.showToast("This is a default cluster");
                } else {
                    Intent intent1 = new Intent(ClusterChat.this, ClusterDescription.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    startActivity(intent1);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    private void setUpMessages() {
        if (clusterRef != null) {
            setUpPolls();
            query = clusterRef.orderBy(SefnetContract.SENT_AT, Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<MessageDetails> options = new FirestoreRecyclerOptions.Builder<MessageDetails>()
                    .setQuery(query, MessageDetails.class).build();
            adapter = new MessagesAdapter(this, options);
            LinearLayoutManager lm = new LinearLayoutManager(this);
            lm.setStackFromEnd(true);
            messageRecycler.setLayoutManager(lm);
            messageRecycler.setHasFixedSize(true);
            messageRecycler.setAdapter(adapter);
            adapter.startListening();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots
                                , @Nullable FirebaseFirestoreException e) {
                            if (e == null && queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() > 0) {
                                    messageRecycler.setVisibility(View.VISIBLE);
                                    emptyText.setVisibility(View.GONE);
                                    messageRecycler.scrollToPosition((queryDocumentSnapshots.size() - 1));
                                } else {
                                    emptyText.setVisibility(View.VISIBLE);
                                    messageRecycler.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                }
            }, 1000);

            adapter.setOnClickListener(new MessagesAdapter.onClickListener() {
                @Override
                public void onUserClicked(int position, String userEmail) {
                    db.collection(SefnetContract.USER_DETAILS).document(userEmail).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null){
                                String nameString = "@" + documentSnapshot.getString(SefnetContract.FULL_NAME);
                                methods.LocateString(mentionNotification, email);
                                etMessage.append(nameString + " ");
                            }
                        }
                    });
                }
            });
        }
    }

    private void setUpPolls() {
        Query query = db.collection(SefnetContract.POLLS_DETAILS).orderBy(SefnetContract.CREATED_AT,
                Query.Direction.DESCENDING).whereEqualTo(SefnetContract.REFERENCE, clusterId);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String creator = snapshot.getString(SefnetContract.CREATOR);
                        String title = snapshot.getString(SefnetContract.TITLE);
                        Date createdAt = snapshot.getDate(SefnetContract.CREATED_AT);
                        itemsList.add(new DoubleClass(creator, "Poll", title, snapshot.getReference().getPath(), createdAt));
                        pollsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        db.collection(SefnetContract.QUIZZES_DETAILS).orderBy(SefnetContract.RELEASE_QUIZ)
                .whereEqualTo(SefnetContract.REFERENCE, clusterId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                String creator = snapshot.getString(SefnetContract.CREATOR);
                                String title = snapshot.getString(SefnetContract.TITLE);
                                Date createdAt = snapshot.getDate(SefnetContract.RELEASE_QUIZ);
                                itemsList.add(new DoubleClass(creator, "Quiz", title, snapshot.getReference().getPath(), createdAt));
                                pollsAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });
        pollsAdapter = new DoubleAdapter(this, itemsList);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        pollsRecycler.setLayoutManager(lm);
        pollsRecycler.setHasFixedSize(true);
        pollsRecycler.setAdapter(pollsAdapter);
        pollsRecycler.setVisibility(View.VISIBLE);
    }

    private void sendMessage() {
        if (clusterRef != null) {
            //noinspection ConstantConditions
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty() || imageUri != null) {
                String id = String.valueOf(System.currentTimeMillis());
                DocumentReference reference = clusterRef.document(id);
                if (imageUri != null) {
                    reference.set(new MessageDetails(message, email, imageUri.toString(), attachmentType));
                    imageTask(reference);
                    inputAttachment.setVisibility(View.GONE);
                } else {
                    reference.set(new MessageDetails(message, email));
                }
                for (int i = 0; i < mentionNotification.size(); i++) {
                    String mentionEmail = mentionNotification.get(i);
                    if (!mentionEmail.equals(email)) {
                        String text = " mentioned you in a message";
                        NotificationsRef.document(mentionEmail + "Mention" + clusterId)
                                .set(new NotificationClass(clusterId, text, email, "Chat", mentionEmail));
                    }
                }
                for (int m = 0; m < members.size(); m++){
                    String member = members.get(m);
                    if (!member.equals(email)){
                        ClusterNotifsRef.document(member+id)
                                .set(new NotificationClass(clusterId, message, email, "message", member));
                    }
                }
                etMessage.setText("");
            }
        }
    }

    private void openAttachmentDialog() {
        dialog = new AttachmentDialog();
        dialog.onAttach(this.getApplicationContext());
        dialog.show(getSupportFragmentManager(), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ANSWER && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            inputAttachment.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            Glide.with(this.getApplicationContext()).load(imageUri).centerCrop().into(inputImage);
            inputAttachment.setVisibility(View.VISIBLE);
            attachmentType = "IMAGE";
            attach.setVisibility(View.GONE);
            send.setVisibility(View.VISIBLE);
            addPoll.setVisibility(View.GONE);
        } else if (requestCode == REQUEST_CODE_ANSWER_PDF && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            inputAttachment.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            Glide.with(this.getApplicationContext()).load(R.drawable.default_pdf).into(inputImage);
            inputAttachment.setVisibility(View.VISIBLE);
            attachmentType = "PDF";
            attach.setVisibility(View.GONE);
            send.setVisibility(View.VISIBLE);
            addPoll.setVisibility(View.GONE);
        }
    }

    private void imageTask(final DocumentReference DocumentRef) {
        if (imageUri != null) {
            ImageRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return ImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        imageUrl = String.valueOf(downloadUri);
                        DocumentRef.update("attachmentUrl", imageUrl, "attType", attachmentType);

                    } else {
                        Toast.makeText(ClusterChat.this, "upload failed: "
                                + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void OnItemClicked(int itemId) {
        if (itemId == R.id.pdf_attach) {
            if (methods.checkIfAlreadyHavePermission()) {
                ActivityCompat.requestPermissions(ClusterChat.this,
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ANSWER_PDF);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, REQUEST_CODE_ANSWER_PDF);
            }
        } else {
            if (methods.checkIfAlreadyHavePermission()) {
                ActivityCompat.requestPermissions(ClusterChat.this,
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ANSWER);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_ANSWER);
            }
        }
        dialog.dismiss();
    }

    private void openDialog() {
        if (privacy.equals("Public")) {
            Intent intent = new Intent(ClusterChat.this, CreateQuiz.class);
            intent.putExtra(SefnetContract.REFERENCE, clusterId);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ClusterChat.this);
            builder.setMessage("What would you like to create?");
            builder.setPositiveButton("Poll", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent1 = new Intent(ClusterChat.this, CreatePoll.class);
                    intent1.putExtra(SefnetContract.REFERENCE, clusterId);
                    startActivity(intent1);
                }
            });
            builder.setNegativeButton("Quiz", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(ClusterChat.this, CreateQuiz.class);
                    intent.putExtra(SefnetContract.REFERENCE, clusterId);
                    startActivity(intent);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private boolean LocateString(List<String> Array, String s) {
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
