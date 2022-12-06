package ru.centhis.songbook.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import ru.centhis.songbook.R;

public class SearchSongResultAdapter extends RecyclerView.Adapter<SearchSongResultAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<SearchSongResult> items;
    private ViewHolder.OnItemListener mOnItemListener;

    public SearchSongResultAdapter(Context context, List<SearchSongResult> items, ViewHolder.OnItemListener onItemListener){
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        mOnItemListener = onItemListener;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_search_song_result, parent, false);
        return new ViewHolder(view, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchSongResult item = items.get(position);
        holder.songNameTV.setText(item.getSongName());
        holder.artistNameTV.setText(item.getArtistName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView songNameTV;
        final TextView artistNameTV;
        OnItemListener onItemListener;

        public ViewHolder(View view, OnItemListener onItemListener){
            super(view);
            songNameTV = view.findViewById(R.id.songNameTV);
            artistNameTV = view.findViewById(R.id.artistNameTV);
            view.setOnClickListener(this);
            this.onItemListener = onItemListener;

        }

        @Override
        public void onClick(View view) {
            onItemListener.onItemClick(getAdapterPosition());
        }

        public interface OnItemListener{
            void onItemClick(int position);
        }
    }
}
