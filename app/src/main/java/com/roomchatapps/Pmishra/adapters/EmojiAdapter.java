package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {

    public interface OnEmojiClickListener {
        void onEmojiClick(String emojiId);
    }

    private final List<String> emojiIds = new ArrayList<>();
    private final OnEmojiClickListener listener;

    public EmojiAdapter(OnEmojiClickListener listener) {
        this.listener = listener;
        for (int i = 1; i <= 20; i++) {
            emojiIds.add(String.valueOf(i));
        }
    }

    public void updateList(List<String> newEmojiIds) {
        this.emojiIds.clear();
        if (newEmojiIds != null) {
            this.emojiIds.addAll(newEmojiIds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        String emojiId = emojiIds.get(position);
        Context context = holder.itemView.getContext();

        try {
            InputStream is = context.getAssets().open("emoji/" + emojiId + ".png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            holder.ivEmojiImage.setImageBitmap(bitmap);
            is.close();
        } catch (Exception e) {
            holder.ivEmojiImage.setImageResource(R.drawable.logo_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmojiClick(emojiId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emojiIds.size();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEmojiImage;

        EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEmojiImage = itemView.findViewById(R.id.ivEmojiImage);
        }
    }
}
