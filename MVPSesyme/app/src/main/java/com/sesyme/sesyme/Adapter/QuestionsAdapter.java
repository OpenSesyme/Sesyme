package com.sesyme.sesyme.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.SendFeedBack;
import com.sesyme.sesyme.ViewImage;
import com.sesyme.sesyme.WritePostActivity;
import com.sesyme.sesyme.data.LikedClass;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;

import javax.annotation.Nullable;

public class QuestionsAdapter extends FirestoreRecyclerAdapter<WritePostClass, QuestionsAdapter.ReviewsHolder> {

    private Context mContext;
    private OnItemClickListener rListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference likesRef, notRef;
    private CollectionReference NotificationsRef;
    private String email, docID, path;
    private Methods methods;
    private String name;
    private String questionTitle = "a Question";

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options of firestore recycler adapter
     */
    public QuestionsAdapter(Context context, @NonNull FirestoreRecyclerOptions<WritePostClass> options) {
        super(options);
        this.mContext = context;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        methods = new Methods(mContext);
        NotificationsRef = db.collection(SefnetContract.NOTIFICATIONS);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReviewsHolder holder, int position, @NonNull final WritePostClass model) {
        String user = model.getAuthor();
        db.collection(SefnetContract.USER_DETAILS).document(user).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String url = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                        String name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                        String aff = documentSnapshot.getString(SefnetContract.AFFILIATION);
                        if (aff != null) {
                            if (aff.equals("Lecturer")){
                                holder.lecturer.setVisibility(View.VISIBLE);
                            }else {
                                holder.lecturer.setVisibility(View.GONE);
                            }
                        }else {
                            holder.lecturer.setVisibility(View.GONE);
                        }
                        Glide.with(mContext.getApplicationContext()).load(url).error
                                (R.drawable.img).fitCenter().into(holder.userImage);
                        holder.username.setText(name);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        SpannableString des = new SpannableString(model.getDescription());

        for (int i = 0; i < des.length(); i++){
            if (des.charAt(i) == '@'){
                int m = 0;
                int l = i;
                for (int n = i; n < des.length(); n++) {
                    if (des.charAt(n) == ' '){
                            m++;
                    }else if (m < 2){
                        l++;
                    }
                }
                des.setSpan(new ForegroundColorSpan(mContext.getResources()
                        .getColor(R.color.iconsColor)), i, l, 0);
                des.setSpan(new StyleSpan(Typeface.BOLD), i, l, 0);
            }
        }
        holder.title.setText(model.getTitle());
        if (model.getDescription() != null && !model.getDescription().isEmpty()) {
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setVisibility(View.GONE);
        }
        holder.description.setText(des);

        if (model.getEdited() != null && model.getEdited()) {
            holder.edited.setVisibility(View.VISIBLE);
        } else {
            holder.edited.setVisibility(View.GONE);
        }


        holder.numLikes.setText(String.valueOf(model.getNumLikes()));
        String replies;
        if (model.getNumComments() > 1) {
            replies = model.getNumComments() + " Replies";
        } else {
            replies = model.getNumComments() + " Reply";
        }
        holder.comments.setText(replies);
        if (model.getCategory() != null) {
            String tags = "#" + TextUtils.join(" #", model.getCategory());
            holder.tags.setText(tags);
        }


        if (email != null) {
            likesRef = db.collection(SefnetContract.LIKED).document(email + "Like" +
                    getSnapshots().getSnapshot(holder.getAdapterPosition()).getId());
            likesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e == null)
                        if (documentSnapshot != null) {
                            if (documentSnapshot.exists()) {
                                holder.btLikeImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_black_24dp));
                                holder.btLikeImage.setTag("Liked");
                            } else {
                                holder.btLikeImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_border_black_24dp));
                                holder.btLikeImage.setTag("Like");
                            }
                        }
                }
            });
        }

        if (!model.getType().equals("Question")) {
            holder.userImage.setVisibility(View.GONE);
            holder.replyLayout.setVisibility(View.GONE);
            holder.title.setVisibility(View.GONE);
            holder.tags.setText(model.getType());
            holder.comments.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.tags.getLayoutParams();
            params.leftMargin = 40;
            ViewGroup.MarginLayoutParams desParams = (ViewGroup.MarginLayoutParams) holder.description.getLayoutParams();
            desParams.leftMargin = 40;

            if (model.getType().equals("Answer")) {
                holder.accept_layout.setVisibility(View.GONE);
                String path = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
                String[] pathSplit = path.split("/");
                db.document(pathSplit[0] + "/" + pathSplit[1]).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {
                                    String author = documentSnapshot.getString("author");
                                    if (author != null) {
                                        if (author.equals(email)) {
                                            if (model.getAccepted() == null) {
                                                holder.accept_layout.setVisibility(View.VISIBLE);
                                            } else {
                                                holder.accept_layout.setVisibility(View.GONE);
                                                if (model.getAccepted()) {
                                                    holder.accepted.setVisibility(View.VISIBLE);
                                                } else {
                                                    holder.accepted.setVisibility(View.GONE);
                                                }
                                            }
                                        } else {
                                            holder.accept_layout.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            if (model.getType().equals("Comment")) {
                holder.description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                holder.description.setPadding(0, 0, 0, 0);
            } else {
                holder.description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                int top = methods.dpToPx(10);
                holder.description.setPadding(0, top, 0, 0);
            }
        }

        if (model.getAccepted() != null && model.getAccepted()) {
            holder.accepted.setVisibility(View.VISIBLE);
        } else {
            holder.accepted.setVisibility(View.GONE);
        }

        if (model.getImageUrl() == null) {
            holder.postImage.setVisibility(View.GONE);
        } else {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(mContext.getApplicationContext()).load(model.getImageUrl())
                    .error(R.drawable.pdf_icon).fitCenter().into(holder.postImage);
        }

        String time = String.valueOf(model.getDateTime());
        String showTime = methods.covertTimeToText(time);
        holder.date.setText(showTime);

        holder.acceptYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
                String[] pathSplit = path.split("/");
                db.document(pathSplit[0] + "/" + pathSplit[1]).update("accepted", true);
                getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().update("accepted", true);
                holder.acceptNo.setBackground(mContext.getDrawable(R.drawable.button_follow));
                holder.accept_layout.setVisibility(View.GONE);
            }
        });

        holder.acceptNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().update("accepted", false);
                holder.acceptYes.setBackground(mContext.getDrawable(R.drawable.button_follow));
                holder.accept_layout.setVisibility(View.GONE);
            }
        });

        holder.likeLayout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                docID = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                path = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
                likesRef = db.collection(SefnetContract.LIKED).document(email + "Like" + docID);
                if (holder.btLikeImage.getTag() != null) {
                    if (holder.btLikeImage.getTag().equals("Like")) {
                        getSnapshots().getSnapshot(holder.getAdapterPosition())
                                .getReference().update("numLikes", FieldValue.increment(1));
                        likesRef.set(new LikedClass(getSnapshots()
                                .getSnapshot(holder.getAdapterPosition()).getId(), email));
                        if (model.getAuthor() != null && !model.getAuthor().equals(email)) {
                            notRef = NotificationsRef.document(email + "Like" + docID);
                            notRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot snapshot = task.getResult();
                                        if (snapshot != null && !snapshot.exists()) {
                                            String text;
                                            if (model.getType().equals("Question")) {
                                                text = "liked your " + model.getType() + ": " + model.getTitle();
                                            } else {
                                                text = "liked your " + model.getType();
                                            }
                                            if (model.getType().equals(SefnetContract.QUESTION)) {
                                                notRef.set(new NotificationClass(docID, text,
                                                        email, "Like", model.getAuthor()));
                                            } else {
                                                notRef.set(new NotificationClass(path, text,
                                                        email, "Like", model.getAuthor()));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        if (model.getNumLikes() > 0) {
                            getSnapshots().getSnapshot(holder.getAdapterPosition())
                                    .getReference().update("numLikes", FieldValue.increment(-1));
                        }
                        likesRef.delete();
                        holder.btLikeImage.setTag("Like");
                    }
                }
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection(SefnetContract.USER_DETAILS).document(model.getAuthor()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {
                                    name = documentSnapshot.getString(SefnetContract.FULL_NAME);

                                    if (model.getType().equals("Question")) {
                                        String questionPath = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                                .getReference().getPath();
                                        methods.generateDeepLinkQuestion(questionPath, model.getTitle(), name);
                                    } else {
                                        String path = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                                .getReference().getPath();
                                        String[] segments = path.split("/");
                                        db.document(segments[0] + "/" + segments[1]).get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot != null) {
                                                            questionTitle = documentSnapshot.getString("title");
                                                            String deePath = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                                                    .getReference().getPath();
                                                            methods.generateDeepLinkReply(deePath, holder.getAdapterPosition(),
                                                                    questionTitle, model.getType(), name);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        });
            }
        });

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImage.class);
                intent.putExtra("imageUrl", model.getImageUrl());
                mContext.startActivity(intent);
            }
        });

        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.options);
                popupMenu.inflate(R.menu.posts_options);
                if (email.equals(model.getAuthor())) {
                    popupMenu.getMenu().setGroupVisible(R.id.author_group, true);
                    popupMenu.getMenu().setGroupVisible(R.id.others_group, false);
                } else {
                    popupMenu.getMenu().setGroupVisible(R.id.author_group, false);
                    popupMenu.getMenu().setGroupVisible(R.id.others_group, true);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_edit_question:
                                if (model.getType().equals("Question")) {
                                    Intent intent = new Intent(mContext, WritePostActivity.class);
                                    intent.putExtra("type", "Question");
                                    intent.putExtra("QuestionRefEdit", getSnapshots()
                                            .getSnapshot(holder.getAdapterPosition()).getId());
                                    mContext.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(mContext, WritePostActivity.class);
                                    intent.putExtra("type", "Reply");
                                    intent.putExtra("ReplyRefEdit", getSnapshots()
                                            .getSnapshot(holder.getAdapterPosition()).getReference().getPath());
                                    mContext.startActivity(intent);
                                }
                                break;
                            case R.id.action_delete_question:
                                if (model.getType().equals("Question")) {
                                    db.collection(SefnetContract.USER_DETAILS).document(email)
                                            .update("numQuestions", FieldValue.increment(-1));
                                } else if (model.getType().equals("Answer")) {
                                    db.collection(SefnetContract.USER_DETAILS).document(email)
                                            .update("numAnswers", FieldValue.increment(-1));
                                    String path = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                            .getReference().getPath();
                                    String[] segments = path.split("/");
                                    db.document(segments[0] + "/" + segments[1])
                                            .update("numComments", FieldValue.increment(-1));
                                } else {
                                    String path = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                            .getReference().getPath();
                                    String[] segments = path.split("/");
                                    db.document(segments[0] + "/" + segments[1])
                                            .update("numComments", FieldValue.increment(-1));
                                }
                                getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().delete();
                                break;
                            case R.id.action_report_question:
                                Intent intent = new Intent(mContext, SendFeedBack.class);
                                intent.putExtra("type", "Report");
                                intent.putExtra("postRef", getSnapshots()
                                        .getSnapshot(holder.getAdapterPosition()).getId());
                                mContext.startActivity(intent);
                                break;
                            case R.id.action_hide_question:
                                holder.itemView.setVisibility(View.GONE);
                                holder.itemView.getLayoutParams().height = 0;
                                ViewGroup.MarginLayoutParams params =
                                        (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
                                params.setMargins(0, 0, 0, 0);
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }


    @NonNull
    @Override
    public ReviewsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.questions_card_view, viewGroup, false);
        return new ReviewsHolder(view);
    }

    class ReviewsHolder extends RecyclerView.ViewHolder {
        ImageView userImage, postImage, btLikeImage, options, accepted;
        TextView username, date, title, description, comments, tags, numLikes, acceptYes, acceptNo, edited, lecturer;
        LinearLayout share, likeLayout, replyLayout, accept_layout;
        String email;
        int docId;

        @SuppressWarnings("WeakerAccess")
        public ReviewsHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.comment_user_image);
            postImage = itemView.findViewById(R.id.ratting_image);
            username = itemView.findViewById(R.id.comment_user_name);
            date = itemView.findViewById(R.id.comment_date_time);
            title = itemView.findViewById(R.id.title_question_reviews);
            description = itemView.findViewById(R.id.title_reviews);
            comments = itemView.findViewById(R.id.comment_comments_count);
            share = itemView.findViewById(R.id.share_layout_post_card);
            likeLayout = itemView.findViewById(R.id.like_layout_post_card);
            replyLayout = itemView.findViewById(R.id.comment_layout_post_card);
            btLikeImage = itemView.findViewById(R.id.bt_like_img);
            tags = itemView.findViewById(R.id.tags_post);
            numLikes = itemView.findViewById(R.id.tv_like_questions_card);
            options = itemView.findViewById(R.id.options_question_card);
            accepted = itemView.findViewById(R.id.answer_accepted);
            accept_layout = itemView.findViewById(R.id.accept_answer_layout);
            acceptYes = itemView.findViewById(R.id.yes_accept);
            acceptNo = itemView.findViewById(R.id.no_accept);
            edited = itemView.findViewById(R.id.edited);
            lecturer = itemView.findViewById(R.id.lecturer);
            email = "";
            if (getAdapterPosition() > -1) {
                docId = getAdapterPosition();
            } else {
                docId = 0;
            }

            replyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && rListener != null) {
                        rListener.onCommentClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && rListener != null) {
                        rListener.onProfileClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && rListener != null) {
                        rListener.onProfileClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && rListener != null) {
                        rListener.onTitleClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && rListener != null) {
                        rListener.onTitleClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && rListener != null) {
                        rListener.onTitleClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.rListener = listener;
    }

    public interface OnItemClickListener {

        void onCommentClicked(DocumentSnapshot snapshot, int position);

        void onProfileClicked(DocumentSnapshot snapshot, int position);

        void onTitleClicked(DocumentSnapshot snapshot, int position);
    }
}
