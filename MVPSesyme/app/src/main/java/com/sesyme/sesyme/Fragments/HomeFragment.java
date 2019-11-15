package com.sesyme.sesyme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sesyme.sesyme.Adapter.QuestionsAdapter;
import com.sesyme.sesyme.Adapter.SearchAdapter;
import com.sesyme.sesyme.AnswersActivity;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.WritePostClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.OthersProfile;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.WritePostActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QuestionsRef;
    private QuestionsAdapter myAdapter;
    private RecyclerView recyclerView;
    private String email;
    private int currentVisPosition = 0;
    private SwipeRefreshLayout refreshLayout;
    private TextView emptyText;
    private ProgressBar loading;
    private Methods methods;
    private SearchView search;
    private List<WritePostClass> posts;
    private SearchAdapter searchAdapter;
    private ListView listView;
//    private LinearLayout layout;

    public HomeFragment() {
        //Required empty Constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        posts = new ArrayList<>();

        recyclerView = rootView.findViewById(R.id.reviews_recycler_view1);
        QuestionsRef = db.collection(SefnetContract.QUESTIONS);
        emptyText = rootView.findViewById(R.id.empty_home);
        loading = rootView.findViewById(R.id.home_progress_loading);
        refreshLayout = rootView.findViewById(R.id.swipe_refresh_home);
        search = rootView.findViewById(R.id.et_search_home);
        listView = rootView.findViewById(R.id.questions_list);
//        layout = getActivity().findViewById(R.id.action_layout);
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 0) {
//                    slideDown(layout);
//                } else {
//                    slideUp(layout);
//                }
//
//            }
//        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                search.clearFocus();
            }
        }, 200);

        loading.setVisibility(View.VISIBLE);
        methods = new Methods(getActivity());

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpRecyclerView();
                refreshLayout.setRefreshing(false);
            }
        });

        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        QuestionsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots){
                    WritePostClass post = snapshot.toObject(WritePostClass.class);
                    post.setId(snapshot.getId());
                    posts.add(post);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        searchAdapter = new SearchAdapter(getActivity(), posts);
        listView.setAdapter(searchAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (listView.getChildAt(0) != null) {
                    refreshLayout.setEnabled(listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() == 0);
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                String searchText = methods.sentenceCaseForText(s);
                if (searchText.length() > 0) {
                    recyclerView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    searchAdapter.getFilter().filter(s);
                }else {
                    listView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        FloatingActionButton fab = rootView.findViewById(R.id.question_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WritePostActivity.class);
                intent.putExtra("type", "Question");
                startActivity(intent);
            }
        });

        setUpRecyclerView();
        return rootView;
    }

    private void setUpRecyclerView() {
        if (email != null) {
            QuestionsRef = db.collection(SefnetContract.QUESTIONS);
            Query query = QuestionsRef.orderBy("dateTime", Query.Direction.DESCENDING).limit(50);

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e == null && queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() < 1) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                }
            });

            recyclerOptions(query);
        }
    }

    private void recyclerOptions(Query query){
        FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                .setQuery(query, WritePostClass.class).build();
        myAdapter = new QuestionsAdapter(getActivity(), options);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);
        loading.setVisibility(View.GONE);

        myAdapter.setOnItemClickListener(new QuestionsAdapter.OnItemClickListener() {
            @Override
            public void onCommentClicked(DocumentSnapshot snapshot, int position) {
                String reference = snapshot.getId();
                Intent intent = new Intent(getActivity(), WritePostActivity.class);
                intent.putExtra(SefnetContract.REPLY_REF, reference);
                intent.putExtra("type", "Reply");
                startActivity(intent);
            }

            @Override
            public void onProfileClicked(DocumentSnapshot snapshot, int position) {
                Intent intent = new Intent(getActivity(), OthersProfile.class);
                intent.putExtra(SefnetContract.PROFILE_REF, snapshot.getString("author"));
                startActivity(intent);
            }

            @Override
            public void onTitleClicked(DocumentSnapshot snapshot, int position) {
                String id = snapshot.getId();
                Intent intent = new Intent(getActivity(), AnswersActivity.class);
                intent.putExtra(SefnetContract.REPLY_REF, id);
                startActivity(intent);
            }
        });
        myAdapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recyclerView.getLayoutManager() != null)
            currentVisPosition = ((LinearLayoutManager) recyclerView
                    .getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                search.clearFocus();
            }
        }, 200);
        if (recyclerView.getLayoutManager() != null)
            recyclerView.getLayoutManager().scrollToPosition(currentVisPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myAdapter != null) {
            myAdapter.stopListening();
        }
    }

    public void slideUp(LinearLayout view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(LinearLayout view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }
}