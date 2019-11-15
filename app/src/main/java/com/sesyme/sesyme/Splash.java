package com.sesyme.sesyme;


import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashTread.start();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    Thread splashTread = new Thread() {
        @Override
        public void run() {
            try {
                int waited = 0;
                // Splash screen pause time
                while (waited < 3000) {
                    sleep(200);
                    waited += 200;
                }
                Intent intent = new Intent(Splash.this,
                        WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                Splash.this.finish();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    };
}