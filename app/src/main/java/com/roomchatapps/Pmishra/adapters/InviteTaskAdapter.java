package com.roomchatapps.Pmishra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.InviteTaskModel;

import java.util.List;

public class InviteTaskAdapter extends RecyclerView.Adapter<InviteTaskAdapter.ViewHolder> {

    private final List<InviteTaskModel> tasks;

    public InviteTaskAdapter(List<InviteTaskModel> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InviteTaskModel task = tasks.get(position);

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getDescription());

        if (task.getIconRes() != 0) {
            holder.ivTaskIcon.setImageResource(task.getIconRes());
        } else {
            holder.ivTaskIcon.setImageResource(R.drawable.gift_icon);
        }

        if (task.isHasHelpInfo()) {
            holder.tvHelpIcon.setVisibility(View.VISIBLE);
            holder.tvHelpIcon.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), task.getTitle() + "\n" + task.getDescription(), Toast.LENGTH_LONG).show());
        } else {
            holder.tvHelpIcon.setVisibility(View.GONE);
        }

        holder.tvRewardCount.setText(String.valueOf(task.getCurrentCount()));
        if ("CHIP".equalsIgnoreCase(task.getRewardType())) {
            holder.tvRewardIcon.setText("🍀");
        } else {
            holder.tvRewardIcon.setText("🪙");
        }
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTaskIcon;
        TextView tvTaskTitle, tvTaskDescription, tvHelpIcon, tvRewardCount, tvRewardIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTaskIcon = itemView.findViewById(R.id.ivTaskIcon);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvHelpIcon = itemView.findViewById(R.id.tvHelpIcon);
            tvRewardCount = itemView.findViewById(R.id.tvRewardCount);
            tvRewardIcon = itemView.findViewById(R.id.tvRewardIcon);
        }
    }
}
