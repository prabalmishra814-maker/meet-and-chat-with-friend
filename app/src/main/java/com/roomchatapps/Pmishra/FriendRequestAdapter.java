package com.roomchatapps.Pmishra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.models.FriendRequestModel;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private final List<FriendRequestModel> requests;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onAccept(FriendRequestModel request);
        void onReject(FriendRequestModel request);
    }

    public FriendRequestAdapter(List<FriendRequestModel> requests, OnActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequestModel request = requests.get(position);

        holder.tvName.setText(request.getSenderName());
        Glide.with(holder.itemView.getContext())
                .load(request.getSenderAvatar())
                .placeholder(R.drawable.ic_person)
                .into(holder.ivAvatar);

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(request);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, btnReject;
        TextView tvName, btnAccept;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivSenderAvatar);
            tvName = itemView.findViewById(R.id.tvSenderName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
