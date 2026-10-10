package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CpHouseThemeModel;

import java.util.List;

public class CpHouseThemeAdapter extends RecyclerView.Adapter<CpHouseThemeAdapter.ViewHolder> {

    public interface OnThemeClickListener {
        void onThemeClick(CpHouseThemeModel theme, int position);
    }

    private List<CpHouseThemeModel> themeList;
    private OnThemeClickListener listener;

    public CpHouseThemeAdapter(List<CpHouseThemeModel> themeList, OnThemeClickListener listener) {
        this.themeList = themeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cp_house_theme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CpHouseThemeModel item = themeList.get(position);

        if (item.isCustomAdd()) {
            holder.ivThemePreview.setVisibility(View.GONE);
            holder.ivCustomPlus.setVisibility(View.VISIBLE);
            holder.ivLockIcon.setVisibility(View.VISIBLE);
            holder.tvLevelBadge.setVisibility(View.GONE);
            holder.btnUseTheme.setVisibility(View.GONE);
        } else {
            holder.ivCustomPlus.setVisibility(View.GONE);
            holder.ivThemePreview.setVisibility(View.VISIBLE);
            holder.ivThemePreview.setImageResource(item.getDrawableRes());

            holder.tvLevelBadge.setVisibility(View.VISIBLE);
            holder.tvLevelBadge.setText("LV." + item.getUnlockLevel());

            if (item.isLocked()) {
                holder.ivLockIcon.setVisibility(View.VISIBLE);
                holder.btnUseTheme.setVisibility(View.GONE);
            } else {
                holder.ivLockIcon.setVisibility(View.GONE);
                holder.btnUseTheme.setVisibility(View.VISIBLE);

                if (item.isEquipped()) {
                    holder.btnUseTheme.setText("In Use");
                    holder.btnUseTheme.setBackgroundColor(0xFF81C784);
                } else {
                    holder.btnUseTheme.setText("Use");
                    holder.btnUseTheme.setBackgroundColor(0x90E6A885);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThemeClick(item, holder.getAdapterPosition());
            }
        });

        holder.btnUseTheme.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThemeClick(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return themeList != null ? themeList.size() : 0;
    }

    public void updateList(List<CpHouseThemeModel> newList) {
        this.themeList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThemePreview;
        ImageView ivCustomPlus;
        ImageView ivLockIcon;
        TextView tvLevelBadge;
        TextView btnUseTheme;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThemePreview = itemView.findViewById(R.id.ivThemePreview);
            ivCustomPlus = itemView.findViewById(R.id.ivCustomPlus);
            ivLockIcon = itemView.findViewById(R.id.ivLockIcon);
            tvLevelBadge = itemView.findViewById(R.id.tvLevelBadge);
            btnUseTheme = itemView.findViewById(R.id.btnUseTheme);
        }
    }
}
