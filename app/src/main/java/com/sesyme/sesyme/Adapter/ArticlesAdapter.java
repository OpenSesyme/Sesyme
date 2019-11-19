package com.sesyme.sesyme.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.ReadArticle;
import com.sesyme.sesyme.SendFeedBack;
import com.sesyme.sesyme.WritePostActivity;
import com.sesyme.sesyme.data.LikedClass;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.WritePostClass;

import javax.annotation.Nullable;

public class ArticlesAdapter extends FirestoreRecyclerAdapter<WritePostClass, ArticlesAdapter.ArticleHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference likesRef, notRef;
    private Context mContext;
    private Methods methods;
    private String email, docID;
    private CollectionReference NotificationsRef;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options of firestore recycler adapter
     */
    public ArticlesAdapter(Context context, @NonNull FirestoreRecyclerOptions<WritePostClass> options) {
        super(options);
        this.mContext = context;
        this.methods = new Methods(context);
        NotificationsRef = db.collection(SefnetContract.NOTIFICATIONS);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final ArticleHolder holder, int i, @NonNull final WritePostClass model) {
        String user = model.getAuthor();
        db.collection(SefnetContract.USER_DETAILS).document(user).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                        holder.infoAuthor.setText(name);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        holder.infoBody.setText(model.getDescription());
        holder.infoTitle.setText(model.getTitle());
        if (model.getAttType() != null && model.getAttType().equals("PDF")){
            Glide.with(mContext.getApplicationContext()).load(R.drawable.default_pdf)
                    .fitCenter().into(holder.infoImage);
        }else {
            Glide.with(mContext.getApplicationContext()).load(model.getImageUrl())
                    .thumbnail(0.25f).error(R.drawable.default_pdf).into(holder.infoImage);
        }
        String time = String.valueOf(model.getDateTime());
        String showTime = methods.covertTimeToText(time);
        holder.infoTime.setText(showTime);
        holder.infoLikeText.setText(String.valueOf(model.getNumLikes()));

        if (model.getCategory() != null) {
            String tags = "#" + TextUtils.join(" #", model.getCategory());
            holder.tag.setText(tags);
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
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    holder.btLikeImage.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite_filled));
                                }else {
                                    holder.btLikeImage.setImageDrawable(mContext.getResources()
                                            .getDrawable(R.mipmap.ic_favorite_filled));
                                }
                                holder.btLikeImage.setTag("Liked");
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    holder.btLikeImage.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite));
                                }else {
                                    holder.btLikeImage.setImageDrawable(mContext.getResources()
                                            .getDrawable(R.mipmap.ic_favorite));
                                }
                                holder.btLikeImage.setTag("Like");
                            }
                        }
                }
            });
        }
        holder.infoLike.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                docID = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
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
                                            String text = "liked your " + model.getType().toLowerCase() + ": " + model.getTitle();
                                            notRef.set(new NotificationClass(docID, text,
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
                        holder.btLikeImage.setTag("Like");
                    }
                }
            }
        });
        holder.infoOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.infoOptions);
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
                                if (model.getType().equals("Article")) {
                                    Intent intent = new Intent(mContext, WritePostActivity.class);
                                    intent.putExtra("type", "Article");
                                    intent.putExtra("ArticleRefEdit", getSnapshots()
                                            .getSnapshot(holder.getAdapterPosition()).getId());
                                    mContext.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(mContext, WritePostActivity.class);
                                    intent.putExtra("type", "Docs");
                                    intent.putExtra("ReplyRefEdit", getSnapshots()
                                            .getSnapshot(holder.getAdapterPosition()).getReference().getPath());
                                    mContext.startActivity(intent);
                                }
                                break;
                            case R.id.action_delete_question:
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ReadArticle.class);
                intent.putExtra(SefnetContract.ARTICLE_ID,
                        getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getId());
                mContext.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ArticleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.article_post_item, parent, false);
        return new ArticleHolder(view);
    }

    class ArticleHolder extends RecyclerView.ViewHolder {

        ImageView infoOptions, infoImage, btLikeImage;
        TextView infoTitle, infoBody, infoAuthor, infoTime, infoLikeText, tag;
        LinearLayout infoLike, infoShare;

        @SuppressWarnings("WeakerAccess")
        public ArticleHolder(@NonNull View itemView) {
            super(itemView);
            infoImage = itemView.findViewById(R.id.article_post_image);
            infoOptions = itemView.findViewById(R.id.article_post_options);
            infoAuthor = itemView.findViewById(R.id.info_author);
            infoTitle = itemView.findViewById(R.id.info_title);
            infoTime = itemView.findViewById(R.id.info_time);
            infoBody = itemView.findViewById(R.id.info_post_body);
            btLikeImage = itemView.findViewById(R.id.info_bt_like_img);
            infoLike = itemView.findViewById(R.id.like_layout_info_card);
            infoShare = itemView.findViewById(R.id.share_layout_info_card);
            infoLikeText = itemView.findViewById(R.id.info_like_button);
            tag = itemView.findViewById(R.id.info_category);
        }
    }
}
