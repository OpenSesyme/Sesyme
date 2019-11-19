package com.sesyme.sesyme.allagi;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sesyme.sesyme.DashboardActivity;
import com.sesyme.sesyme.R;
import java.util.ArrayList;

public class MenuListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ScrollableMenuRecyclerViewAdapter adapter;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<Integer> imagesList = new ArrayList<>();
    private ImageView back;

    boolean clickable = true;
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        recyclerView = findViewById(R.id.recycler_view);
        back = findViewById(R.id.back_button_courses);
        clickable = true;

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuListActivity.this, DashboardActivity.class));
            }
        });

        if (recyclerView != null && firstTime) {
            firstTime = false;
            list = getIntent().getStringArrayListExtra("list");
            imagesList = getIntent().getIntegerArrayListExtra("imagesList");
            adapter = new ScrollableMenuRecyclerViewAdapter(this, list, imagesList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
        }

        adapter.setOnClickListener(new ScrollableMenuRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, Pair[] pairs) {

                if (!clickable) {
                    return;
                }

                clickable = false;

                System.gc();

                Intent intent = new Intent(MenuListActivity.this, ScrollableMenuActivity.class);
                intent.putExtra("viewPagerInitialPosition", position);
                intent.putStringArrayListExtra("list", list);
                intent.putIntegerArrayListExtra("imagesList", imagesList);


                if (list.size() <= 20) {

                    //noinspection unchecked
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MenuListActivity.this, pairs);
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }

            @Override
            public void scrollCallback(double pos) {
                recyclerView.smoothScrollToPosition(0);
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        clickable = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }

        final int position = ScrollableMenuActivity.getPosition(resultCode, data);

        if (position != -1) {
            recyclerView.scrollToPosition(position);
        }

        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startPostponedEnterTransition();
                }
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        clickable = true;

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void startActivity(@NonNull Activity activity,
                                     ArrayList<String> list,
                                     ArrayList<Integer> imagesList) {
        Intent intent = new Intent(activity, MenuListActivity.class);
        intent.putStringArrayListExtra("list", list);
        intent.putIntegerArrayListExtra("imagesList", imagesList);
        activity.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back.performClick();
    }
}
