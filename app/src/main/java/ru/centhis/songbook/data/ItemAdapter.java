package ru.centhis.songbook.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.centhis.songbook.R;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Item> items;
    private ViewHolder.OnItemListener mOnItemListener;

    public ItemAdapter(Context context, List<Item> items, ViewHolder.OnItemListener onItemListener){
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.mOnItemListener = onItemListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameView;
        OnItemListener onItemListener;

        ViewHolder(View view, OnItemListener onItemListener){
            super(view);
            nameView = view.findViewById(R.id.name);
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.nameView.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



}
