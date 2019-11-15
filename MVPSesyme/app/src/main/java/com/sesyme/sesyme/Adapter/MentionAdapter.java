package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.UserDetails;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MentionAdapter extends ArrayAdapter<UserDetails> implements Filterable {

    private List<UserDetails> mUsersFull;
    private onItemClicked listener;
    private List<UserDetails> mUsers;
    private Context mContext;

    public MentionAdapter(@NonNull Context context, List<UserDetails> list) {
        super(context, 0, list);
        this.mUsers = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        UserDetails user = getItem(position);
        final int Position = position;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        TextView userName = convertView.findViewById(R.id.suggested_user);
        CircleImageView userImage = convertView.findViewById(R.id.suggested_image);
        if (user != null) {
            String username = user.getFullName();
            userName.setText(username);
            Glide.with(mContext.getApplicationContext()).load(user.getProfileUrl()).error(R.drawable.img).into(userImage);
        }
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null && Position != -1) {
                    UserDetails clickedUser = getItem(Position);
                    listener.onUserClicked(clickedUser);
                }
            }
        });
        return convertView;
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return UserFilter;
    }

    private Filter UserFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<UserDetails> suggestions = new ArrayList<>();
            if (mUsersFull == null){
                mUsersFull = new ArrayList<>();
                mUsersFull.addAll(mUsers);
            }

            if (charSequence != null && charSequence.length() != 0){
                String searchString = charSequence.toString().toLowerCase().trim();
                for (UserDetails post: mUsersFull){
                    String text = post.getFullName().toLowerCase();
                    if (text.contains(searchString)){
                        suggestions.add(post);
                    }
                }
            }else {
                suggestions.addAll(mUsersFull);
            }

            results.values = suggestions;
            results.count = suggestions.size();


            if (results.count == 0){
                Toast.makeText(mContext, "No Results", Toast.LENGTH_SHORT).show();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if (filterResults.values != null) {
                //noinspection unchecked
                mUsers = (List<UserDetails>) filterResults.values;
                addAll(mUsers);
                if (filterResults.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }


    };

    public void setOnItemClickListener(onItemClicked listener) {
        this.listener = listener;
    }

    public interface onItemClicked {
        void onUserClicked(UserDetails userDetails);
    }
}