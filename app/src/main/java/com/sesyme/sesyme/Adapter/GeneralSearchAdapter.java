package com.sesyme.sesyme.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.ViewImage;
import com.sesyme.sesyme.data.MessageDetails;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;
import java.util.List;

import developer.semojis.Helper.EmojiconTextView;

public class GeneralSearchAdapter extends ArrayAdapter<MessageDetails> implements Filterable {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<MessageDetails> allMessages;
    private List<MessageDetails> filteredMessages;
    private Context mContext;
    private String name, url, email;
    private Methods methods;
    private RelativeLayout myLayout, otherLayout;
    private CardView myCard, otherCard;
    private ImageView myImage, otherImage, myAttachment, otherAttachment;
    private TextView myName, otherName, myTime, otherTime;
    private EmojiconTextView myMessage, otherMessage;

    public GeneralSearchAdapter (Context context, List<MessageDetails> list){
        super(context, 0, list);
        this.mContext = context;
        filteredMessages = list;
        this.methods = new Methods(context);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            email = user.getEmail();
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_messages, parent, false);
        }
        myLayout = convertView.findViewById(R.id.my_message_layout);
        otherLayout = convertView.findViewById(R.id.other_message_layout);
        myImage = convertView.findViewById(R.id.message_pic_me);
        otherImage = convertView.findViewById(R.id.message_other_profile_pic);
        myName = convertView.findViewById(R.id.message_name_me);
        otherName = convertView.findViewById(R.id.message_other_name);
        myMessage = convertView.findViewById(R.id.message_body_me);
        otherMessage = convertView.findViewById(R.id.message_other_body);
        myTime = convertView.findViewById(R.id.message_time_me);
        otherTime = convertView.findViewById(R.id.message_time_other);
        myAttachment = convertView.findViewById(R.id.my_attachment);
        otherAttachment = convertView.findViewById(R.id.other_attachment);
        myCard = convertView.findViewById(R.id.image_card_me);
        otherCard = convertView.findViewById(R.id.image_card_other);
        final MessageDetails model = filteredMessages.get(position);

        db.collection(SefnetContract.USER_DETAILS).document(model.getSender())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null){
                    name = documentSnapshot.getString(SefnetContract.FULL_NAME);
                    url = documentSnapshot.getString(SefnetContract.PROFILE_URL);

                    String time = String.valueOf(model.getSentAt());
                    String showTime = methods.covertTimeToText(time);
                    if (model.getSender().equals(email)){
                        myLayout.setVisibility(View.VISIBLE);
                        otherLayout.setVisibility(View.GONE);
                        myName.setText(name);
                        if (model.getMessageBody().isEmpty()){
                            myMessage.setVisibility(View.GONE);
                        }else {
                            myMessage.setVisibility(View.VISIBLE);
                            myMessage.setText(model.getMessageBody());
                        }
                        myTime.setText(showTime);
                        Glide.with(mContext).load(url).error(R.drawable.img).into(myImage);
                        if (model.getAttachmentUri() != null){
                            if (model.getAttachmentUrl() != null){
                                Glide.with(mContext.getApplicationContext()).load(model.getAttachmentUrl())
                                        .centerCrop().into(myAttachment);
                            }
                            Glide.with(mContext.getApplicationContext()).load(Uri.parse(model.getAttachmentUri()))
                                    .centerCrop().into(myAttachment);
                            myCard.setVisibility(View.VISIBLE);
                            myAttachment.setVisibility(View.VISIBLE);
                        }else {
                            myCard.setVisibility(View.GONE);
                            myAttachment.setVisibility(View.GONE);
                        }
                    }else {
                        myLayout.setVisibility(View.GONE);
                        otherLayout.setVisibility(View.VISIBLE);
                        otherName.setText(name);
                        if (model.getMessageBody().isEmpty()){
                            otherMessage.setVisibility(View.GONE);
                        }else {
                            otherMessage.setVisibility(View.VISIBLE);
                            otherMessage.setText(model.getMessageBody());
                        }
                        otherTime.setText(showTime);
                        Glide.with(mContext.getApplicationContext()).load(url).error(R.drawable.img).into(otherImage);
                        if (model.getAttachmentUrl() != null){
                            Glide.with(mContext.getApplicationContext()).load(model.getAttachmentUrl())
                                    .centerCrop().into(otherAttachment);
                            otherCard.setVisibility(View.VISIBLE);
                            otherAttachment.setVisibility(View.VISIBLE);
                        }else {
                            otherCard.setVisibility(View.GONE);
                            otherAttachment.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        myLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (email.equals(model.getSender())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to delete this message?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.document(model.getMessagePath()).delete();
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
        myImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImage.class);
                intent.putExtra("attType", model.getAttType());
                intent.putExtra("imageUrl", model.getAttachmentUrl());
                mContext.startActivity(intent);
            }
        });
        otherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImage.class);
                intent.putExtra("attType", model.getAttType());
                intent.putExtra("imageUrl", model.getAttachmentUrl());
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new MessageFilter();
    }

    private class MessageFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            List<MessageDetails> suggestions = new ArrayList<>();
            if (allMessages == null){
                allMessages = new ArrayList<>();
                allMessages.addAll(filteredMessages);
            }
            if (charSequence == null || charSequence.length() < 1) {
                suggestions.addAll(allMessages);
            } else {
                for (MessageDetails message : allMessages) {
                    if (message.getMessageBody() != null) {
                        String fullName = message.getMessageBody().toLowerCase().trim();
                        if (fullName.contains(charSequence.toString().toLowerCase().trim())) {
                            suggestions.add(message);
                        }
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            Log.d("GeneralSearch", "performFiltering: " + results.values.toString());
            if (results.count == 0) {
                Toast.makeText(mContext, "No Results", Toast.LENGTH_SHORT).show();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            //noinspection unchecked
            addAll((List) filterResults.values);
            Log.d("GeneralSearch", "performFiltering: " + filterResults.values.toString());
            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
