package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.ViewImage;
import com.sesyme.sesyme.data.MessageDetails;

public class AttachmentsAdapter extends FirestoreRecyclerAdapter<MessageDetails, AttachmentsAdapter.AttachmentHolder> {

    private Context mContext;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options recycler options
     */
    public AttachmentsAdapter(Context context, @NonNull FirestoreRecyclerOptions<MessageDetails> options) {
        super(options);
        this.mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull AttachmentHolder holder, int i, @NonNull final MessageDetails model) {
        Glide.with(mContext.getApplicationContext()).load(model.getAttachmentUrl())
                .error(R.drawable.pdf_icon).centerCrop().into(holder.attachment);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImage.class);
                intent.putExtra("attType", model.getAttType());
                intent.putExtra("imageUrl", model.getAttachmentUrl());
                mContext.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public AttachmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_item, parent, false);
        return new AttachmentHolder(view);
    }

    class AttachmentHolder extends RecyclerView.ViewHolder{

        ImageView attachment;

        @SuppressWarnings("WeakerAccess")
        public AttachmentHolder(@NonNull View itemView) {
            super(itemView);
            attachment = itemView.findViewById(R.id.image_attachment_item);
        }
    }
}
