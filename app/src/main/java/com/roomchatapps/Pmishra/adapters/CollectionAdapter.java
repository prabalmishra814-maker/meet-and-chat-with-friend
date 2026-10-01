package com.roomchatapps.Pmishra.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.utils.CoinUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// COLLECTION ACTIVITY
public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder> {

    private final List<CollectionItemModel> itemList = new ArrayList<>();
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(CollectionItemModel item);
    }

    public CollectionAdapter(List<CollectionItemModel> items, OnItemClickListener listener) {
        if (items != null) {
            this.itemList.addAll(items);
        }
        this.clickListener = listener;
    }

    public void updateList(List<CollectionItemModel> newItems) {
        this.itemList.clear();
        if (newItems != null) {
            this.itemList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_card, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        CollectionItemModel item = itemList.get(position);

        holder.tvQuantityBadge.setVisibility(View.GONE);
        holder.tvStatusBadge.setVisibility(View.GONE);
        holder.svgaPreview.setVisibility(View.GONE);
        holder.ivStaticPreview.setVisibility(View.VISIBLE);

        holder.tvItemName.setText(item.getItemName());

        if (item.getItemType() == CollectionItemModel.ItemType.GIFT_RECEIVED) {
            // RECEIVED GIFT COLLECTION
            holder.tvItemSubtitle.setText("🪙 " + CoinUtils.formatCoins(item.getItemPrice()));
            
            // Quantity count badge (x4, x10)
            if (item.getReceivedCount() > 0) {
                holder.tvQuantityBadge.setVisibility(View.VISIBLE);
                holder.tvQuantityBadge.setText("x" + item.getReceivedCount());
            }

            if (item.getGiftStoreItem() != null) {
                holder.ivStaticPreview.setImageResource(item.getGiftStoreItem().iconRes);
            } else {
                int res = item.getGiftIconRes();
                if (res == 0) res = R.drawable.gift_icon;
                holder.ivStaticPreview.setImageResource(res);
            }

            holder.cardCollectionItem.setStrokeColor(Color.parseColor("#40FFD700"));

        } else {
            // ENTRY EFFECT & FRAME COLLECTION
            StoreItemModel storeItem = item.getStoreItem();
            if (storeItem != null) {
                if (storeItem.getBadgeText() != null && !storeItem.getBadgeText().isEmpty()) {
                    holder.tvItemSubtitle.setText(storeItem.getBadgeText());
                } else {
                    holder.tvItemSubtitle.setText(item.getItemType() == CollectionItemModel.ItemType.ENTRY_EFFECT ? "Entry Effect" : "Avatar Frame");
                }

                if (storeItem.isEquipped()) {
                    holder.tvStatusBadge.setVisibility(View.VISIBLE);
                    holder.tvStatusBadge.setText("EQUIPPED ✨");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#40E0D0"));
                    holder.cardCollectionItem.setStrokeColor(Color.parseColor("#40E0D0"));
                } else {
                    holder.tvStatusBadge.setVisibility(View.VISIBLE);
                    holder.tvStatusBadge.setText("OWNED 🏆");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#FFD700"));
                    holder.cardCollectionItem.setStrokeColor(Color.parseColor("#25FFFFFF"));
                }

                // Check for SVGA
                if (storeItem.getSvgaPath() != null && !storeItem.getSvgaPath().trim().isEmpty()) {
                    holder.ivStaticPreview.setVisibility(View.GONE);
                    holder.svgaPreview.setVisibility(View.VISIBLE);
                    holder.svgaPreview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                    SVGAParser parser = new SVGAParser(holder.itemView.getContext());
                    parser.decodeFromAssets(storeItem.getSvgaPath(), new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                            if (holder.itemView.getContext() != null) {
                                holder.svgaPreview.setVideoItem(videoItem);
                                holder.svgaPreview.stepToFrame(0, true);
                            }
                        }

                        @Override
                        public void onError() {
                            holder.svgaPreview.setVisibility(View.GONE);
                            holder.ivStaticPreview.setVisibility(View.VISIBLE);
                            int resId = 0;
                            if (storeItem.getIconResName() != null) {
                                resId = holder.itemView.getResources().getIdentifier(storeItem.getIconResName(), "drawable", holder.itemView.getContext().getPackageName());
                            }
                            if (resId == 0) resId = R.drawable.family_owner_frame;
                            holder.ivStaticPreview.setImageResource(resId);
                        }
                    }, null);
                } else {
                    int resId = 0;
                    if (storeItem.getIconResName() != null) {
                        resId = holder.itemView.getResources().getIdentifier(storeItem.getIconResName(), "drawable", holder.itemView.getContext().getPackageName());
                    }
                    if (resId == 0) resId = R.drawable.family_owner_frame;
                    holder.ivStaticPreview.setImageResource(resId);
                }
            }
        }

        holder.cardCollectionItem.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCollectionItem;
        TextView tvQuantityBadge, tvStatusBadge;
        FrameLayout flPreviewContainer;
        ImageView ivStaticPreview;
        SVGAImageView svgaPreview;
        TextView tvItemName, tvItemSubtitle;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCollectionItem = itemView.findViewById(R.id.cardCollectionItem);
            tvQuantityBadge = itemView.findViewById(R.id.tvQuantityBadge);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            flPreviewContainer = itemView.findViewById(R.id.flPreviewContainer);
            ivStaticPreview = itemView.findViewById(R.id.ivStaticPreview);
            svgaPreview = itemView.findViewById(R.id.svgaPreview);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemSubtitle = itemView.findViewById(R.id.tvItemSubtitle);
        }
    }
}
