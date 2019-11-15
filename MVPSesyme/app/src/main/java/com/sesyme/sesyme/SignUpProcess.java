package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.sesyme.sesyme.data.PagerAdapterSignUp;
import com.rakshakhegde.stepperindicator.StepperIndicator;

public class SignUpProcess extends AppCompatActivity {

    private Boolean backed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stepper_indicator_main);

        Intent intent = getIntent();

        backed = false;

        ViewPager pager = findViewById(R.id.pager);
        assert pager != null;
        pager.setAdapter(new PagerAdapterSignUp(getSupportFragmentManager()));

        StepperIndicator indicator = findViewById(R.id.stepper_indicator);
        // We keep last page for a "finishing" page
        indicator.setViewPager(pager, true);

        if (pager.getCurrentItem() == 0){
            if(getSupportActionBar() != null){
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        if (intent.hasExtra("edit")) {
            String text = intent.getStringExtra("edit");
            assert text != null;
            switch (text) {
                case "profile":
                    indicator.setVisibility(View.GONE);
                    pager.setCurrentItem(1);
                    setTitle("Edit Profile");
                    if(getSupportActionBar() != null){
                        getSupportActionBar().setHomeButtonEnabled(true);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                    break;
                case "interests":
                    indicator.setVisibility(View.GONE);
                    pager.setCurrentItem(2);
                    setTitle("Manage Interests");
                    if(getSupportActionBar() != null){
                        getSupportActionBar().setHomeButtonEnabled(true);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                    break;
                case "Create Profile":
                    pager.setCurrentItem(1);
                    setTitle("Create Profile");
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void backButtonHandler() {
        if (backed) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
            backed = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (getTitle().equals("Create Profile")) {
            backButtonHandler();
        }else {
            super.onBackPressed();
        }
    }
}
