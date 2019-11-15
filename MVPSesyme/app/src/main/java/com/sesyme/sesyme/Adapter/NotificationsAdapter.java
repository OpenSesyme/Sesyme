package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sesyme.sesyme.data.NotificationClass;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends FirestoreRecyclerAdapter<NotificationClass, NotificationsAdapter.NotificationHolder> {

    private Context mContext;
    private OnItemClickListener nListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userName, userImageUrl;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options query for getting notifications from firebase
     */
    public NotificationsAdapter(Context context, @NonNull FirestoreRecyclerOptions<NotificationClass> options) {
        super(options);
        this.mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final NotificationHolder holder, int position, @NonNull final NotificationClass model) {
        if (model.getSender() != null && model.getReceiver() != null) {
            if (model.getSeen() == 0) {
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.highlight));
            } else {
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.lightGray));
            }
            DocumentReference user = db.collection(SefnetContract.USER_DETAILS).document(model.getSender());
            user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        userImageUrl = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                        userName = documentSnapshot.getString(SefnetContract.FULL_NAME);
                        Glide.with(mContext.getApplicationContext()).load(userImageUrl)
                                .error(R.drawable.img).centerCrop().into(holder.nProfile);
                        if (userName != null) {
                            SpannableString message = new SpannableString(userName + " " + model.getNotificationText());
                            message.setSpan(new StyleSpan(Typeface.BOLD), 0, userName.length(), 0);
                            holder.user.setText(message);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Please Check your connection", Toast.LENGTH_SHORT).show();
                }
            });

            String timeString = String.valueOf(model.getTime());
            String time = this.covertTimeToText(timeString);
            holder.nTime.setText(time);

            holder.options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, holder.options);
                    popupMenu.inflate(R.menu.notification_options);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (menuItem.getItemId() == R.id.action_delete_notification) {
                                getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().delete();
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_card_view, viewGroup, false);
        return new NotificationHolder(view);
    }

    class NotificationHolder extends RecyclerView.ViewHolder {

        ImageView nProfile;
        TextView user;
        TextView nTime;
        ImageButton options;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            nProfile = itemView.findViewById(R.id.profile_image_notification);
            user = itemView.findViewById(R.id.name_notification);
            nTime = itemView.findViewById(R.id.time_notification);
            options = itemView.findViewById(R.id.options_notification);

            nProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && nListener != null) {
                        nListener.onPictureClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && nListener != null) {
                        nListener.onUsernameClicked(getSnapshots().getSnapshot(position), position);
                    }

                }
            });
        }
    }

    public interface OnItemClickListener {
        void onPictureClicked(DocumentSnapshot snapshot, int position);

        void onUsernameClicked(DocumentSnapshot snapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.nListener = listener;
    }

    private String covertTimeToText(String dataDate) {

        String convTime = "Moments ago";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = 0;
            if (pasTime != null) {
                dateDiff = nowTime.getTime() - pasTime.getTime();
            }

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + "sec ";
            } else if (minute < 60) {
                convTime = minute + "min ";
            } else if (hour < 24) {
                convTime = hour + "h ";
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + "y ";
                } else if (day > 30) {
                    convTime = (day / 30) + "month/s ";
                } else {
                    convTime = (day / 7) + "w ";
                }
            } else {
                convTime = day + "d ";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvertTimeE", e.getMessage());
        }
        return convTime;
    }
}
