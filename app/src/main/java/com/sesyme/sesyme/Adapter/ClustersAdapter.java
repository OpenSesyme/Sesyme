package com.sesyme.sesyme.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.ClusterChat;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.ClusterDetails;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.List;

public class ClustersAdapter extends FirestoreRecyclerAdapter<ClusterDetails, ClustersAdapter.ClusterHolder> {

    private Context mContext;
    private Methods methods;
    private String email;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options of firestore recycler adapter
     */
    public ClustersAdapter(Context context, @NonNull FirestoreRecyclerOptions<ClusterDetails> options) {
        super(options);
        this.mContext = context;
        methods = new Methods(context);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final ClusterHolder holder, int i, @NonNull final ClusterDetails model) {
        if (model.getPrivacy().equals("Private")) {
            Glide.with(mContext.getApplicationContext()).load(R.drawable.cluster_private).into(holder.privacy);
        } else {
            Glide.with(mContext.getApplicationContext()).load(R.drawable.cluster_privacy).into(holder.privacy);
        }

        Glide.with(mContext.getApplicationContext()).load(model.getClusterIcon())
                .thumbnail(0.25f).error(R.drawable.img).into(holder.ClusterIcon);

        holder.clusterName.setText(model.getClusterName());
        String time = String.valueOf(model.getCreatedAt());
        String showTime = "Created " + methods.covertTimeToText(time) + " ago";
        holder.createdAt.setText(showTime);
        int members = model.getMembers().size();
        String clusterMembers;
        if (model.getRequests() != null && model.getAdmins().contains(email)) {
            int requests = model.getRequests().size();
            if (requests == 1) {
                clusterMembers = requests + " Request";
            } else {
                clusterMembers = requests + " Requests";
            }
        } else {
            if (members == 1) {
                clusterMembers = members + " member";
            } else {
                clusterMembers = members + " members";
            }
        }
        holder.membersNumber.setText(clusterMembers);
        if (model.getDescription() != null && !model.getDescription().isEmpty()) {
            holder.description.setText(model.getDescription());
        } else {
            String description = "This cluster does not have a description";
            holder.description.setText(description);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                Intent intent = new Intent(mContext, ClusterChat.class);
                intent.putExtra(SefnetContract.REFERENCE, id);
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String id = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                showDialog(id, model);
                return false;
            }
        });
    }

    @NonNull
    @Override
    public ClusterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item_single_layout, parent, false);
        return new ClusterHolder(view);
    }

    class ClusterHolder extends RecyclerView.ViewHolder {

        ImageView ClusterIcon, privacy;
        TextView clusterName, description, createdAt, membersNumber;

        @SuppressWarnings("WeakerAccess")
        public ClusterHolder(@NonNull View itemView) {
            super(itemView);
            ClusterIcon = itemView.findViewById(R.id.group_image_single_item);
            privacy = itemView.findViewById(R.id.group_privacy_single_item);
            clusterName = itemView.findViewById(R.id.group_name_single_item);
            description = itemView.findViewById(R.id.group_description_single_item);
            createdAt = itemView.findViewById(R.id.group_time_single_item);
            membersNumber = itemView.findViewById(R.id.members_number_single_item);
        }
    }

    private void showDialog(final String id, final ClusterDetails model) {
        final List<String> admins = model.getAdmins();
        final List<String> membersL = model.getMembers();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (admins.contains(email)) {
            builder.setMessage("Are you sure you want to delete this cluster?");
        } else {
            builder.setMessage("Are you sure you want to leave this cluster?");
        }
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (admins.contains(email)) {
                    db.collection(SefnetContract.CLUSTERS_DETAILS).document(id).delete();
                    db.collection(id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots != null) {
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    snapshot.getReference().delete();
                                }
                            }
                        }
                    });
                } else {
                    for (i = 0; i < membersL.size(); i++) {
                        if (email.equals(membersL.get(i))) {
                            membersL.remove(i);
                            db.collection(SefnetContract.CLUSTERS_DETAILS).document(id)
                                    .update("members", membersL);
                            notifyDataSetChanged();
                            methods.showToast("You have been removed from this cluster");
                        }
                    }
                }

            }
        });
        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder.create();
        alert11.show();
    }
}