package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.CommentsClass;
import com.sesyme.sesyme.data.LikedClass;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;

import javax.annotation.Nullable;

public class CommentsAdapter extends FirestoreRecyclerAdapter<CommentsClass, CommentsAdapter.CommentHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Methods methods;
    private Context mContext;
    private String name, Title, docID, path, email;
    private DocumentReference likesRef, notRef;
    private CollectionReference NotificationsRef;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options
     */
    public CommentsAdapter(Context context, @NonNull FirestoreRecyclerOptions<CommentsClass> options) {
        super(options);
        this.methods = new Methods(context);
        this.mContext = context;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null){
            email = user.getEmail();
        }
        NotificationsRef = db.collection(SefnetContract.NOTIFICATIONS);
    }

    @Override
    protected void onBindViewHolder(@NonNull final CommentHolder holder, int i, @NonNull final CommentsClass model) {
        db.collection(SefnetContract.USER_DETAILS).document(model.getAuthor()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            String author = documentSnapshot.getString(SefnetContract.FULL_NAME);
                            holder.userName.setText(author);
                            holder.comment.setText(model.getComment());
                            String date = String.valueOf(model.getSentAt());
                            String time = methods.covertTimeToText(date);
                            holder.time.setText(time);
                            String liked = model.getNumLikes() + " Liked";
                            holder.likes.setText(liked);
                        }
                    }
                });

        likesRef = db.collection(SefnetContract.LIKED).document(email + "Like" +
                getSnapshots().getSnapshot(holder.getAdapterPosition()).getId());
        likesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null)
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                holder.likeImage.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite_filled));
                            }else {
                                holder.likeImage.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_favorite_filled));
                            }
                            holder.likeImage.setTag("Liked");
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                holder.likeImage.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite));
                            }else {
                                holder.likeImage.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_favorite));
                            }
                            holder.likeImage.setTag("Like");
                        }
                    }
            }
        });

        holder.btLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                docID = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                path = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
                likesRef = db.collection(SefnetContract.LIKED).document(email + "Like" + docID);
                Log.d("MyComments", "onClick: " + holder.likeImage.getTag());
                if (holder.likeImage.getTag() != null) {
                    if (holder.likeImage.getTag().equals("Like")) {
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
                                            String text = "liked your comment: " + model.getComment();
                                                notRef.set(new NotificationClass(path, text,
                                                        email, "Like", model.getAuthor()));

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
                        holder.likeImage.setTag("Like");
                    }
                }
            }
        });

        holder.btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection(SefnetContract.USER_DETAILS).document(model.getAuthor()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {
                                    name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                                    String path = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                            .getReference().getPath();
                                    String[] segments = path.split("/");
                                    db.document(segments[0] + "/" + segments[1]).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot != null) {
                                                        Title = documentSnapshot.getString("title");
                                                        String deePath = getSnapshots().getSnapshot(holder.getAdapterPosition())
                                                                .getReference().getPath();
                                                        methods.generateDeepLinkReply(deePath, holder.getAdapterPosition(),
                                                                Title, "comment", name, model.getAuthor());
                                                    }
                                                }
                                            });

                                }
                            }
                        });
            }
        });
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentHolder(view);
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        TextView userName, comment, time, likes;
        LinearLayout btLike, btShare;
        ImageView likeImage;

        @SuppressWarnings("WeakerAccess")
        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username_comment_item);
            comment = itemView.findViewById(R.id.message_comment_item);
            time = itemView.findViewById(R.id.time_comment_item);
            likes = itemView.findViewById(R.id.likes_comment_item);
            btLike = itemView.findViewById(R.id.like_layout_comment_item);
            btShare = itemView.findViewById(R.id.share_layout_comment_item);
            likeImage = itemView.findViewById(R.id.bt_like_img_comment_item);
        }
    }
}
