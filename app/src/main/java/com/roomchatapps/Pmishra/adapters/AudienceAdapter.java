package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.SeatAnimationManager;
import com.roomchatapps.Pmishra.models.User;
import java.util.List;

public class AudienceAdapter extends RecyclerView.Adapter<AudienceAdapter.ViewHolder> {

    private List<User> audienceList;
    private int lastAnimatedPosition = -1;

    public AudienceAdapter(List<User> audienceList) {
        this.audienceList = audienceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audience, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = audienceList.get(position);

        // Entrance animation
        if (position > lastAnimatedPosition) {
            SeatAnimationManager.animateSeatEntrance(holder.itemView, false);
            lastAnimatedPosition = position;
        }

        holder.tvAudienceName.setText(user.getUserName());
        holder.tvAudienceStatus.setText("Listening...");

        Glide.with(holder.itemView.getContext())
                .load(user.getUserIcon())
                .placeholder(R.drawable.logo_placeholder)
                .into(holder.ivAudienceProfile);
    }

    @Override
    public int getItemCount() {
        return audienceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAudienceProfile;
        TextView tvAudienceName, tvAudienceStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAudienceProfile = itemView.findViewById(R.id.ivAudienceProfile);
            tvAudienceName = itemView.findViewById(R.id.tvAudienceName);
            tvAudienceStatus = itemView.findViewById(R.id.tvAudienceStatus);
        }
    }
}
