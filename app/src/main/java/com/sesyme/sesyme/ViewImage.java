package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesyme.sesyme.data.Methods;

public class ViewImage extends AppCompatActivity {

    private Methods methods;
    private String url;
    private ProgressBar progressBar;
    private TextView textView;
    private String type, path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        methods = new Methods(this);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        ImageView postImage = findViewById(R.id.full_image);
        ImageView download = findViewById(R.id.download_image);
        progressBar = findViewById(R.id.progress_view_image);
        textView = findViewById(R.id.progress_text_view_image);
        progressBar.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) {
            url = intent.getStringExtra("imageUrl");
            type = intent.getStringExtra("attType");
            path = intent.getStringExtra("path");
        }
        if (type != null && type.equals("PDF")){
            Glide.with(this.getApplicationContext()).load(R.drawable.default_pdf).into(postImage);
        }else {
            Glide.with(this.getApplicationContext()).load(url).thumbnail(0.20f)
                    .error(R.drawable.default_pdf).into(postImage);
        }


        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type != null && type.equals("PDF")){
                    Toast.makeText(ViewImage.this, "Downloading Pdf", Toast.LENGTH_SHORT).show();
                    methods.downloadPdfFile(url, "pdf", textView, progressBar);
                }else {
                    Toast.makeText(ViewImage.this, "Downloading Image", Toast.LENGTH_SHORT).show();
                    methods.downloadPdfFile(url, "jpeg", textView, progressBar);
                }
                FirebaseFirestore.getInstance().document(path).update("downloads", FieldValue.increment(1));
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
