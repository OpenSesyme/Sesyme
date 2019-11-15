package com.sesyme.sesyme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.sesyme.sesyme.allagi.Allagi;
import com.sesyme.sesyme.Adapter.DashboardAdaptor;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.Fragments.SampleFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DashboardActivity extends AppCompatActivity {

    private final int ID_HOME = 0;
    private final int ID_EXPLORE = 1;
    private final int ID_NOTIFICATION = 2;
    private final int ID_ACCOUNT = 3;
    private MeowBottomNavigation bottomNavigation;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ViewPager viewPager;
    private ArrayList<Fragment> fragmentsList;
    private ArrayList<Integer> imagesList;
    private ArrayList<String> menuList;
    private Methods methods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        viewPager = findViewById(R.id.view_pager);
        methods = new Methods(this);

        DashboardAdaptor adaptor = new DashboardAdaptor(getSupportFragmentManager());

        viewPager.setAdapter(adaptor);
        Intent intent = getIntent();
        if (intent.hasExtra("scrollTo")) {
            int s = intent.getIntExtra("scrollTo", 0);
            switch (s) {
                case ID_NOTIFICATION:
                    viewPager.setCurrentItem(ID_NOTIFICATION);
                    break;
                case ID_ACCOUNT:
                    viewPager.setCurrentItem(ID_ACCOUNT);
                    break;
                default:
                    viewPager.setCurrentItem(ID_HOME);
            }
        }

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.ic_home_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_EXPLORE, R.drawable.ic_explore_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_NOTIFICATION, R.drawable.ic_notifications_black_24dp));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_person_black_24dp));

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
                                        methods.showToast("News Feed");
                                        break;
                                    case ID_EXPLORE:
                                        methods.showToast("Explore");
                                        showCategories();
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

        menuList.add("BUSINESS");       //add titles
        menuList.add("EDUCATION");
        menuList.add("CHEMISTRY");
        menuList.add("HUMANITIES");
        menuList.add("ENGINEERING");
        menuList.add("MATHEMATICS");
        menuList.add("PHYSICS");
        menuList.add("HEALTH");
        menuList.add("ARTS");
        menuList.add("LAW");
        menuList.add("COMPUTER SCIENCES");
        menuList.add("ECONOMICS");
        menuList.add("AGRICULTURE");
        menuList.add("THEOLOGY");
        menuList.add("GEOGRAPHY");

        imagesList.add(R.drawable.business);        //add background images
        imagesList.add(R.drawable.education);
        imagesList.add(R.drawable.chemistry);
        imagesList.add(R.drawable.humanities);
        imagesList.add(R.drawable.engineering);
        imagesList.add(R.drawable.maths);
        imagesList.add(R.drawable.health);
        imagesList.add(R.drawable.health2);
        imagesList.add(R.drawable.arts);
        imagesList.add(R.drawable.law);
        imagesList.add(R.drawable.ict);
        imagesList.add(R.drawable.economics);
        imagesList.add(R.drawable.agriculture);
        imagesList.add(R.drawable.religious);
        imagesList.add(R.drawable.geography);

        fragmentsList.add(SampleFragment.newInstance("Business"));      //add fragment instances
        fragmentsList.add(SampleFragment.newInstance("Education"));
        fragmentsList.add(SampleFragment.newInstance("Chemistry"));
        fragmentsList.add(SampleFragment.newInstance("Humanities"));
        fragmentsList.add(SampleFragment.newInstance("Engineering"));
        fragmentsList.add(SampleFragment.newInstance("Mathematics"));
        fragmentsList.add(SampleFragment.newInstance("Physics"));
        fragmentsList.add(SampleFragment.newInstance("Health"));
        fragmentsList.add(SampleFragment.newInstance("Arts"));
        fragmentsList.add(SampleFragment.newInstance("Law"));
        fragmentsList.add(SampleFragment.newInstance("Computer Science"));
        fragmentsList.add(SampleFragment.newInstance("Economics"));
        fragmentsList.add(SampleFragment.newInstance("Agriculture"));
        fragmentsList.add(SampleFragment.newInstance("Theology"));
        fragmentsList.add(SampleFragment.newInstance("Geography"));

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
}