package com.sesyme.sesyme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.sesyme.sesyme.Adapter.QuestionsAdapter;
import com.sesyme.sesyme.Adapter.SearchPostAdapter;
import com.sesyme.sesyme.Adapter.TagsAdapter;
import com.sesyme.sesyme.AnswersActivity;
import com.sesyme.sesyme.OthersProfile;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.WritePostActivity;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QuestionsRef;
    private QuestionsAdapter myAdapter;
    private RecyclerView recyclerView, interestsRecycler;
    private int currentVisPosition = 0;
    private SwipeRefreshLayout refreshLayout;
    private TextView emptyText;
    private ProgressBar loading;
    private Methods methods;
    private EditText search;
    private List<WritePostClass> posts;
    private SearchPostAdapter searchPostAdapter;
    private ListView listView;
    private List<String> interests;
    private TagsAdapter adapter;
    private String email;

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
        interests = new ArrayList<>();

        interests.add("All");

        recyclerView = rootView.findViewById(R.id.reviews_recycler_view1);
        interestsRecycler = rootView.findViewById(R.id.interests_recycler_home);
        QuestionsRef = db.collection(SefnetContract.QUESTIONS);
        emptyText = rootView.findViewById(R.id.empty_home);
        loading = rootView.findViewById(R.id.home_progress_loading);
        refreshLayout = rootView.findViewById(R.id.swipe_refresh_home);
        //noinspection ConstantConditions
        search = getActivity().findViewById(R.id.et_search_home);
        listView = rootView.findViewById(R.id.questions_list);

        search.setVisibility(View.GONE);

        loading.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                search.clearFocus();
                hideKeyboard();
            }
        }, 150);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }, 3000);

        methods = new Methods(getActivity());

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpRecyclerView(0);
                refreshLayout.setRefreshing(false);
            }
        });

        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        db.collection(SefnetContract.USER_DETAILS).document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressWarnings({"unchecked", "ConstantConditions"})
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.get("interests") != null) {
                                interests.addAll((ArrayList<String>) documentSnapshot.get("interests"));
                            }
                            interests.add("General");
                            interests.add("Student Life");
                            setUpRecyclerView(0);
                            setUpInterests();
                            for (int i = 0; i < interests.size(); i++){
                                String interest = interests.get(i);
                                interest = interest.replace(" ", "");
                                FirebaseMessaging.getInstance().subscribeToTopic(interest)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                String msg = "Registered";
                                                if (!task.isSuccessful()) {
                                                    msg = "Registration Failed";
                                                }
                                                Log.d("MyTopic", msg);
                                            }
                                        });
                            }
                        }
                    }
                });

        QuestionsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
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

        searchPostAdapter = new SearchPostAdapter(getActivity(), posts);
        listView.setAdapter(searchPostAdapter);

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

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = methods.sentenceCaseForText(editable.toString());
                if (searchText.length() > 0) {
                    recyclerView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    searchPostAdapter.getFilter().filter(editable.toString());
                } else {
                    listView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
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
        return rootView;
    }

    private void setUpRecyclerView(int i) {
        if (email != null) {
            QuestionsRef = db.collection(SefnetContract.QUESTIONS);
            Query query;
            if (interests.size() > 0 && i != 0) {
                String interest = interests.get(i);
                query = QuestionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                        .whereEqualTo(SefnetContract.TYPE, "Question")
                        .whereArrayContains("category", interest);
                Log.d("MyNewsFeed", "onEvent: " + interest);
            } else {
                query = QuestionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                        .whereEqualTo(SefnetContract.TYPE, "Question");
            }

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e == null && queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() < 1) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                    if (e != null) {
                        Log.d("MyNewsFeed", "onEvent: null" + e.getMessage());
                    }
                }
            });
            recyclerOptions(query);
        }
    }

    private void recyclerOptions(Query query) {
        FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                .setQuery(query, WritePostClass.class).build();
        myAdapter = new QuestionsAdapter(getActivity(), options);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

        myAdapter.setOnItemClickListener(new QuestionsAdapter.OnItemClickListener() {
            @Override
            public void onCommentClicked(DocumentSnapshot snapshot, int position) {
                String reference = snapshot.getId();
                Intent intent = new Intent(getActivity(), WritePostActivity.class);
                intent.putExtra(SefnetContract.REFERENCE, reference);
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
                intent.putExtra(SefnetContract.REFERENCE, id);
                startActivity(intent);
            }
        });
        myAdapter.startListening();
    }

    private void setUpInterests() {
        adapter = new TagsAdapter(getActivity(), interests);
        interestsRecycler.setHasFixedSize(true);
        interestsRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        interestsRecycler.setAdapter(adapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RelativeLayout view = (RelativeLayout) interestsRecycler.getChildAt(0);
                if (view != null) {
                    TextView textView = view.findViewById(R.id.name_hash_tag);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    textView.setBackground(getResources()
                            .getDrawable(R.drawable.bt_ui));
                }
            }
        }, 150);
        adapter.setOnClickListener(new TagsAdapter.OnClickListener() {
            @Override
            public void onTagSelected(int Position, TextView tagView) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (i != Position) {
                        RelativeLayout view = (RelativeLayout) interestsRecycler.getChildAt(i);
                        if (view != null) {
                            TextView textView = view.findViewById(R.id.name_hash_tag);
                            textView.setTextColor(getResources().getColor(R.color.iconsColor));
                            textView.setBackground(getResources()
                                    .getDrawable(R.drawable.button_follow));
                        }
                    }
                }
                setUpRecyclerView(Position);
                tagView.setTextColor(getResources().getColor(R.color.white));
                tagView.setBackground(getResources().getDrawable(R.drawable.bt_ui));
            }
        });
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
                hideKeyboard();
            }
        }, 200);
        if (recyclerView.getLayoutManager() != null)
            recyclerView.getLayoutManager().scrollToPosition(currentVisPosition);
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myAdapter != null) {
            myAdapter.stopListening();
        }
    }
}