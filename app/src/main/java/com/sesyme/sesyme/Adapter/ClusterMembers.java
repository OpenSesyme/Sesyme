package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.UserDetails;
import java.util.List;

public class ClusterMembers extends RecyclerView.Adapter<ClusterMembers.MembersHolder> {

    private List<UserDetails> members;
    private Context mContext;
    private OnItemClickListener listener;

    public ClusterMembers(Context context, List<UserDetails> list){
        this.mContext = context;
        this.members = list;
    }

    @NonNull
    @Override
    public MembersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_member, parent, false);
        return new MembersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersHolder holder, final int position) {
        UserDetails user = members.get(position);
        String name = user.getFullName();
        String url = user.getProfileUrl();
        Glide.with(mContext.getApplicationContext()).load(url).error(R.drawable.img).into(holder.profilePic);
        holder.userName.setText(name);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MembersHolder extends RecyclerView.ViewHolder{

        ImageView profilePic, remove;
        TextView userName;

        @SuppressWarnings("WeakerAccess")
        public MembersHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic_group_member);
            userName = itemView.findViewById(R.id.name_group_member);
            remove = itemView.findViewById(R.id.remove_group_member);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRemoveClicked(position);
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onRemoveClicked(int position);
    }
}
