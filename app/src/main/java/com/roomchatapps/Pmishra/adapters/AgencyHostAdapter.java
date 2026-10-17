package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.databinding.ItemAgencyHostBinding;
import com.roomchatapps.Pmishra.models.AgencyHostModel;

import java.util.List;

public class AgencyHostAdapter extends RecyclerView.Adapter<AgencyHostAdapter.HostViewHolder> {

    private final List<AgencyHostModel> hostList;
    private final OnHostActionListener listener;

    public interface OnHostActionListener {
        void onHostClick(AgencyHostModel host);
        void onHostOptionsClick(AgencyHostModel host, View anchorView);
    }

    public AgencyHostAdapter(List<AgencyHostModel> hostList, OnHostActionListener listener) {
        this.hostList = hostList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAgencyHostBinding binding = ItemAgencyHostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new HostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        AgencyHostModel host = hostList.get(position);
        holder.bind(host, listener);
    }

    @Override
    public int getItemCount() {
        return hostList != null ? hostList.size() : 0;
    }

    public static class HostViewHolder extends RecyclerView.ViewHolder {
        private final ItemAgencyHostBinding binding;

        public HostViewHolder(ItemAgencyHostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AgencyHostModel host, OnHostActionListener listener) {
            if (host == null) return;

            binding.tvHostName.setText(host.getHostName() != null ? host.getHostName() : "Host User");
            String numericId = com.roomchatapps.Pmishra.utils.UserProfileCache.getDisplayNumericId(host.getHostUid(), host.getProfileId());
            binding.tvHostProfileId.setText("ID: " + numericId);

            long coins = host.getMonthlyCoins() > 0 ? host.getMonthlyCoins() : host.getTotalCoins();
            binding.tvMonthlyCoins.setText("🪙 " + String.format("%,d", coins) + " Coins");

            binding.tvActiveHours.setText("⏱️ " + String.format("%.1f", host.getActiveHours()) + " hrs");

            String status = host.getStatus() != null ? host.getStatus().toUpperCase() : "ACTIVE";
            binding.tvHostStatusBadge.setText(status);

            if ("LIVE".equals(status)) {
                binding.tvLiveStatusDot.setVisibility(View.VISIBLE);
                binding.tvHostStatusBadge.setBackgroundResource(R.drawable.chip_charm_bg);
            } else {
                binding.tvLiveStatusDot.setVisibility(View.GONE);
                binding.tvHostStatusBadge.setBackgroundResource(R.drawable.chip_room_bg);
            }

            if (host.getHostAvatar() != null && !host.getHostAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(host.getHostAvatar())
                        .placeholder(R.drawable.app_create_room_ic)
                        .into(binding.ivHostAvatar);
            } else {
                binding.ivHostAvatar.setImageResource(R.drawable.app_create_room_ic);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onHostClick(host);
            });

            binding.btnHostOptions.setOnClickListener(v -> {
                if (listener != null) listener.onHostOptionsClick(host, v);
            });
        }
    }
}
