package com.sesyme.sesyme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sesyme.sesyme.Adapter.TagsAdapter;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;
import java.util.ArrayList;
import java.util.Objects;

public class WriteArticle extends AppCompatActivity {

    private static final int REQUEST_CODE_ARTICLE = 333;
    private static final int REQUEST_CODE_DOCS = 444;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QuestionsRef;
    private ArrayList<String> tagsList, list;
    private RecyclerView hashTags;
    private StorageReference ImageRef;
    private ImageView articleImage, cancelImage;
    private Methods methods;
    private EditText etTitle, etBody;
    private String type, email, attachment;
    private TextView progressText;
    private ProgressBar progressBar;
    private String attachmentType, id;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_article);

        hashTags = findViewById(R.id.article_course_recycler);
        progressBar = findViewById(R.id.article_progress_bar);
        progressText = findViewById(R.id.article_progress_text);
        etTitle = findViewById(R.id.article_title);
        etBody = findViewById(R.id.article_body);
        articleImage = findViewById(R.id.article_image);
        cancelImage = findViewById(R.id.article_cancel_image);
        TextView publish = findViewById(R.id.article_publish);
        ImageView back = findViewById(R.id.article_back);
        methods = new Methods(this);
        list = new ArrayList<>();

        cancelImage.setVisibility(View.GONE);
        QuestionsRef = db.collection(SefnetContract.QUESTIONS);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.TYPE)) {
            type = intent.getStringExtra(SefnetContract.TYPE);
        }

        if (type != null && type.equals("Article")) {
            etTitle.setHint("Title of your article");
            etBody.setHint("Write your article");
            Glide.with(this.getApplicationContext()).load(R.drawable.article_image).centerCrop().into(articleImage);
        }else {
            etTitle.setHint("Title of your document");
            etBody.setHint("Write a description for your document");
            Glide.with(this.getApplicationContext()).load(R.drawable.article_pin).centerCrop().into(articleImage);
        }

        articleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type != null && type.equals("Article")) {
                    if (checkIfAlreadyHavePermission()) {
                        ActivityCompat.requestPermissions(WriteArticle.this,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ARTICLE);
                    } else {
                        Intent imageIntent = new Intent(Intent.ACTION_PICK);
                        imageIntent.setType("image/*");
                        startActivityForResult(imageIntent, REQUEST_CODE_ARTICLE);
                    }
                }else {
                    if (checkIfAlreadyHavePermission()) {
                        ActivityCompat.requestPermissions(WriteArticle.this,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_DOCS);
                    } else {
                        Intent pdfIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        pdfIntent.setType("application/pdf");
                        startActivityForResult(pdfIntent, REQUEST_CODE_DOCS);
                    }
                }
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }

        cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = null;
                cancelImage.setVisibility(View.GONE);
                Glide.with(WriteArticle.this.getApplicationContext())
                        .load(R.drawable.select_image).into(articleImage);
            }
        });
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString().trim();
                String body = etBody.getText().toString().trim();
                if (type.equals("Article")) {
                    if (title.length() > 6 && body.length() > 299) {
                        postArticle();
                    } else {
                        methods.showToast("Your article must be at least 300 characters");
                    }
                } else {
                    if (title.length() < 10) {
                        methods.showToast("Please provide a valid title");
                    } else if (imageUri == null) {
                        methods.showToast("Please attach a document");
                    } else {
                        postArticle();
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setUpTags();
    }

    private void postArticle() {
        progressBar.setVisibility(View.VISIBLE);
        String title = etTitle.getText().toString().trim();
        String body = etBody.getText().toString().trim();
        id = String.valueOf(System.currentTimeMillis());
        final DocumentReference questionDoc = QuestionsRef.document(id);
        if (list.size() > 0) {
            questionDoc.set(new WritePostClass(list, title, email, "Article", body, attachment))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            imageTask(questionDoc);
                        }
                    });
        } else {
            methods.showToast("Please select a related course");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ARTICLE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_ARTICLE);
        } else if (requestCode == REQUEST_CODE_DOCS && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, REQUEST_CODE_DOCS);
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ARTICLE && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            cancelImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).error(R.drawable.maths).into(articleImage);
            attachmentType = "IMAGE";
        } else if (requestCode == REQUEST_CODE_DOCS && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            cancelImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.default_pdf)
                    .centerInside().into(articleImage);
            attachmentType = "PDF";
        }
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result != PackageManager.PERMISSION_GRANTED;
    }

    private void imageTask(final DocumentReference DocumentRef) {
        if (imageUri != null) {
            ImageRef = FirebaseStorage.getInstance()
                    .getReference(SefnetContract.ARTICLES + "/" + id);
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
                        attachment = String.valueOf(downloadUri);
                        DocumentRef.update("imageUrl", attachment, "attType", attachmentType);

                    } else {
                        Toast.makeText(WriteArticle.this, "upload failed: "
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
}
