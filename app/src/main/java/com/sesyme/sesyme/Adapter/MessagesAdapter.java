package com.sesyme.sesyme.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.sesyme.sesyme.ViewImage;
import com.sesyme.sesyme.data.LikedClass;
import com.sesyme.sesyme.data.MessageDetails;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.List;

import javax.annotation.Nullable;

import developer.semojis.Helper.EmojiconTextView;

public class MessagesAdapter extends FirestoreRecyclerAdapter<MessageDetails, MessagesAdapter.MessageHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference likesRef, notRef;
    private CollectionReference NotificationsRef;
    private Context mContext;
    private Methods methods;
    private onClickListener listener;
    private List<String> admins;
    private String url, name, email, docID, path, myName;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options of firestore recycler adapter
     */
    public MessagesAdapter(Context context, @NonNull FirestoreRecyclerOptions<MessageDetails> options) {
        super(options);
        this.mContext = context;
        this.methods = new Methods(context);
        NotificationsRef = db.collection(SefnetContract.NOTIFICATIONS_CLUSTER);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final MessageHolder holder, int i, @NonNull final MessageDetails model) {
        db.collection(SefnetContract.USER_DETAILS).document(model.getSender())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                    url = documentSnapshot.getString(SefnetContract.PROFILE_URL);

                    String time = String.valueOf(model.getSentAt());
                    String showTime = methods.covertTimeToText(time);
                    if (model.getSender().equals(email)) {
                        holder.myLayout.setVisibility(View.VISIBLE);
                        holder.otherLayout.setVisibility(View.GONE);
                        holder.myName.setText(name);
                        if (model.getMessageBody().isEmpty()) {
                            holder.myMessage.setVisibility(View.GONE);
                        } else {
                            holder.myMessage.setVisibility(View.VISIBLE);
                            holder.myMessage.setText(model.getMessageBody());
                        }
                        holder.myTime.setText(showTime);
                        Glide.with(mContext).load(url).error(R.drawable.img).into(holder.myImage);
                        if (model.getAttachmentUri() != null) {
                            Glide.with(mContext.getApplicationContext()).load(Uri.parse(model.getAttachmentUri()))
                                    .error(R.drawable.default_pdf).centerCrop().into(holder.myAttachment);
                            if (model.getAttachmentUrl() != null) {
                                Glide.with(mContext.getApplicationContext()).load(model.getAttachmentUrl())
                                        .error(R.drawable.default_pdf).centerCrop().into(holder.myAttachment);
                            }
                            holder.myCard.setVisibility(View.VISIBLE);
                            holder.myAttachment.setVisibility(View.VISIBLE);
                        } else {
                            holder.myCard.setVisibility(View.GONE);
                            holder.myAttachment.setVisibility(View.GONE);
                        }
                    } else {
                        holder.myLayout.setVisibility(View.GONE);
                        holder.otherLayout.setVisibility(View.VISIBLE);
                        holder.otherName.setText(name);
                        if (model.getMessageBody().isEmpty()) {
                            holder.otherMessage.setVisibility(View.GONE);
                        } else {
                            holder.otherMessage.setVisibility(View.VISIBLE);
                            holder.otherMessage.setText(model.getMessageBody());
                        }
                        holder.otherTime.setText(showTime);
                        Glide.with(mContext.getApplicationContext()).load(url).error(R.drawable.img).into(holder.otherImage);
                        if (model.getAttachmentUrl() != null) {
                            Glide.with(mContext.getApplicationContext()).load(model.getAttachmentUrl())
                                    .centerCrop().into(holder.otherAttachment);
                            holder.otherCard.setVisibility(View.VISIBLE);
                            holder.otherAttachment.setVisibility(View.VISIBLE);
                        } else {
                            holder.otherCard.setVisibility(View.GONE);
                            holder.otherAttachment.setVisibility(View.GONE);
                        }
                    }
                }

            }
        });

        String mPath = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
        String[] paths = mPath.split("/");
        if (paths.length > 0) {
            db.collection(SefnetContract.CLUSTERS_DETAILS).document(paths[0])
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //noinspection unchecked
                    admins = (List<String>) documentSnapshot.get("admins");
                }
            });
        }

        if (email != null){
            likesRef = db.collection(SefnetContract.LIKED).document(email + "Chat" +
                    getSnapshots().getSnapshot(holder.getAdapterPosition()).getId());
            likesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e == null)
                        if (documentSnapshot != null) {
                            if (documentSnapshot.exists()) {
                                holder.btLikeMe.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite_filled));
                                holder.btLikeMe.setTag("Liked");
                                holder.btLikeOther.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite_filled));
                                holder.btLikeOther.setTag("Liked");
                            } else {
                                holder.btLikeMe.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite));
                                holder.btLikeMe.setTag("Like");
                                holder.btLikeOther.setImageDrawable(mContext.getDrawable(R.mipmap.ic_favorite));
                                holder.btLikeOther.setTag("Like");
                            }
                        }
                }
            });

            db.collection(SefnetContract.USER_DETAILS).document(email).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null){
                        myName = documentSnapshot.getString(SefnetContract.FULL_NAME);
                    }
                }
            });
        }

        holder.numLikeMe.setText(String.valueOf(model.getNumLikes()));
        holder.numLikeOther.setText(String.valueOf(model.getNumLikes()));

        holder.myLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (email.equals(model.getSender())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to delete this message?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().delete();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return false;
            }
        });
        holder.otherLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (admins != null && admins.contains(email)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to delete this message?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String deletedString = myName + " deleted this message";
                            getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference()
                                    .update("messageBody", deletedString);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return false;
            }
        });
        holder.myCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImage.class);
                intent.putExtra("attType", model.getAttType());
                intent.putExtra("imageUrl", model.getAttachmentUrl());
                intent.putExtra("path", getSnapshots()
                        .getSnapshot(holder.getAdapterPosition()).getReference().getPath());
                mContext.startActivity(intent);
            }
        });
        holder.otherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImage.class);
                intent.putExtra("attType", model.getAttType());
                intent.putExtra("imageUrl", model.getAttachmentUrl());
                intent.putExtra("path", getSnapshots()
                        .getSnapshot(holder.getAdapterPosition()).getReference().getPath());
                mContext.startActivity(intent);
            }
        });
        holder.btLikeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                docID = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                path = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
                likesRef = db.collection(SefnetContract.LIKED).document(email + "Chat" + docID);
                if (holder.btLikeMe.getTag() != null) {
                    if (holder.btLikeMe.getTag().equals("Like")) {
                        getSnapshots().getSnapshot(holder.getAdapterPosition())
                                .getReference().update("numLikes", FieldValue.increment(1));
                        likesRef.set(new LikedClass(getSnapshots()
                                .getSnapshot(holder.getAdapterPosition()).getId(), email));
                        if (model.getSender() != null && !model.getSender().equals(email)) {
                            notRef = NotificationsRef.document(email + "Chat" + docID);
                            notRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot snapshot = task.getResult();
                                        if (snapshot != null && !snapshot.exists()) {
                                            String text = "liked your message " + model.getMessageBody();
                                            notRef.set(new NotificationClass(path, text,
                                                    email, "Chat", model.getSender()));
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
                        holder.btLikeMe.setTag("Like");
                    }
                }
            }
        });
        holder.btLikeOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                docID = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                path = getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getPath();
                likesRef = db.collection(SefnetContract.LIKED).document(email + "Chat" + docID);
                if (holder.btLikeOther.getTag() != null) {
                    if (holder.btLikeOther.getTag().equals("Like")) {
                        getSnapshots().getSnapshot(holder.getAdapterPosition())
                                .getReference().update("numLikes", FieldValue.increment(1));
                        likesRef.set(new LikedClass(getSnapshots()
                                .getSnapshot(holder.getAdapterPosition()).getId(), email));
                        if (model.getSender() != null && !model.getSender().equals(email)) {
                            notRef = NotificationsRef.document(email + "Chat" + docID);
                            notRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot snapshot = task.getResult();
                                        if (snapshot != null && !snapshot.exists()) {
                                            String text = "liked your message " + model.getMessageBody();
                                            notRef.set(new NotificationClass(path, text,
                                                    email, "Chat", model.getSender()));
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
                        holder.btLikeOther.setTag("Like");
                    }
                }
            }
        });

        holder.otherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClicked(position, model.getSender());
                }
            }
        });
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_messages, parent, false);
        return new MessageHolder(view);
    }

    class MessageHolder extends RecyclerView.ViewHolder {

        RelativeLayout myLayout, otherLayout;
        LinearLayout myText, otherText;
        CardView myCard, otherCard;
        ImageView myImage, otherImage, myAttachment, otherAttachment, btLikeMe, btLikeOther;
        TextView myName, otherName, myTime, otherTime, numLikeMe, numLikeOther;
        EmojiconTextView myMessage, otherMessage;

        @SuppressWarnings("WeakerAccess")
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            myLayout = itemView.findViewById(R.id.my_message_layout);
            otherLayout = itemView.findViewById(R.id.other_message_layout);
            myImage = itemView.findViewById(R.id.message_pic_me);
            otherImage = itemView.findViewById(R.id.message_other_profile_pic);
            myName = itemView.findViewById(R.id.message_name_me);
            otherName = itemView.findViewById(R.id.message_other_name);
            myMessage = itemView.findViewById(R.id.message_body_me);
            otherMessage = itemView.findViewById(R.id.message_other_body);
            myTime = itemView.findViewById(R.id.message_time_me);
            otherTime = itemView.findViewById(R.id.message_time_other);
            myAttachment = itemView.findViewById(R.id.my_attachment);
            otherAttachment = itemView.findViewById(R.id.other_attachment);
            myText = itemView.findViewById(R.id.my_text_layout);
            otherText = itemView.findViewById(R.id.other_text_layout);
            myCard = itemView.findViewById(R.id.image_card_me);
            otherCard = itemView.findViewById(R.id.image_card_other);
            btLikeMe = itemView.findViewById(R.id.like_message_me);
            btLikeOther = itemView.findViewById(R.id.like_message_other);
            numLikeMe = itemView.findViewById(R.id.like_message_text_me);
            numLikeOther = itemView.findViewById(R.id.like_message_text);
        }
    }

    public void setOnClickListener(onClickListener listener){
        this.listener = listener;
    }

    public interface onClickListener{
        void onUserClicked(int position, String email);
    }
}
