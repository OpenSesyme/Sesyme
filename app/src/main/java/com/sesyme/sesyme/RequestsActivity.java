package com.sesyme.sesyme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesyme.sesyme.Adapter.RequestsAdapter;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RequestsAdapter adapter;
    private RecyclerView recyclerView;
    private String clusterId;
    private ArrayList<String> requests, members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        recyclerView = findViewById(R.id.request_recycler);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.REFERENCE)){
            clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
            db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null){
                                requests = (ArrayList<String>) documentSnapshot.get("requests");
                                members = (ArrayList<String>) documentSnapshot.get("members");
                                adapter = new RequestsAdapter(RequestsActivity.this, requests);
                                recyclerView.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
                                recyclerView.setAdapter(adapter);
                                adapter.setOnClickListener(new RequestsAdapter.OnClickListener() {
                                    @Override
                                    public void onAcceptClicked(int position, TextView view) {
                                        String newUser = requests.get(position);
                                        if (!LocateString(members, newUser)){
                                            members.add(newUser);
                                            requests.remove(position);
                                            db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                                                    .update("members", members);
                                            db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                                                    .update("requests", requests);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onRejectClicked(int position, TextView view) {
                                        requests.remove(position);
                                        db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
                                                .update("requests", requests);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private boolean LocateString(List<String> Array, String s) {
        boolean found = false;
        for (int i = (Array.size() - 1); i > -1; i--) {
            String element = Array.get(i);
            if (element.equals(s)) {
                found = true;
            }
        }
        return found;
    }
}
