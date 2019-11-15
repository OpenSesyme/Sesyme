package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sesyme.sesyme.AnswerPoll;
import com.sesyme.sesyme.AnswerQuiz;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.DoubleClass;
import com.sesyme.sesyme.data.SefnetContract;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DoubleAdapter extends RecyclerView.Adapter<DoubleAdapter.ItemHolder>{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<DoubleClass> joinedList;
    private Context mContext;
    private Date today;


    public DoubleAdapter(Context context, List<DoubleClass> list){
        this.mContext = context;
        this.joinedList = list;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.poll_card, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder holder, int position) {
        final DoubleClass item = joinedList.get(position);
        holder.title.setText(item.getTitle());
        db.collection(SefnetContract.USER_DETAILS).document(item.getCreator())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e == null && documentSnapshot != null){
                            String url = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                            Glide.with(mContext.getApplicationContext())
                                    .load(url).error(R.drawable.img).into(holder.image);
                        }
                    }
                });

        today = Calendar.getInstance().getTime();
        if (item.getType().equals("Poll")){
            Glide.with(mContext.getApplicationContext()).load(R.drawable.polls).into(holder.symbol);
            if (item.getDuration().after(today)){
                db.document(item.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            String clusterId = documentSnapshot.getString(SefnetContract.REFERENCE);
                            if (clusterId != null) {
//                                db.collection(SefnetContract.CLUSTERS_DETAILS).document(clusterId)
//                                        .collection(SefnetContract.POLLS_DETAILS).document()
//                                        .update("option1", "")
                            }
                        }
                    }
                });
                holder.itemView.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, AnswerPoll.class);
                    intent.putExtra(SefnetContract.POLL_ID, item.getId());
                    intent.putExtra(SefnetContract.CREATOR, item.getCreator());
                    mContext.startActivity(intent);
                }
            });
        }else {
            Glide.with(mContext.getApplicationContext()).load(R.drawable.quiz).into(holder.symbol);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, AnswerQuiz.class);
                    intent.putExtra(SefnetContract.QUIZ_ID, item.getId());
                    intent.putExtra(SefnetContract.CREATOR, item.getCreator());
                    mContext.startActivity(intent);
                }
            });

            if (item.getDuration() != null && item.getDuration().after(today)){
                holder.itemView.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return joinedList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder{

        TextView title;
        ImageView image, symbol;

        @SuppressWarnings("WeakerAccess")
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.poll_card_title);
            image = itemView.findViewById(R.id.poll_card_creator_image);
            symbol = itemView.findViewById(R.id.poll_card_symbol);
        }
    }
}
