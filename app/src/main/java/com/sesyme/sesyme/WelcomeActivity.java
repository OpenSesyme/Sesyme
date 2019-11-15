package com.sesyme.sesyme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.sesyme.sesyme.Adapter.ProductTourFragment;
import com.sesyme.sesyme.data.CustomViewPager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity{

    static final int NUM_PAGES = 5;
    CustomViewPager pager;
    ScreenSlidePagerAdapter pagerAdapter;
    LinearLayout circles;
    Button register;
    Button login;
    Timer timer;
    boolean isOpaque = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            startActivity(new Intent(this, DashboardActivity.class));
        }
        //screen orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_welcome);
        register = findViewById(R.id.btn_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, SignUpProcess.class));
            }
        });
        login = findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        });
        pager = findViewById(R.id.view_pager_welcome);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new CrossfadePageTransformer());
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == NUM_PAGES - 1 && positionOffset > 0) {
                    if (isOpaque) {
                        pager.setBackgroundColor(Color.TRANSPARENT);
                        isOpaque = false;
                    }
                } else {
                    if (!isOpaque) {
                        pager.setBackgroundColor(getResources().getColor(R.color.background));
                        isOpaque = true;
                    }
                }
            }
            @Override
            public void onPageSelected(int position) {
                setIndicator(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        buildCircles();
        timer = new Timer();
        timer.scheduleAtFixedRate(new WelcomeActivity.SliderTimer(), 5000,5000);
        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    timer.cancel();
                }else if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_MOVE){
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new WelcomeActivity.SliderTimer(), 5000,5000);
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new WelcomeActivity.SliderTimer(), 3000,5000);
                }
                return false;
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pager != null) {
            pager.clearOnPageChangeListeners();
        }
    }
    private void buildCircles() {
        circles = findViewById(R.id.dotLayout);
        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (5 * scale + 0.5f);
        for (int i = 0; i < NUM_PAGES - 1; i++) {
            ImageView circle = new ImageView(this);
            circle.setImageResource(R.drawable.ic_swipe_indicator_white_18dp);
            ViewGroup.LayoutParams params = new ViewGroup
                    .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 30);
            circle.setLayoutParams(params);
            circle.setAdjustViewBounds(true);
            circle.setPadding(padding, 0, padding, 0);
            circles.addView(circle);
        }
        setIndicator(0);
    }
    private void setIndicator(int index) {
        if (index < NUM_PAGES - 1) {
            for (int i = 0; i < NUM_PAGES - 1; i++) {
                ImageView circle = (ImageView) circles.getChildAt(i);
                if (i == index) {
                    circle.setColorFilter(getResources().getColor(R.color.gray));
                } else {
                    circle.setColorFilter(getResources().getColor(android.R.color.transparent));
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            ProductTourFragment tp;
            switch (position) {
                case 0:
                    tp = ProductTourFragment.newInstance(R.layout.slider_1);
                    break;
                case 1:
                    tp = ProductTourFragment.newInstance(R.layout.slider_3);
                    break;
                case 2:
                    tp = ProductTourFragment.newInstance(R.layout.slider_2);
                    break;
                default:
                    tp = ProductTourFragment.newInstance(R.layout.slider_4);
            }
            return tp;
        }
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
    public class CrossfadePageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();
            View backgroundView = page.findViewById(R.id.welcome_fragment);
            View text_head = page.findViewById(R.id.heading);
            View text_content = page.findViewById(R.id.content);
            if (0 <= position && position < 1) {
                page.setTranslationX(pageWidth * -position);
            }
            if (-1 < position && position < 0) {
                page.setTranslationX(pageWidth * -position);
            }
            if (position <= -1.0f || position >= 1.0f) {
            } else if (position == 0.0f) {
            } else {
                if (backgroundView != null) {
                    backgroundView.setAlpha(1.0f - Math.abs(position));
                }
                if (text_head != null) {
                    text_head.setTranslationX(pageWidth * position);
                    text_head.setAlpha(1.0f - Math.abs(position));
                }
                if (text_content != null) {
                    text_content.setTranslationX(pageWidth * position);
                    text_content.setAlpha(1.0f - Math.abs(position));
                }
            }
        }
    }
    private class SliderTimer extends TimerTask {
        @Override
        public void run() {
            WelcomeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pager.getCurrentItem() < NUM_PAGES - 2) {
                        pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                    } else {
                        pager.setCurrentItem(0, true);
                    }
                }
            });
        }
    }
}