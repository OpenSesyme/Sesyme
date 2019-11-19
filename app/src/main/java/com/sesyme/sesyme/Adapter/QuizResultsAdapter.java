package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.ResultsClass;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.List;

public class QuizResultsAdapter extends RecyclerView.Adapter<QuizResultsAdapter.ResultHolder> {

    private List<ResultsClass> results;
    private Context mContext;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userName, userImageUrl;

    public QuizResultsAdapter(Context context, List<ResultsClass> list) {
        this.mContext = context;
        this.results = list;
    }

    @NonNull
    @Override
    public ResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_view, parent, false);
        return new ResultHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ResultHolder holder, int position) {
        holder.user.setGravity(Gravity.CENTER_VERTICAL);
        ResultsClass result = results.get(position);
        holder.options.setVisibility(View.GONE);
        db.collection(SefnetContract.USER_DETAILS).document(result.getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            userImageUrl = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                            userName = documentSnapshot.getString(SefnetContract.FULL_NAME);
                            Glide.with(mContext.getApplicationContext()).load(userImageUrl)
                                    .error(R.drawable.img).centerCrop().into(holder.nProfile);
                            holder.user.setText(userName);
                        }
                    }
                });
        holder.nTime.setText(result.getMarks());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ResultHolder extends RecyclerView.ViewHolder {

        ImageView nProfile;
        TextView user;
        TextView nTime;
        ImageButton options;

        @SuppressWarnings("WeakerAccess")
        public ResultHolder(@NonNull View itemView) {
            super(itemView);
            nProfile = itemView.findViewById(R.id.profile_image_notification);
            user = itemView.findViewById(R.id.name_notification);
            nTime = itemView.findViewById(R.id.time_notification);
            options = itemView.findViewById(R.id.options_notification);
        }
    }
}
