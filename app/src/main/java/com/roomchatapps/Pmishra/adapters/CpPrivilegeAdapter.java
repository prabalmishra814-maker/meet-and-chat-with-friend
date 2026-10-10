package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CpPrivilegeModel;

import java.util.List;

public class CpPrivilegeAdapter extends RecyclerView.Adapter<CpPrivilegeAdapter.ViewHolder> {

    public interface OnPrivilegeClickListener {
        void onPrivilegeClick(CpPrivilegeModel model);
    }

    private List<CpPrivilegeModel> privilegeList;
    private OnPrivilegeClickListener listener;

    public CpPrivilegeAdapter(List<CpPrivilegeModel> privilegeList, OnPrivilegeClickListener listener) {
        this.privilegeList = privilegeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cp_privilege, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CpPrivilegeModel item = privilegeList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.ivIcon.setImageResource(item.getIconRes());

        if (item.isUnlocked()) {
            holder.ivLock.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.ivLock.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(0.65f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPrivilegeClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return privilegeList != null ? privilegeList.size() : 0;
    }

    public void updateList(List<CpPrivilegeModel> newList) {
        this.privilegeList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        ImageView ivLock;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivPrivilegeIcon);
            ivLock = itemView.findViewById(R.id.ivLockOverlay);
            tvTitle = itemView.findViewById(R.id.tvPrivilegeTitle);
        }
    }
}
