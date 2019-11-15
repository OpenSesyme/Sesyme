package com.sesyme.sesyme.allagi;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.sesyme.sesyme.DashboardActivity;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MenuListActivity extends AppCompatActivity {

    private final int ID_HOME = 0;
    private final int ID_EXPLORE = 1;
    private final int ID_NOTIFICATION = 2;
    private final int ID_ACCOUNT = 3;
    private Methods methods;
    RecyclerView recyclerView;
    MeowBottomNavigation bottomNavigation;
    ScrollableMenuRecyclerViewAdapter adapter;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<Integer> imagesList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    boolean clickable = true;
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        methods = new Methods(this);

        recyclerView = findViewById(R.id.recycler_view);

        bottomNavigation = findViewById(R.id.bottomNavigationMenu);

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.ic_home_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notifications_black_24dp));
//        bottomNavigation.add(new MeowBottomNavigation.Model(ID_Library, R.drawable.ic_library_books_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_person_black_24dp));

        bottomNavigation.show(ID_EXPLORE, false);

        bottomNavigation
                .setOnShowListener
                        (new Function1
                                <MeowBottomNavigation.Model, Unit>() {
                            @Override
                            public Unit invoke(MeowBottomNavigation.Model model) {
                                int id = model.getId();
                                if (id != ID_EXPLORE) {
                                    scroll(id);
                                }
                                switch (id) {
                                    case ID_HOME:
                                        methods.showToast("News Feed");
                                        break;
                                    case ID_EXPLORE:
                                        methods.showToast("Explore");
                                        break;
                                    case ID_NOTIFICATION:
                                        methods.showToast("Notification");
                                        break;
                                    case ID_ACCOUNT:
                                        methods.showToast("Profile");
                                        break;
                                }
                                return Unit.INSTANCE;
                            }
                        });

        clickable = true;

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            CollectionReference NotificationRef = db.collection("Notifications");
            NotificationRef.whereEqualTo("receiver", email).whereEqualTo("seen", 0)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                String count = String.valueOf(queryDocumentSnapshots.size());
                                if (queryDocumentSnapshots.size() > 0) {
                                    bottomNavigation.setCount(2, count);
                                } else {
                                    bottomNavigation.setCount(2, "0");
                                }
                            }
                        }
                    });
        }

        if (recyclerView != null && firstTime) {
            firstTime = false;
            list = getIntent().getStringArrayListExtra("list");
            imagesList = getIntent().getIntegerArrayListExtra("imagesList");
            adapter = new ScrollableMenuRecyclerViewAdapter(this, list, imagesList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            recyclerView.smoothScrollToPosition(list.size() - 1);
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


                if (list.size() <= 15) {

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("scrollTo", ID_HOME);
        startActivity(intent);
    }

    public static void startActivity(@NonNull Activity activity,
                                     ArrayList<String> list,
                                     ArrayList<Integer> imagesList) {
        Intent intent = new Intent(activity, MenuListActivity.class);
        intent.putStringArrayListExtra("list", list);
        intent.putIntegerArrayListExtra("imagesList", imagesList);
        activity.startActivity(intent);
    }

    private void scroll(int page){
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("scrollTo", page);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
