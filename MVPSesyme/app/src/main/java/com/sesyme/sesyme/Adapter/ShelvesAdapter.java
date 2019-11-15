package com.sesyme.sesyme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sesyme.sesyme.data.AddBook;
import com.sesyme.sesyme.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class ShelvesAdapter extends FirestoreRecyclerAdapter<AddBook, ShelvesAdapter.BookHolder> {

    private Context mContext;
    private OnShelfClickListener listener;


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options options with data to query
     */
    public ShelvesAdapter(Context context, @NonNull FirestoreRecyclerOptions<AddBook> options) {
        super(options);
        this.mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull BookHolder holder, int position, @NonNull AddBook model) {
        Glide.with(mContext).load(model.getCoverPageUrl())
                .error(R.drawable.maths).centerCrop().into(holder.bookCover);
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View shelfView = LayoutInflater.from(mContext).inflate(R.layout.books_in_shelf, viewGroup, false);
        return new BookHolder(shelfView);
    }

    public void setOnClickListener(OnShelfClickListener listener) {
        this.listener = listener;
    }

    public interface OnShelfClickListener {
        void OnBookClicked(DocumentSnapshot snapshot, int position);
    }

    class BookHolder extends RecyclerView.ViewHolder {

        ImageView bookCover;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.cover_books_in_shelf);

            bookCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.OnBookClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
}