package com.sesyme.sesyme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sesyme.sesyme.Adapter.NotificationsAdapter;
import com.sesyme.sesyme.AnswersActivity;
import com.sesyme.sesyme.ClusterChat;
import com.sesyme.sesyme.OthersProfile;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    private NotificationsAdapter notificationsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference NotificationRef;
    private TextView emptyText;
    private RecyclerView notificationRecycler;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View notificationView = inflater.inflate(R.layout.notifications_recycler_view, container, false);

        notificationRecycler = notificationView.findViewById(R.id.recycler_view_notifications);
        NotificationRef = db.collection(SefnetContract.NOTIFICATIONS);
        emptyText = notificationView.findViewById(R.id.empty_notifications);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            Query query = NotificationRef.orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("receiver", email).limit(50);

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e == null && queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() < 1) {
                            emptyText.setVisibility(View.VISIBLE);
                            notificationRecycler.setVisibility(View.GONE);
                        }
                    }
                }
            });

            FirestoreRecyclerOptions<NotificationClass> options = new FirestoreRecyclerOptions
                    .Builder<NotificationClass>().setQuery(query, NotificationClass.class).build();

            notificationsAdapter = new NotificationsAdapter(getActivity(), options);

            notificationRecycler.setHasFixedSize(true);
            notificationRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            notificationRecycler.setAdapter(notificationsAdapter);

            notificationsAdapter.setOnItemClickListener(new NotificationsAdapter.OnItemClickListener() {
                @Override
                public void onPictureClicked(DocumentSnapshot snapshot, int position) {
                    String profileRef = snapshot.getString("sender");
                    Intent intent = new Intent(getActivity(), OthersProfile.class);
                    intent.putExtra(SefnetContract.PROFILE_REF, profileRef);
                    startActivity(intent);
                    String id = snapshot.getId();
                    NotificationRef.document(id).update("seen", 1);
                }

                @Override
                public void onUsernameClicked(DocumentSnapshot snapshot, int position) {
                    String elementRef = snapshot.getString("elementRef");
                    String type = snapshot.getString("type");
                    if (elementRef != null) {
                        if (elementRef.contains("/")) {
                            String[] path = elementRef.split("/");
                            if (type != null && type.equals("Chat")) {
                                if (path.length > 1) {
                                    String ref = path[0];
                                    Log.d("MyRef", "onUsernameClicked: " + ref);
                                    Intent intent = new Intent(getActivity(), ClusterChat.class);
                                    intent.putExtra(SefnetContract.REFERENCE, ref);
                                    startActivity(intent);
                                }
                            } else {
                                if (path.length > 1) {
                                    String ref = path[1];
                                    Intent intent = new Intent(getActivity(), AnswersActivity.class);
                                    intent.putExtra(SefnetContract.REFERENCE, ref);
                                    startActivity(intent);
                                }

                            }
                        }else {
                            Intent intent = new Intent(getActivity(), AnswersActivity.class);
                            intent.putExtra(SefnetContract.REFERENCE, elementRef);
                            startActivity(intent);
                        }
                    }
                    String id = snapshot.getId();
                    NotificationRef.document(id).update("seen", 1);
                }
            });
            notificationsAdapter.startListening();
        }
        return notificationView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notificationsAdapter.stopListening();
    }
}

