package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.UserDetails;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MyMemberHolder> {

    private List<UserDetails> members;
    private Context mContext;

    public MembersAdapter(Context context, List<UserDetails> list){
        this.mContext = context;
        this.members = list;
    }
    @NonNull
    @Override
    public MyMemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.description_member, parent, false);
        return new MyMemberHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyMemberHolder holder, int position) {
        UserDetails user = members.get(position);
        if (user.getFullName() != null) {
            String name = user.getFullName();
            holder.userName.setText(name);
        }
            String url = user.getProfileUrl();
            Glide.with(mContext.getApplicationContext()).load(url).error(R.drawable.img).into(holder.profilePic);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MyMemberHolder extends RecyclerView.ViewHolder {

        ImageView profilePic;
        TextView userName;

        @SuppressWarnings("WeakerAccess")
        public MyMemberHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic_desc_member);
            userName = itemView.findViewById(R.id.name_desc_member);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(mContext, "Pressed", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }
}
