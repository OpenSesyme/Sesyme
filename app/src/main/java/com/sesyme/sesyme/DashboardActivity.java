package com.sesyme.sesyme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sesyme.sesyme.Adapter.DashboardAdaptor;
import com.sesyme.sesyme.Fragments.SampleFragment;
import com.sesyme.sesyme.allagi.Allagi;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;

import javax.annotation.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DashboardActivity extends AppCompatActivity {

    private final int ID_HOME = 0;
    private final int ID_EXPLORE = 1;
    private final int ID_GROUPS = 2;
    private final int ID_NOTIFICATION = 3;
//    private final int ID_ACCOUNT = 2;
    private MeowBottomNavigation bottomNavigation;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Fragment> fragmentsList;
    private ImageView searchIcon, closeSearch;
    private ArrayList<Integer> imagesList;
    private ArrayList<String> menuList;
    private EditText searchView;
    private ViewPager viewPager;
    private ImageView profile;
    private ImageView courses;
//    private TextView page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        viewPager = findViewById(R.id.view_pager);
        searchView = findViewById(R.id.et_search_home);
        searchIcon = findViewById(R.id.search_icon_home);
        closeSearch = findViewById(R.id.close_icon_home);
        courses = findViewById(R.id.courses_button);
        profile = findViewById(R.id.logo_dashboard);
//        page = findViewById(R.id.page_dashboard);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
            }
        });

        DashboardAdaptor adaptor = new DashboardAdaptor(getSupportFragmentManager());

        viewPager.setAdapter(adaptor);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (viewPager.getCurrentItem()) {
                    case ID_HOME:
                        searchHome();
                        break;
                    case ID_EXPLORE:
                        searchPortal();
                        break;
                    case ID_GROUPS:
                        searchClusters();
                        break;
                    default:
                        searchIcon.setVisibility(View.GONE);
                }
            }
        });

        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearch();
                stopSearch();
            }
        });

        if (viewPager.getCurrentItem() != ID_HOME) {
            hideSearch();
        }

        courses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategories();
            }
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.ic_home_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore_black_24dp));
//        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_books_stack_of_three));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_GROUPS, R.drawable.ic_group_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notifications_black_24dp));

        bottomNavigation
                .setOnShowListener
                        (new Function1
                                <MeowBottomNavigation.Model, Unit>() {
                            @Override
                            public Unit invoke(MeowBottomNavigation.Model model) {
                                int id = model.getId();
                                viewPager.setCurrentItem(id);
                                switch (id) {
                                    case ID_HOME:
//                                        page.setText(getResources().getString(R.string.news_feed));
                                        searchIcon.setVisibility(View.VISIBLE);
                                        courses.setVisibility(View.VISIBLE);
                                        break;
                                    case ID_EXPLORE:
//                                        page.setText(getResources().getString(R.string.info_portal));
                                        courses.setVisibility(View.GONE);
                                        break;
                                    case ID_GROUPS:
//                                        page.setText(getResources().getString(R.string.clusters));
                                        hideSearch();
                                        searchIcon.setVisibility(View.VISIBLE);
                                        break;
                                    case ID_NOTIFICATION:
                                        NotificationClick();
                                        break;
//                                    case ID_ACCOUNT:
////                                        page.setText(getResources().getString(R.string.books));
//                                        hideSearch();
//                                        break;
                                }
                                return Unit.INSTANCE;
                            }
                        });

        viewPager.setOffscreenPageLimit(3);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomNavigation.show(viewPager.getCurrentItem(), true);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            final String email = user.getEmail();
            if (email != null) {
                CollectionReference NotificationRef = db.collection("Notifications");
                NotificationRef.whereEqualTo("receiver", email).whereEqualTo("seen", 0)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (queryDocumentSnapshots != null) {
                                    String count = String.valueOf(queryDocumentSnapshots.size());
                                    if (queryDocumentSnapshots.size() > 0) {
                                        bottomNavigation.setCount(ID_NOTIFICATION, count);
                                    } else {
                                        bottomNavigation.setCount(ID_NOTIFICATION, "empty");
                                    }
                                }
                            }
                        });

                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult().getToken();
                            DocumentReference userProfile = db.
                                    collection(SefnetContract.USER_DETAILS).document(email);
                            userProfile.update("regToken", token);
                        }
                    }
                });
            }
        }

        if (user == null) {
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        } else if (auth.getCurrentUser().getEmail() == null) {
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        } else if (user.getEmail() != null) {
            String email = user.getEmail();
            DocumentReference userDoc = db.collection(SefnetContract.USER_DETAILS).document(email);
            userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Intent intent = new Intent(DashboardActivity.this, SignUpProcess.class);
                        intent.putExtra("edit", "Create Profile");
                        startActivity(intent);
                    } else {
                        String url = snapshot.getString(SefnetContract.PROFILE_URL);
                        Glide.with(DashboardActivity.this.getApplicationContext()).load(url)
                                .centerCrop().thumbnail(0.2f).into(profile);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        menuList = new ArrayList<>();     //menu titles
        imagesList = new ArrayList<>();      //menu backgrounds
        fragmentsList = new ArrayList<>();      //fragments for each menu headers in second activity

        menuList.add("ACCOUNTING");               //add titles
        menuList.add("AGRICULTURE");
        menuList.add("ARTS");
        menuList.add("BIOLOGY");
        menuList.add("BUSINESS");
        menuList.add("CHEMISTRY");
        menuList.add("COMPUTER SCIENCES");
        menuList.add("ECONOMICS");
        menuList.add("EDUCATION");
        menuList.add("ENGINEERING");
        menuList.add("GEOGRAPHY");
        menuList.add("GENERAL");
        menuList.add("HEALTH");
        menuList.add("HUMANITIES");
        menuList.add("LAW");
        menuList.add("MATHEMATICS");
        menuList.add("MULTIMEDIA");
        menuList.add("PHYSICS");
        menuList.add("STUDENT LIFE");
        menuList.add("THEOLOGY");

        imagesList.add(R.drawable.accounting);        //add background images
        imagesList.add(R.drawable.agriculture);
        imagesList.add(R.drawable.arts);
        imagesList.add(R.drawable.biology);
        imagesList.add(R.drawable.business);
        imagesList.add(R.drawable.chemistry);
        imagesList.add(R.drawable.ict);
        imagesList.add(R.drawable.economics);
        imagesList.add(R.drawable.education);
        imagesList.add(R.drawable.engineering);
        imagesList.add(R.drawable.geography);
        imagesList.add(R.drawable.general);
        imagesList.add(R.drawable.health2);
        imagesList.add(R.drawable.humanities);
        imagesList.add(R.drawable.law);
        imagesList.add(R.drawable.maths);
        imagesList.add(R.drawable.multimedia);
        imagesList.add(R.drawable.physics);
        imagesList.add(R.drawable.student_life);
        imagesList.add(R.drawable.religious);

        fragmentsList.add(SampleFragment.newInstance("Accounting"));      //add fragment instances
        fragmentsList.add(SampleFragment.newInstance("Agriculture"));
        fragmentsList.add(SampleFragment.newInstance("Arts"));
        fragmentsList.add(SampleFragment.newInstance("Biology"));
        fragmentsList.add(SampleFragment.newInstance("Business"));
        fragmentsList.add(SampleFragment.newInstance("Chemistry"));
        fragmentsList.add(SampleFragment.newInstance("Computer Science"));
        fragmentsList.add(SampleFragment.newInstance("Economics"));
        fragmentsList.add(SampleFragment.newInstance("Education"));
        fragmentsList.add(SampleFragment.newInstance("Engineering"));
        fragmentsList.add(SampleFragment.newInstance("Geography"));
        fragmentsList.add(SampleFragment.newInstance("General"));
        fragmentsList.add(SampleFragment.newInstance("Health"));
        fragmentsList.add(SampleFragment.newInstance("Humanities"));
        fragmentsList.add(SampleFragment.newInstance("Law"));
        fragmentsList.add(SampleFragment.newInstance("Mathematics"));
        fragmentsList.add(SampleFragment.newInstance("Multimedia"));
        fragmentsList.add(SampleFragment.newInstance("Physics"));
        fragmentsList.add(SampleFragment.newInstance("Student Life"));
        fragmentsList.add(SampleFragment.newInstance("Theology"));
    }

    private void stopSearch() {
        searchView.setText("");
        searchView.clearFocus();
        searchView.setVisibility(View.GONE);
        closeSearch.setVisibility(View.GONE);
        searchIcon.setVisibility(View.VISIBLE);
        courses.setVisibility(View.VISIBLE);
//        page.setVisibility(View.VISIBLE);
        profile.setVisibility(View.VISIBLE);
        hideKeyboard();
    }

    private void searchClusters() {
    }

    private void searchPortal() {
    }

    private void searchHome() {
        searchView.setVisibility(View.VISIBLE);
        searchView.requestFocus();
        closeSearch.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.GONE);
        courses.setVisibility(View.GONE);
//        page.setVisibility(View.GONE);
        profile.setVisibility(View.GONE);
    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                finish();
            }
        }


    };

    @Override
    public void onBackPressed() {
        backButtonHandler();
    }

    private void backButtonHandler() {
        if (viewPager.getCurrentItem() != ID_HOME) {
            viewPager.setCurrentItem(ID_HOME);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void showCategories() {
        Allagi allagi = Allagi.initialize(DashboardActivity.this, menuList, imagesList, fragmentsList);
        allagi.start();         //start the menu list activity
    }

    private void hideSearch() {
        if (!(viewPager.getCurrentItem() == ID_GROUPS)) {
            searchView.setVisibility(View.GONE);
            searchIcon.setVisibility(View.GONE);
            closeSearch.setVisibility(View.GONE);
        }
        courses.setVisibility(View.GONE);
    }

    private void NotificationClick() {
//        page.setText(getResources().getString(R.string.notifications));
        hideSearch();
        bottomNavigation.setCount(ID_NOTIFICATION, "empty");
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }

    }
}