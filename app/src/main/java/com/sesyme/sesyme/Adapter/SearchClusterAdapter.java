package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.sesyme.sesyme.ClusterChat;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.ClusterDetails;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import java.util.ArrayList;
import java.util.List;

public class SearchClusterAdapter extends ArrayAdapter<ClusterDetails> {

    private List<ClusterDetails> allClusters;
    private List<ClusterDetails> filteredClusters;
    private Context mContext;
    private Methods methods;

    public SearchClusterAdapter(@NonNull Context context, List<ClusterDetails> list) {
        super(context, 0, list);
        this.filteredClusters = list;
        this.mContext = context;
        this.methods = new Methods(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View itemView, @NonNull ViewGroup parent) {
        final ClusterDetails model = filteredClusters.get(position);
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.group_item_single_layout, parent, false);
        }
        ImageView clusterIcon = itemView.findViewById(R.id.group_image_single_item);
        ImageView privacy = itemView.findViewById(R.id.group_privacy_single_item);
        TextView clusterName = itemView.findViewById(R.id.group_name_single_item);
        TextView description = itemView.findViewById(R.id.group_description_single_item);
        TextView createdAt = itemView.findViewById(R.id.group_time_single_item);
        TextView membersNumber = itemView.findViewById(R.id.members_number_single_item);
        int limit = 200;
        if (model.getPrivacy().equals("Private")) {
            Glide.with(mContext.getApplicationContext()).load(R.drawable.cluster_private).into(privacy);
        } else {
            Glide.with(mContext.getApplicationContext()).load(R.drawable.cluster_privacy).into(privacy);
            limit = 500;
        }

        Glide.with(mContext.getApplicationContext()).load(model.getClusterIcon()).error(R.drawable.img).into(clusterIcon);

        clusterName.setText(model.getClusterName());
        String time = String.valueOf(model.getCreatedAt());
        String showTime = "Created " + methods.covertTimeToText(time) + " ago";
        createdAt.setText(showTime);
        if (model.getMembers() != null) {
            int members = model.getMembers().size();
            String clusterMembers = members + "/" + limit;
            membersNumber.setText(clusterMembers);
        }
        if (model.getDescription() != null && !model.getDescription().isEmpty()) {
            description.setText(model.getDescription());
        } else {
            String descriptionString = "This cluster does not have a description";
            description.setText(descriptionString);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = model.getId();
                Intent intent = new Intent(mContext, ClusterChat.class);
                intent.putExtra(SefnetContract.REFERENCE, id);
                mContext.startActivity(intent);
            }
        });
        return itemView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new ClusterFilter();
    }

    private class ClusterFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<ClusterDetails> suggestions = new ArrayList<>();
            if (allClusters == null) {
                allClusters = new ArrayList<>();
                allClusters.addAll(filteredClusters);
            }

            if (charSequence == null || charSequence.length() < 1) {
                suggestions.addAll(allClusters);
            } else {
                for (ClusterDetails user : allClusters) {
                    if (user.getClusterName() != null) {
                        String fullName = user.getClusterName().toLowerCase().trim();
                        if (fullName.contains(charSequence.toString().toLowerCase().trim())) {
                            suggestions.add(user);
                        }
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            if (results.count == 0) {
                Toast.makeText(mContext, "No Results", Toast.LENGTH_SHORT).show();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            //noinspection unchecked
            addAll((List) filterResults.values);
            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
