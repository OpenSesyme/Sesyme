package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.AnswersActivity;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;
import com.sesyme.sesyme.OthersProfile;
import com.sesyme.sesyme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends ArrayAdapter<WritePostClass> implements Filterable {

    private List<WritePostClass> allQuestions;
    private List<WritePostClass> filterQuestions;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context mContext;
    private ImageView userImage;
    private TextView username;
    private Methods methods;

    public SearchAdapter(@NonNull Context context, List<WritePostClass> list) {
        super(context, 0, list);
        this.filterQuestions = list;
        this.mContext = context;
        this.methods = new Methods(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View itemView, @NonNull ViewGroup parent) {
        WritePostClass post = getItem(position);
        final int Position = position;

        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.questions_card_view, parent, false);
        }

        userImage = itemView.findViewById(R.id.comment_user_image);
        ImageView postImage = itemView.findViewById(R.id.ratting_image);
        username = itemView.findViewById(R.id.comment_user_name);
        TextView date = itemView.findViewById(R.id.comment_date_time);
        TextView title = itemView.findViewById(R.id.title_question_reviews);
        TextView description = itemView.findViewById(R.id.title_reviews);
        TextView comments = itemView.findViewById(R.id.comment_comments_count);
        TextView tags = itemView.findViewById(R.id.tags_post);
        TextView numLikes = itemView.findViewById(R.id.tv_like_questions_card);
        ImageView options = itemView.findViewById(R.id.options_question_card);
        ImageView accepted = itemView.findViewById(R.id.answer_accepted);
        LinearLayout accept_layout = itemView.findViewById(R.id.accept_answer_layout);
        LinearLayout bottomLayout = itemView.findViewById(R.id.bottom_layout_post_card);

        accept_layout.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        options.setVisibility(View.GONE);

        if (post != null){
            String author = post.getAuthor();
            db.collection(SefnetContract.USER_DETAILS).document(author).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Glide.with(mContext.getApplicationContext()).load(documentSnapshot.getString
                            (SefnetContract.PROFILE_URL)).error(R.drawable.img).into(userImage);
                    String userName = documentSnapshot.getString(SefnetContract.FULL_NAME);
                    username.setText(userName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
            String time = String.valueOf(post.getDateTime());
            String timeAgo = methods.covertTimeToText(time);
            date.setText(timeAgo);
            title.setText(post.getTitle());
            description.setText(post.getDescription());

            if (post.getAccepted() != null && post.getAccepted()){
                accepted.setVisibility(View.VISIBLE);
            }else {
                accepted.setVisibility(View.GONE);
            }

            if (post.getImageUrl() == null){
                postImage.setVisibility(View.GONE);
            }else {
                postImage.setVisibility(View.VISIBLE);
                Glide.with(mContext.getApplicationContext()).load(post.getImageUrl()).error(R.drawable.pdf_icon).into(postImage);
            }
            if (post.getNumLikes() > -1) {
                String likes = String.valueOf(post.getNumLikes());
                numLikes.setText(likes);
            }else {
                numLikes.setText("0");
            }
            if (post.getNumComments() > 1){
                String replies = post.getNumComments() + " Replies";
                comments.setText(replies);
            }else if (post.getNumComments() > -1){
                String reply = post.getNumComments() + " Reply";
                comments.setText(reply);
            }
            String tag = "#" + TextUtils.join(" #", post.getCategory());
            tags.setText(tag);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WritePostClass thisPost = getItem(Position);
                Intent intent = new Intent(mContext, AnswersActivity.class);
                if (thisPost != null) {
                    intent.putExtra(SefnetContract.REPLY_REF, thisPost.getId());
                }
                mContext.startActivity(intent);
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WritePostClass thisPost = getItem(Position);
                Intent intent = new Intent(mContext, OthersProfile.class);
                if (thisPost != null) {
                    intent.putExtra(SefnetContract.PROFILE_REF, thisPost.getAuthor());
                }
                mContext.startActivity(intent);
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WritePostClass thisPost = getItem(Position);
                Intent intent = new Intent(mContext, OthersProfile.class);
                if (thisPost != null) {
                    intent.putExtra(SefnetContract.PROFILE_REF, thisPost.getAuthor());
                }
                mContext.startActivity(intent);
            }
        });

        return itemView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return QuestionFilter;
    }

    private Filter QuestionFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<WritePostClass> suggestions = new ArrayList<>();
            if (allQuestions == null){
                allQuestions = new ArrayList<>();
                allQuestions.addAll(filterQuestions);
            }
            if (charSequence == null || charSequence.length() == 0){
                suggestions.addAll(allQuestions);
            }else {
                String searchString = charSequence.toString().toLowerCase().trim();
                for (WritePostClass post: allQuestions){
                    String text = post.getTitle().toLowerCase() + post.getDescription().toLowerCase();
                    if (text.contains(searchString)){
                        suggestions.add(post);
                    }
                }
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
            clear();
            //noinspection unchecked
            addAll((List) filterResults.values);
            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}
