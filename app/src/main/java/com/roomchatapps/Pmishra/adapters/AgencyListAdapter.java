package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.databinding.ItemAgencyListBinding;
import com.roomchatapps.Pmishra.models.AgencyModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AgencyListAdapter extends RecyclerView.Adapter<AgencyListAdapter.AgencyViewHolder> {

    public interface OnAgencyClickListener {
        void onJoinClick(AgencyModel agency);
    }

    private final List<AgencyModel> originalList;
    private final List<AgencyModel> filteredList;
    private final OnAgencyClickListener listener;

    public AgencyListAdapter(List<AgencyModel> agencyList, OnAgencyClickListener listener) {
        this.originalList = new ArrayList<>();
        if (agencyList != null) {
            this.originalList.addAll(agencyList);
        }
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    public void updateData(List<AgencyModel> newList) {
        this.originalList.clear();
        if (newList != null) {
            this.originalList.addAll(newList);
        }
        filter("");
    }

    public void filter(String text) {
        filteredList.clear();
        if (text == null || text.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String query = text.trim().toLowerCase(Locale.getDefault());
            for (AgencyModel item : originalList) {
                boolean nameMatch = item.getAgencyName() != null && item.getAgencyName().toLowerCase(Locale.getDefault()).contains(query);
                boolean idMatch = item.getAgencyId() != null && item.getAgencyId().toLowerCase(Locale.getDefault()).contains(query);
                boolean ownerMatch = item.getOwnerName() != null && item.getOwnerName().toLowerCase(Locale.getDefault()).contains(query);

                if (nameMatch || idMatch || ownerMatch) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredList.size();
    }

    @NonNull
    @Override
    public AgencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAgencyListBinding binding = ItemAgencyListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AgencyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AgencyViewHolder holder, int position) {
        holder.bind(filteredList.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    class AgencyViewHolder extends RecyclerView.ViewHolder {

        private final ItemAgencyListBinding binding;

        public AgencyViewHolder(@NonNull ItemAgencyListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AgencyModel agency) {
            String agencyName = agency.getAgencyName() != null ? agency.getAgencyName() : "Official Agency";
            String agencyId = agency.getAgencyId() != null ? agency.getAgencyId() : "N/A";
            String ownerName = agency.getOwnerName() != null ? agency.getOwnerName() : "Owner";

            binding.tvAgencyName.setText(agencyName);
            binding.tvAgencyDetails.setText("ID: " + agencyId + " • Owner: " + ownerName);

            if (agency.getAgencyLogo() != null && !agency.getAgencyLogo().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(agency.getAgencyLogo())
                        .placeholder(R.drawable.app_create_room_ic)
                        .error(R.drawable.app_create_room_ic)
                        .into(binding.ivAgencyLogo);
            } else {
                binding.ivAgencyLogo.setImageResource(R.drawable.app_create_room_ic);
            }

            binding.btnJoinAgencyItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJoinClick(agency);
                }
            });
        }
    }
}
