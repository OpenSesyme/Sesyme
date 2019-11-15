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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestHolder>{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context mContext;
    private List<String> usersList;
    private OnClickListener listener;

    public RequestsAdapter(Context context, List<String> list){
        this.mContext = context;
        this.usersList = list;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.request_layout, parent, false);
        return new RequestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestHolder holder, final int position) {
        String user = usersList.get(position);
        db.collection(SefnetContract.USER_DETAILS).document(user).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null){
                    String name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                    String url = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                    holder.username.setText(name);
                    Glide.with(mContext.getApplicationContext()).load(url)
                            .error(R.drawable.img).centerCrop().into(holder.image);
                }
            }
        });

        holder.later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class RequestHolder extends RecyclerView.ViewHolder{

        TextView later, accept, reject, username;
        ImageView image;

        @SuppressWarnings("WeakerAccess")
        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            later = itemView.findViewById(R.id.later_request);
            accept = itemView.findViewById(R.id.accept_request);
            reject = itemView.findViewById(R.id.reject_request);
            username = itemView.findViewById(R.id.username_request);
            image = itemView.findViewById(R.id.profile_pic_request);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onAcceptClicked(position, accept);
                    }
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRejectClicked(position, reject);
                    }
                }
            });
        }
    }

    public void setOnClickListener(OnClickListener listener){
        this.listener = listener;
    }

    public interface OnClickListener{
        void onAcceptClicked(int position, TextView view);
        void onRejectClicked(int position, TextView view);
    }
}
