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

    private Context mContext;
    private List<UserDetails> mUsers, mAllUsers;
    private onItemClicked listener;

    public MentionAdapter(@NonNull Context context, List<UserDetails> list) {
        super(context, 0, list);
        mContext = context;
        mUsers = list;
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

    @NonNull
    @Override
    public Filter getFilter() {
        return new UserFilter();
    }

    private class UserFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<UserDetails> suggestions = new ArrayList<>();
            if (mAllUsers == null) {
                mAllUsers = new ArrayList<>();
                mAllUsers.addAll(mUsers);
            }

            if (charSequence == null || charSequence.length() < 1) {
                suggestions.addAll(mAllUsers);
            } else {
                for (UserDetails user : mAllUsers) {
                    if (user.getFullName() != null) {
                        String fullName = user.getFullName().toLowerCase().trim();
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

    public void setOnItemClickListener(onItemClicked listener) {
        this.listener = listener;
    }

    public interface onItemClicked {
        void onUserClicked(UserDetails userDetails);
    }
}