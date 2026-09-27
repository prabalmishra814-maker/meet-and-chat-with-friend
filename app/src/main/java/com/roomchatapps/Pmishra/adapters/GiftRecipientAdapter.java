package com.roomchatapps.Pmishra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;

import java.util.ArrayList;
import java.util.List;

public class GiftRecipientAdapter extends RecyclerView.Adapter<GiftRecipientAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(List<GiftRecipientModel> selectedList);
    }

    private final List<GiftRecipientModel> recipientList;
    private OnSelectionChangedListener selectionListener;

    public GiftRecipientAdapter(List<GiftRecipientModel> recipientList) {
        this.recipientList = recipientList != null ? recipientList : new ArrayList<>();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public List<GiftRecipientModel> getSelectedRecipients() {
        List<GiftRecipientModel> selected = new ArrayList<>();
        for (GiftRecipientModel item : recipientList) {
            if (!item.isAll() && item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_recipient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiftRecipientModel item = recipientList.get(position);
        Context context = holder.itemView.getContext();

        if (item.isAll()) {
            holder.flAllContainer.setVisibility(View.VISIBLE);
            holder.ivAvatar.setVisibility(View.GONE);
            holder.tvSeatBadge.setText("ALL");
            holder.tvName.setText("All Members");
        } else {
            holder.flAllContainer.setVisibility(View.GONE);
            holder.ivAvatar.setVisibility(View.VISIBLE);
            holder.tvSeatBadge.setText(item.getSeatBadge() != null ? item.getSeatBadge() : "");
            holder.tvName.setText(item.getName() != null ? item.getName() : "User");

            if (item.getAvatar() != null && !item.getAvatar().isEmpty()) {
                Glide.with(context)
                        .load(item.getAvatar())
                        .placeholder(R.drawable.gift2)
                        .error(R.drawable.gift2)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.gift2);
            }
        }

        // Selection ring
        if (item.isSelected()) {
            holder.vSelectionRing.setBackgroundResource(R.drawable.bg_recipient_avatar_selected);
        } else {
            holder.vSelectionRing.setBackgroundResource(R.drawable.bg_recipient_avatar_unselected);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            GiftRecipientModel clickedItem = recipientList.get(pos);

            if (clickedItem.isAll()) {
                boolean targetState = !clickedItem.isSelected();
                for (GiftRecipientModel model : recipientList) {
                    model.setSelected(targetState);
                }
            } else {
                clickedItem.setSelected(!clickedItem.isSelected());

                // Check if all individual items are now selected
                boolean allMembersSelected = true;
                boolean hasMembers = false;
                for (GiftRecipientModel model : recipientList) {
                    if (!model.isAll()) {
                        hasMembers = true;
                        if (!model.isSelected()) {
                            allMembersSelected = false;
                            break;
                        }
                    }
                }

                // Update "ALL" item selection state
                for (GiftRecipientModel model : recipientList) {
                    if (model.isAll()) {
                        model.setSelected(hasMembers && allMembersSelected);
                        break;
                    }
                }
            }

            notifyDataSetChanged();

            if (selectionListener != null) {
                selectionListener.onSelectionChanged(getSelectedRecipients());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipientList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vSelectionRing;
        ImageView ivAvatar;
        FrameLayout flAllContainer;
        TextView tvSeatBadge;
        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            vSelectionRing = itemView.findViewById(R.id.vSelectionRing);
            ivAvatar = itemView.findViewById(R.id.ivRecipientAvatar);
            flAllContainer = itemView.findViewById(R.id.flAllContainer);
            tvSeatBadge = itemView.findViewById(R.id.tvSeatBadge);
            tvName = itemView.findViewById(R.id.tvRecipientName);
        }
    }
}
