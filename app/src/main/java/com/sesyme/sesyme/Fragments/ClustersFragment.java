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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.Adapter.ClustersAdapter;
import com.sesyme.sesyme.Adapter.SearchClusterAdapter;
import com.sesyme.sesyme.ClusterChat;
import com.sesyme.sesyme.CreateCluster;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.ClusterDetails;
import com.sesyme.sesyme.data.SefnetContract;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClustersFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SwipeRefreshLayout refreshLayout;
    private LinearLayout defaultClusters;
    private RecyclerView clustersRecycler;
    private ClustersAdapter adapter;
    private EditText search;
    private SearchClusterAdapter searchAdapter;
    private GridView listView;
    private List<ClusterDetails> clusters;
    private ImageView closeSearch, searchIcon, logo;
    private String email;

    public ClustersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View groupsView = inflater.inflate(R.layout.groups_fragment, container, false);
        clustersRecycler = groupsView.findViewById(R.id.groups_recycler);
        FloatingActionButton add = groupsView.findViewById(R.id.add_group);
        CardView help = groupsView.findViewById(R.id.help_cluster);
        CardView general = groupsView.findViewById(R.id.general_cluster);
        //noinspection ConstantConditions
        search = getActivity().findViewById(R.id.et_search_home);
        searchIcon = getActivity().findViewById(R.id.search_icon_home);
        closeSearch = getActivity().findViewById(R.id.close_icon_home);
        logo = getActivity().findViewById(R.id.logo_dashboard);
        defaultClusters = groupsView.findViewById(R.id.default_clusters_layout);
        refreshLayout = groupsView.findViewById(R.id.swipe_refresh_clusters);
        listView = groupsView.findViewById(R.id.groups_list_view);
        clusters = new ArrayList<>();

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ClusterChat.class);
                intent.putExtra(SefnetContract.REFERENCE, "Help Cluster");
                startActivity(intent);
            }
        });

        general.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ClusterChat.class);
                intent.putExtra(SefnetContract.REFERENCE, "General Cluster");
                startActivity(intent);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateCluster.class);
                startActivity(intent);
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpRecyclerView();
                refreshLayout.setRefreshing(false);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }

        search.setVisibility(View.GONE);
        searchIcon.setVisibility(View.VISIBLE);
        closeSearch.setVisibility(View.GONE);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setVisibility(View.VISIBLE);
                search.requestFocus();
                closeSearch.setVisibility(View.VISIBLE);
                searchIcon.setVisibility(View.GONE);
                logo.setVisibility(View.GONE);

            }
        });

        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setText("");
                search.clearFocus();
                search.setVisibility(View.GONE);
                closeSearch.setVisibility(View.GONE);
                searchIcon.setVisibility(View.VISIBLE);
                logo.setVisibility(View.VISIBLE);
                hideKeyboard();
            }
        });

        db.collection(SefnetContract.CLUSTERS_DETAILS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null){
                    for (DocumentSnapshot snapshot: queryDocumentSnapshots){
                        ClusterDetails cluster = snapshot.toObject(ClusterDetails.class);
                        if (cluster != null) {
                            cluster.setId(snapshot.getId());
                        }
                        clusters.add(cluster);
                    }
                    if (getActivity() != null) {
                        searchAdapter = new SearchClusterAdapter(getActivity(), clusters);
                        listView.setAdapter(searchAdapter);
                    }
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
                if (editable.toString().length() > 0){
                    listView.setVisibility(View.VISIBLE);
                    clustersRecycler.setVisibility(View.GONE);
                    defaultClusters.setVisibility(View.GONE);
                    searchAdapter.getFilter().filter(editable.toString().toLowerCase());
                }else {
                    listView.setVisibility(View.GONE);
                    defaultClusters.setVisibility(View.VISIBLE);
                    clustersRecycler.setVisibility(View.VISIBLE);
                }
            }
        });
        setUpRecyclerView();
        return groupsView;
    }

    private void setUpRecyclerView() {
        if (email != null) {
            CollectionReference clustersRef = db.collection(SefnetContract.CLUSTERS_DETAILS);
            Query query = clustersRef.orderBy(SefnetContract.CLUSTER_NAME, Query.Direction.ASCENDING)
                    .whereArrayContains("members", email);
            FirestoreRecyclerOptions<ClusterDetails> options = new FirestoreRecyclerOptions.Builder<ClusterDetails>()
                    .setQuery(query, ClusterDetails.class).build();

            adapter = new ClustersAdapter(getActivity(), options);
            clustersRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            clustersRecycler.setAdapter(adapter);
        } else {
            Log.d("SetUpRecycler", "setUpRecyclerView: email null");
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        adapter.startListening();
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
        adapter.stopListening();
    }
}
