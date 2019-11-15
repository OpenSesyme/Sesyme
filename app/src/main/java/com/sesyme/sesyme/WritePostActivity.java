package com.sesyme.sesyme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.Adapter.MentionAdapter;
import com.sesyme.sesyme.Adapter.TagsAdapter;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.WritePostClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.UserDetails;
import com.sesyme.sesyme.Fragments.AttachmentDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import developer.semojis.Helper.EmojiconEditText;
import developer.semojis.actions.EmojIconActions;

@SuppressWarnings("ConstantConditions")
public class WritePostActivity extends AppCompatActivity implements AttachmentDialog.attachmentDialogListener {

    @ServerTimestamp
    Date time;
    private String recipient, title, imageUrl, email, DocRef, qRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QuestionsRef, userDetails, NotificationsRef;
    private DocumentReference reviewDoc, notRef;
    private EmojiconEditText etDescription;
    private StorageReference ImageRef;
    private EditText etReplyTitle;
    private static final int REQUEST_CODE_ANSWER = 1201;
    private static final int REQUEST_CODE_ANSWER_PDF = 1202;
    private Button postAnswer, postComment, postQuestion;
    private ImageView replyImage, emojiButton;
    private ProgressBar progressBar;
    private RecyclerView hashTags;
    private Methods methods;
    private Uri imageUri;
    private Intent intent;
    private RelativeLayout imageLayout;
    private ArrayList<String> tagsList, list;
    private AttachmentDialog dialog;
    private MentionAdapter adaptor;
    private TextView pdfName, progressText;
    private ArrayList<UserDetails> usersList;
    private ArrayList<String> mentionNotification;
    private ListView listView;
    private int start, end;
    private String attachmentType, id, repRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        methods = new Methods(this);
        usersList = new ArrayList<>();
        mentionNotification = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        QuestionsRef = db.collection(SefnetContract.QUESTIONS);
        userDetails = db.collection(SefnetContract.USER_DETAILS);
        NotificationsRef = db.collection(SefnetContract.NOTIFICATIONS);

        etReplyTitle = findViewById(R.id.title_write_review);
        pdfName = findViewById(R.id.pdf_name);
        etDescription = findViewById(R.id.review_description_write_review);
        replyImage = findViewById(R.id.img_reply);
        progressBar = findViewById(R.id.progress_bar_post);
        postAnswer = findViewById(R.id.btn_answer_post);
        postComment = findViewById(R.id.btn_comment_post);
        postQuestion = findViewById(R.id.btn_question_post);
        ImageView attach = findViewById(R.id.attachment_answer);
        ImageView removeImage = findViewById(R.id.cancel_image);
        emojiButton = findViewById(R.id.emoji_button);
        TextView tagsHeading = findViewById(R.id.tags_label);
        hashTags = findViewById(R.id.hash_tag_text);
        LinearLayout replyLay = findViewById(R.id.layout_btn);
        imageLayout = findViewById(R.id.image_layout);
        listView = findViewById(R.id.suggestions_recycler);
        progressText = findViewById(R.id.progress_text_post);
        RelativeLayout rootLayout = findViewById(R.id.write_post_root);

        EmojIconActions emojIconActions = new EmojIconActions(this, rootLayout, etDescription, emojiButton);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "Open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "Close");
                emojiButton.setImageDrawable(getDrawable(R.mipmap.ic_mood));
            }
        });

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

        removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = null;
                imageLayout.setVisibility(View.GONE);
            }
        });

        if (imageUri != null) {
            imageLayout.setVisibility(View.VISIBLE);
        } else {
            imageLayout.setVisibility(View.GONE);
        }

        postComment.setEnabled(false);
        postQuestion.setEnabled(false);
        postAnswer.setEnabled(false);
        list = new ArrayList<>();

        setUpTags();

        intent = getIntent();
        if (intent.hasExtra("type")) {
            String id = intent.getStringExtra("type");
            if (id != null) {
                switch (id) {
                    case "Question":
                        replyLay.setVisibility(View.GONE);
                        if (getSupportActionBar() != null){
                            getSupportActionBar().setTitle("Write your Question");
                        }
                        loadQuestion(intent);
                        break;
                    case "Reply":
                        if (intent.hasExtra(SefnetContract.REFERENCE)) {
                            DocRef = intent.getStringExtra(SefnetContract.REFERENCE);
                        }
                        if (getSupportActionBar() != null){
                            getSupportActionBar().setTitle("Write your Reply");
                        }
                        attach.setImageDrawable(getDrawable(R.mipmap.ic_attachment));
                        tagsHeading.setVisibility(View.GONE);
                        postQuestion.setVisibility(View.GONE);
                        etReplyTitle.setVisibility(View.GONE);
                        hashTags.setVisibility(View.GONE);
                        break;
                }
            }
        }

        loadReply(intent);

        if (intent.hasExtra("tag")) {
            list = intent.getStringArrayListExtra("tag");
        }

        if (DocRef != null) {
            QuestionsRef.document(DocRef).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    recipient = snapshot.getString("author");
                    title = snapshot.getString("title");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        adaptor = new MentionAdapter(WritePostActivity.this, usersList);
        listView.setAdapter(adaptor);

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() < 6) {
                    disableButton(true, postAnswer);
                    disableButton(true, postComment);
                } else {
                    disableButton(false, postAnswer);
                    disableButton(false, postComment);
                }

                StringBuilder word = new StringBuilder();
                for (i = 0; i < charSequence.length(); i++) {
                    if (charSequence.charAt(i) == '@') {
                        start = i;
                        end = charSequence.length();
                        for (int n = i + 1; n < charSequence.length(); n++) {
                            if (charSequence.charAt(n) == ' ') {
                                i = n + 1;
                                word.delete(0, word.length());
                                break;
                            } else {
                                word.append(charSequence.charAt(n));
                            }
                        }
                    }
                    if (word.length() > 0) {
                        listView.setVisibility(View.VISIBLE);
                        if (word.length() < charSequence.length()) {
                            adaptor.getFilter().filter(word.toString());
                        } else {
                            adaptor.getFilter().filter(charSequence.subSequence(i, charSequence.length()));
                        }
                    } else {
                        listView.setVisibility(View.GONE);
                    }

                    adaptor.setOnItemClickListener(new MentionAdapter.onItemClicked() {
                        @Override
                        public void onUserClicked(UserDetails userDetails) {
                            String UserName = "@" + userDetails.getFullName();
                            String email = userDetails.getuID();
                            methods.LocateString(mentionNotification, email);
                            SpannableString text = new SpannableString(UserName);
                            text.setSpan(new ForegroundColorSpan(getResources()
                                    .getColor(R.color.iconsColor)), 0, text.length(), 0);
                            text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
                            etDescription.getText().replace(start, end, text);
                            listView.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        etReplyTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() < 6) {
                    disableButton(true, postQuestion);
                } else {
                    disableButton(false, postQuestion);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postQuestion.getVisibility() == View.VISIBLE) {
                    if (checkIfAlreadyHavePermission()) {
                        ActivityCompat.requestPermissions(WritePostActivity.this,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ANSWER);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_CODE_ANSWER);
                    }
                } else {
                    openAttachmentDialog();
                }
            }
        });

        postQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableButton(true, postAnswer);
                disableButton(true, postComment);
                disableButton(true, postQuestion);
                progressBar.setVisibility(View.VISIBLE);
                saveQuestion();
            }
        });

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableButton(true, postAnswer);
                disableButton(true, postComment);
                disableButton(true, postQuestion);
                progressBar.setVisibility(View.VISIBLE);
                String replyType = "Comment";
                saveReply(replyType);
            }
        });

        postAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButton(true, postAnswer);
                disableButton(true, postComment);
                disableButton(true, postQuestion);
                progressBar.setVisibility(View.VISIBLE);
                String replyType = "Answer";
                saveReply(replyType);
            }
        });
    }

    private void openAttachmentDialog() {
        dialog = new AttachmentDialog();
        dialog.onAttach(this.getApplicationContext());
        dialog.show(getSupportFragmentManager(), "");
    }

    private void saveQuestion() {
        String title = methods.sentenceCaseForText(etReplyTitle.getText().toString().trim());
        String des = etDescription.getText().toString().trim();

        if (list.size() > 0) {
            DocumentReference questionDoc;
            if (intent.hasExtra("QuestionRefEdit")) {
                questionDoc = QuestionsRef.document(qRef);
                questionDoc.update("title", title, "description", des, "edited", true);
                imageTask(questionDoc, qRef);
            } else {
                id = String.valueOf(System.currentTimeMillis());
                questionDoc = QuestionsRef.document(id);
                questionDoc.set(new WritePostClass(list, title, email, "Question", des, imageUrl)).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DocumentReference questionRef = userDetails.document(email);
                                questionRef.update("numQuestions", FieldValue.increment(1))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(WritePostActivity.this,
                                                        "Question Posted", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                for (int i = 0; i < mentionNotification.size(); i++) {
                                    String mentionEmail = mentionNotification.get(i);
                                    if (!mentionEmail.equals(email)) {
                                        String text = " mentioned you in a Question.";
                                        NotificationsRef.document(mentionEmail + "Mention" + id)
                                                .set(new NotificationClass(id, text, email, "Mention", mentionEmail));
                                    }
                                }
                            }
                        });
                imageTask(questionDoc, id);
            }
        } else {
            Toast.makeText(this, "Please Choose a related Course", Toast.LENGTH_SHORT).show();
            disableButton(false, postAnswer);
            disableButton(false, postComment);
            disableButton(false, postQuestion);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void saveReply(final String type) {
        String review = etDescription.getText().toString().trim();
        if (!TextUtils.isEmpty(review)) {
            if (DocRef != null) {
                final String repId = String.valueOf(System.currentTimeMillis());
                reviewDoc = QuestionsRef.document(DocRef).collection(SefnetContract.REPLIES)
                        .document(repId);
                reviewDoc.set(new WritePostClass(email, time, type, review, imageUrl)).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (type.equals("Answer")) {
                                    DocumentReference userRef = userDetails.document(email);
                                    userRef.update("numAnswers", FieldValue.increment(1))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(WritePostActivity.this,
                                                            "Answer Posted", Toast.LENGTH_SHORT).show();
                                                    if (recipient != null && !email.equals(recipient) && DocRef != null) {
                                                        notRef = NotificationsRef.document(email + "Answer" + repId);
                                                        notRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot snapshot = task.getResult();
                                                                    if (snapshot != null && !snapshot.exists()) {
                                                                        String text = "answered your Question: " + title;
                                                                        NotificationsRef.document(email + "Answer" + DocRef)
                                                                                .set(new NotificationClass(DocRef, text,
                                                                                        email, "Answer", recipient));
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(WritePostActivity.this,
                                            "Comment Posted", Toast.LENGTH_SHORT).show();

                                    if (recipient != null && !email.equals(recipient) && DocRef != null) {
                                        notRef = NotificationsRef.document(email + "Comment" + repId);
                                        notRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot snapshot = task.getResult();
                                                    if (snapshot != null && !snapshot.exists()) {
                                                        String text = "commented on your Question: " + title;
                                                        NotificationsRef.document(email + "Comment" + DocRef)
                                                                .set(new NotificationClass(DocRef, text,
                                                                        email, "Comment", recipient));
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }

                                for (int i = 0; i < mentionNotification.size(); i++) {
                                    String mentionEmail = mentionNotification.get(i);
                                    if (!mentionEmail.equals(email)) {
                                        String text = " mentioned you in a " + type;
                                        NotificationsRef.document(mentionEmail + "Mention" + DocRef)
                                                .set(new NotificationClass(DocRef, text, email, "Mention", mentionEmail));
                                    }
                                }
                                imageTask(reviewDoc, repId);
                            }
                        });
                QuestionsRef.document(DocRef).update(SefnetContract.COMMENTS, FieldValue.increment(1));
            } else {
                reviewDoc.update("description", review, "type", type,
                        "edited", true);
                imageTask(reviewDoc, repRef);
            }
        } else {
            Toast.makeText(this, "Please Choose a related Course", Toast.LENGTH_SHORT).show();
            disableButton(false, postAnswer);
            disableButton(false, postComment);
            disableButton(false, postQuestion);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ANSWER && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_ANSWER);
        } else if (requestCode == REQUEST_CODE_ANSWER_PDF && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, REQUEST_CODE_ANSWER_PDF);
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ANSWER && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            replyImage.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            Glide.with(this).load(imageUri).error(R.drawable.pdf_icon).into(replyImage);
            imageLayout.setVisibility(View.VISIBLE);
            attachmentType = "IMAGE";
        } else if (requestCode == REQUEST_CODE_ANSWER_PDF && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            replyImage.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            String pdf = imageUri.getLastPathSegment();
            pdfName.setText(pdf);
            pdfName.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.default_pdf).into(replyImage);
            imageLayout.setVisibility(View.VISIBLE);
            attachmentType = "PDF";
        }
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result != PackageManager.PERMISSION_GRANTED;
    }

    private void imageTask(final DocumentReference DocumentRef, String id) {
        if (imageUri != null) {
            ImageRef = FirebaseStorage.getInstance()
                    .getReference(SefnetContract.QUESTIONS + "/" + id);
            ImageRef.putFile(imageUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double percent = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    percent = methods.RoundOff(percent, 1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress((int) percent, true);
                    } else {
                        progressBar.setProgress((int) percent);
                    }
                    progressText.setVisibility(View.VISIBLE);
                    String progressString = percent + "%";
                    progressText.setText(progressString);
                }
            }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                        DocumentRef.update("imageUrl", imageUrl, "attType", attachmentType);

                    } else {
                        Toast.makeText(WritePostActivity.this, "upload failed: "
                                + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    finish();
                }
            });
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    finish();
                }
            }, 2000);
        }
    }

    private void setUpTags() {
        tagsList = new ArrayList<>();
        tagsList.add(0, "Accounting");
        tagsList.add(1, "Arts");
        tagsList.add(2, "Biology");
        tagsList.add(3, "Business");
        tagsList.add(4, "Chemistry");
        tagsList.add(5, "Computer Science");
        tagsList.add(6, "Economics");
        tagsList.add(7, "Education");
        tagsList.add(8, "Engineering");
        tagsList.add(9, "Health");
        tagsList.add(10, "Humanities");
        tagsList.add(11, "Law");
        tagsList.add(12, "Mathematics");
        tagsList.add(13, "Multimedia");
        tagsList.add(14, "Physics");
        tagsList.add(15, "Agriculture");
        tagsList.add(16, "Theology");
        tagsList.add(17, "Geography");
        tagsList.add(18, "Student Life");
        tagsList.add(19, "General");

        TagsAdapter adapter = new TagsAdapter(this, tagsList);
        hashTags.setHasFixedSize(true);
        hashTags.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        hashTags.setAdapter(adapter);
        adapter.setOnClickListener(new TagsAdapter.OnClickListener() {
            @Override
            public void onTagSelected(int Position, TextView tagView) {
                methods.LocateStringTextView(list, tagsList.get(Position), tagView);
            }
        });
    }

    private void loadQuestion(Intent intent) {
        if (intent.hasExtra("QuestionRefEdit")) {
            qRef = intent.getStringExtra("QuestionRefEdit");
            if (qRef != null) {
                QuestionsRef.document(qRef).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        String description = snapshot.getString("description");
                        String title = snapshot.getString("title");
                        String url = snapshot.getString("imageUrl");
                        //noinspection unchecked
                        list = (ArrayList<String>) snapshot.get("category");
                        etDescription.setText(description);
                        etReplyTitle.setText(title);
                        if (url != null) {
                            Glide.with(WritePostActivity.this).load(url).into(replyImage);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }

    private void loadReply(Intent intent) {
        if (intent.hasExtra("ReplyRefEdit")) {
            repRef = intent.getStringExtra("ReplyRefEdit");
            if (repRef != null) {
                reviewDoc = db.document(repRef);
                reviewDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        String description = snapshot.getString("description");
                        String url = snapshot.getString("imageUrl");
                        etDescription.setText(description);
                        if (url != null) {
                            Glide.with(WritePostActivity.this).load(url).into(replyImage);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }

    private void disableButton(Boolean yes, Button button) {
        if (yes) {
            button.setEnabled(false);
            button.setBackground(getDrawable(R.drawable.button_follow));
            button.setTextColor(getResources().getColor(R.color.black));
        } else {
            button.setEnabled(true);
            button.setBackground(getDrawable(R.drawable.bt_ui));
            button.setTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onBackPressed() {
        if (etReplyTitle.getText().toString().trim().length() != 0 || etDescription.getText().toString().trim().length() != 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(WritePostActivity.this);
            builder1.setMessage("You have not posted yet, Leave anyway?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            etDescription.setText("");
                            etReplyTitle.setText("");
                            onBackPressed();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnItemClicked(int itemId) {
        if (itemId == R.id.pdf_attach) {
            if (checkIfAlreadyHavePermission()) {
                ActivityCompat.requestPermissions(WritePostActivity.this,
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ANSWER_PDF);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, REQUEST_CODE_ANSWER_PDF);
            }
        } else {
            if (checkIfAlreadyHavePermission()) {
                ActivityCompat.requestPermissions(WritePostActivity.this,
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
}
