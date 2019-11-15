package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sesyme.sesyme.R;

import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsHolder> {

    private Context mContext;
    private List<String> list;
    private OnClickListener listener;

    public TagsAdapter (){
        //Required constructor
    }

    public TagsAdapter(Context context, List<String> list){
        this.mContext = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TagsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.hash_tag, parent, false);
        return new TagsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagsHolder holder, int position) {
        holder.tag.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TagsHolder extends RecyclerView.ViewHolder{

        TextView tag;

        public TagsHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.name_hash_tag);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onTagSelected(position, tag);
                    }
                }
            });
        }
    }

    public void setOnClickListener(OnClickListener listener){
        this.listener = listener;
    }

    public interface OnClickListener{
        void onTagSelected(int Position, TextView tagView);
    }
}
