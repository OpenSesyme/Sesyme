package com.sesyme.sesyme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.Adapter.ArticlesAdapter;
import com.sesyme.sesyme.Adapter.TagsAdapter;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.WriteArticle;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoPortalFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference QuestionsRef;
    private ArticlesAdapter myAdapter;
    private TextView emptyText;
    private RecyclerView recyclerView;
    private CardView uploadDoc, writeArticle;
    private RecyclerView hashTags;
    private TagsAdapter adapter;

    public InfoPortalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View portalView = inflater.inflate(R.layout.info_portal_fragment, container, false);
        recyclerView = portalView.findViewById(R.id.info_portal_recycler);
        emptyText = portalView.findViewById(R.id.info_portal_empty);
        uploadDoc = portalView.findViewById(R.id.info_portal_docs);
        writeArticle = portalView.findViewById(R.id.info_portal_article);
        hashTags = portalView.findViewById(R.id.tags_recycler_info);
        FloatingActionButton write = portalView.findViewById(R.id.info_portal_fab);
        RelativeLayout rootLayout = portalView.findViewById(R.id.info_portal_root_layout);

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDoc.setVisibility(View.GONE);
                writeArticle.setVisibility(View.GONE);
            }
        });

        QuestionsRef = db.collection(SefnetContract.QUESTIONS);

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploadDoc.getVisibility() == View.VISIBLE) {
                    uploadDoc.setVisibility(View.GONE);
                    writeArticle.setVisibility(View.GONE);
                }else {
                    uploadDoc.setVisibility(View.VISIBLE);
                    writeArticle.setVisibility(View.VISIBLE);
                }
            }
        });

        uploadDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDoc.setVisibility(View.GONE);
                writeArticle.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), WriteArticle.class);
                intent.putExtra(SefnetContract.TYPE, "Doc");
                startActivity(intent);
            }
        });

        writeArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDoc.setVisibility(View.GONE);
                writeArticle.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), WriteArticle.class);
                intent.putExtra(SefnetContract.TYPE, "Article");
                startActivity(intent);
            }
        });
        setUpTags();
        setUpPortal(null);
        return portalView;
    }

    private void setUpPortal(String type){
        Query query;
        if (type != null && !type.equals("All")) {
            query = QuestionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                    .whereEqualTo(SefnetContract.TYPE, "Article")
                    .whereArrayContains("category", type);
        }else {
            query = QuestionsRef.orderBy(SefnetContract.DATE_TIME, Query.Direction.DESCENDING)
                    .whereEqualTo(SefnetContract.TYPE, "Article");
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null && queryDocumentSnapshots != null) {
                    if (queryDocumentSnapshots.size() < 1) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        FirestoreRecyclerOptions<WritePostClass> options = new FirestoreRecyclerOptions.Builder<WritePostClass>()
                .setQuery(query, WritePostClass.class).build();
        myAdapter = new ArticlesAdapter(getActivity(), options);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

        myAdapter.startListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myAdapter != null) {
            myAdapter.stopListening();
        }
    }

    private void setUpTags() {
        ArrayList<String> tagsList = new ArrayList<>();
        tagsList.add(0, "All");
        tagsList.add(1, "Accounting");
        tagsList.add(2, "Arts");
        tagsList.add(3, "Biology");
        tagsList.add(4, "Business");
        tagsList.add(5, "Chemistry");
        tagsList.add(6, "Computer Science");
        tagsList.add(7, "Economics");
        tagsList.add(8, "Education");
        tagsList.add(9, "Engineering");
        tagsList.add(10, "Health");
        tagsList.add(11, "Humanities");
        tagsList.add(12, "Law");
        tagsList.add(13, "Mathematics");
        tagsList.add(14, "Multimedia");
        tagsList.add(15, "Physics");
        tagsList.add(16, "Agriculture");
        tagsList.add(17, "Theology");
        tagsList.add(18, "Geography");
        tagsList.add(19, "Student Life");
        tagsList.add(20, "General");

        adapter = new TagsAdapter(getActivity(), tagsList);
        hashTags.setHasFixedSize(true);
        hashTags.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        hashTags.setAdapter(adapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RelativeLayout view = (RelativeLayout) hashTags.getChildAt(0);
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
                        RelativeLayout view = (RelativeLayout) hashTags.getChildAt(i);
                        if (view != null) {
                            TextView textView = view.findViewById(R.id.name_hash_tag);
                            textView.setTextColor(getResources().getColor(R.color.iconsColor));
                            textView.setBackground(getResources()
                                    .getDrawable(R.drawable.button_follow));
                        }
                    }
                }
                setUpPortal(tagView.getText().toString());
                tagView.setTextColor(getResources().getColor(R.color.white));
                tagView.setBackground(getResources().getDrawable(R.drawable.bt_ui));
            }
        });
    }
}
