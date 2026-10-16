package com.roomchatapps.Pmishra.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.databinding.ItemAgencyWithdrawalBinding;
import com.roomchatapps.Pmishra.models.AgencyWithdrawalModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgencyWithdrawalAdapter extends RecyclerView.Adapter<AgencyWithdrawalAdapter.WithdrawalViewHolder> {

    private final List<AgencyWithdrawalModel> list;

    public AgencyWithdrawalAdapter(List<AgencyWithdrawalModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public WithdrawalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAgencyWithdrawalBinding binding = ItemAgencyWithdrawalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new WithdrawalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WithdrawalViewHolder holder, int position) {
        AgencyWithdrawalModel item = list.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class WithdrawalViewHolder extends RecyclerView.ViewHolder {
        private final ItemAgencyWithdrawalBinding binding;

        public WithdrawalViewHolder(ItemAgencyWithdrawalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AgencyWithdrawalModel item) {
            if (item == null) return;

            String method = item.getPaymentMethod() != null ? item.getPaymentMethod() : "Withdrawal";
            binding.tvPaymentMethodTitle.setText(method);
            binding.tvAccountDetails.setText("Account: " + (item.getAccountDetails() != null ? item.getAccountDetails() : "N/A"));

            if (method.toLowerCase().contains("upi")) {
                binding.tvPaymentMethodIcon.setText("📱");
            } else if (method.toLowerCase().contains("bank")) {
                binding.tvPaymentMethodIcon.setText("🏦");
            } else if (method.toLowerCase().contains("paypal")) {
                binding.tvPaymentMethodIcon.setText("💳");
            } else {
                binding.tvPaymentMethodIcon.setText("💸");
            }

            binding.tvWithdrawAmount.setText(String.format(Locale.getDefault(), "₹%.2f", item.getAmountUsd()));

            String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "PENDING";
            binding.tvStatusBadge.setText(status);

            switch (status) {
                case "COMPLETED":
                case "APPROVED":
                    binding.tvStatusBadge.setText("COMPLETED");
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.chip_charm_bg);
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#4CAF50"));
                    break;
                case "REJECTED":
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.chip_room_bg);
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#FF5252"));
                    break;
                default:
                    binding.tvStatusBadge.setText("PENDING");
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_referral_gold_pill);
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#FFD700"));
                    break;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            binding.tvTimestamp.setText(sdf.format(new Date(item.getTimestamp())));

            String idStr = item.getWithdrawalId() != null ? item.getWithdrawalId() : "";
            binding.tvWithdrawalId.setText("ID: " + (idStr.length() > 10 ? idStr.substring(0, 10) : idStr));

            if (item.getAdminNote() != null && !item.getAdminNote().isEmpty()) {
                binding.tvAdminNote.setVisibility(View.VISIBLE);
                binding.tvAdminNote.setText("Note: " + item.getAdminNote());
            } else {
                binding.tvAdminNote.setVisibility(View.GONE);
            }
        }
    }
}
