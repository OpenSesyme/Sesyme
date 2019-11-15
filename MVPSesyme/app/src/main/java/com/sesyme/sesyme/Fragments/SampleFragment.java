package com.sesyme.sesyme.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sesyme.sesyme.AnswersActivity;
import com.sesyme.sesyme.Adapter.QuestionsAdapter;
import com.sesyme.sesyme.data.WritePostClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.OthersProfile;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.WritePostActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SampleFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private int currentVisPosition = 0;
    private TextView emptyText;
    private String FragmentTitle = null;
    private QuestionsAdapter myAdapter;
    private ProgressBar progressLoading;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button ask;

    public SampleFragment() {
        super();
    }

    public static SampleFragment newInstance(String title) {

        Bundle args = new Bundle();
        args.putString("title", title);
        SampleFragment fragment = new SampleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sample_categories, container, false);
        if (getArguments() != null) {
            FragmentTitle = getArguments().getString("title");
        }
        TextView titleView = view.findViewById(R.id.fragmentTitle);
        emptyText = view.findViewById(R.id.empty_fragment);
        progressLoading = view.findViewById(R.id.sample_progress_loading);
        ask = view.findViewById(R.id.ask_fragment);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_sample);
        titleView.setText(FragmentTitle);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        progressLoading.setVisibility(View.VISIBLE);

        ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WritePostActivity.class);
                ArrayList<String> list = new ArrayList<>();
                if (FragmentTitle != null) {
                    list.add(FragmentTitle);
                    intent.putExtra("tag", list);
                    intent.putExtra("type", "Question");
                    startActivity(intent);
                }
            }
        });

        recyclerView = view.findViewById(R.id.recycler_view_category);

        setUpRecyclerView();
        return view;
    }

    private void setUpRecyclerView() {

        CollectionReference questionsRef = db.collection(SefnetContract.QUESTIONS);
        Query query = questionsRef.orderBy("dateTime", Query.Direction.DESCENDING).limit(50);
        if (FragmentTitle != null) {
            query = questionsRef.orderBy("dateTime").whereArrayContains("category", FragmentTitle);
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null) {
                    if (queryDocumentSnapshots.size() < 1) {
                        emptyText.setVisibility(View.VISIBLE);
                        String s = FragmentTitle.toUpperCase();
                        char first = s.charAt(0);
                        String message;
                        if (first == 'A' || first == 'E' || first == 'I' || first == 'O' || first == 'U') {
                            message = "Be the first to ask a question.";
                        } else {
                            message = "Be the first to ask a question.";
                        }
                        ask.setVisibility(View.VISIBLE);
                        emptyText.setText(message);
                        recyclerView.setVisibility(View.GONE);
                    }
                    progressLoading.setVisibility(View.GONE);
                }
            }
        });

        FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                .setQuery(query, WritePostClass.class).build();
        myAdapter = new QuestionsAdapter(getActivity(), options);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);
        progressLoading.setVisibility(View.GONE);

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
}