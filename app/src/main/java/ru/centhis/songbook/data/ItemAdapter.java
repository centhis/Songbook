package ru.centhis.songbook.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.Transliterator;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.centhis.songbook.R;
import ru.centhis.songbook.activities.ListSongActivity;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Item> items;
    private final Context context;
    private ViewHolder.OnItemListener mOnItemListener;

    public ItemAdapter(Context context, List<Item> items, ViewHolder.OnItemListener onItemListener){
        this.items = items;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mOnItemListener = onItemListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener{
        final TextView nameView;
        final TextView itemType;
        final TextView itemMp3TV;
        final TextView itemGuitarTV;
        final TextView itemUkuleleTV;
        final Context context;
        OnItemListener onItemListener;

        ViewHolder(View view, OnItemListener onItemListener, Context context){
            super(view);
            nameView = view.findViewById(R.id.name);
            itemType = view.findViewById(R.id.item_type);
            itemMp3TV = view.findViewById(R.id.itemMp3TV);
            itemGuitarTV = view.findViewById(R.id.itemGuitarTV);
            itemUkuleleTV = view.findViewById(R.id.itemUkuleleTV);
            this.context = context;
            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
            this.onItemListener = onItemListener;

        }

        @Override
        public void onClick(View view) {
            onItemListener.onItemClick(getAdapterPosition());
        }


        @SuppressLint("ResourceAsColor")
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            Activity activity = (Activity) context;
            if (activity.getClass().getSimpleName().equals("ListSongActivity")) {
                contextMenu.add(this.getAdapterPosition(), 111, 0, context.getString(R.string.edit));
                if (this.itemUkuleleTV.getCurrentTextColor() == R.color.black)
                    contextMenu.add(this.getAdapterPosition(), 110, 0, context.getString(R.string.edit_ukulele_version));
                else
                    contextMenu.add(this.getAdapterPosition(), 110, 0, context.getString(R.string.add_ukulele_version));
            }
            contextMenu.add(this.getAdapterPosition(), 121, 0, context.getString(R.string.text_song_delete_song));

        }

        public interface OnItemListener{
            void onItemClick(int position);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, mOnItemListener, context);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.nameView.setText(item.getName());
        holder.nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemListener.onItemClick(holder.getAdapterPosition());
            }
        });
        holder.nameView.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onLongClick(View view) {
                holder.nameView.showContextMenu(view.getPivotX(), view.getPivotY());
                return true;
            }
        });
        holder.itemType.setText(item.getType());
        if (item.isMp3()){
            holder.itemMp3TV.setBackgroundResource(R.color.white);
            holder.itemMp3TV.setTypeface(null, Typeface.BOLD);
        }
        if (item.isGuitar()) {
            holder.itemGuitarTV.setTextColor(R.color.black);
            holder.itemGuitarTV.setTypeface(null, Typeface.BOLD);
        }
        if (item.isUkulele()){
            holder.itemUkuleleTV.setTextColor(R.color.black);
            holder.itemUkuleleTV.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



}
