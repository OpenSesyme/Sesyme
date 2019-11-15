package com.sesyme.sesyme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.data.Methods;

public class ViewImage extends AppCompatActivity {

    private ImageView postImage;
    private Methods methods;
    private String url;
    private ProgressBar progressBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        methods = new Methods(this);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        postImage = findViewById(R.id.full_image);
        ImageView download = findViewById(R.id.download_image);
        progressBar = findViewById(R.id.progress_view_image);
        textView = findViewById(R.id.progress_text_view_image);
        progressBar.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) {
            url = intent.getStringExtra("imageUrl");
        }
        Glide.with(this.getApplicationContext()).load(url).error(R.drawable.pdf_icon).into(postImage);


        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getResources().getDrawable(R.drawable.pdf_icon).getConstantState() == postImage.getDrawable().getConstantState()){
                    Toast.makeText(ViewImage.this, "Downloading Pdf", Toast.LENGTH_SHORT).show();
                    methods.downloadPdfFile(url, "pdf", textView, progressBar);
                }else {
                    Toast.makeText(ViewImage.this, "Downloading Image", Toast.LENGTH_SHORT).show();
                    methods.downloadPdfFile(url, "jpg", textView, progressBar);
                }
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
