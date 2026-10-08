package com.roomchatapps.Pmishra.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.models.User;

public class CpInviteConfirmDialog extends Dialog {

    public interface OnConfirmListener {
        void onConfirm(User targetUser);
    }

    private final User targetUser;
    private OnConfirmListener onConfirmListener;

    public CpInviteConfirmDialog(@NonNull Context context, @NonNull User targetUser) {
        super(context);
        this.targetUser = targetUser;
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.onConfirmListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_cp_invite_confirm);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        ShapeableImageView ivFriendAvatar = findViewById(R.id.ivFriendAvatar);
        TextView tvFriendName = findViewById(R.id.tvFriendName);
        TextView tvFriendId = findViewById(R.id.tvFriendId);
        TextView btnCancel = findViewById(R.id.btnCancel);
        TextView btnConfirmInvite = findViewById(R.id.btnConfirmInvite);

        if (targetUser != null) {
            String name = targetUser.getUserName();
            tvFriendName.setText(!TextUtils.isEmpty(name) ? name : "User");

            String displayId = targetUser.getProfileId();
            if (TextUtils.isEmpty(displayId)) {
                displayId = targetUser.getUserId();
            }
            if (TextUtils.isEmpty(displayId)) {
                displayId = "100098180";
            }
            tvFriendId.setText("ID:" + displayId);

            String avatarUrl = targetUser.getUserIcon();
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.img_20260904_135725)
                        .error(R.drawable.img_20260904_135725)
                        .into(ivFriendAvatar);
            } else {
                ivFriendAvatar.setImageResource(R.drawable.img_20260904_135725);
            }
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirmInvite.setOnClickListener(v -> {
            if (onConfirmListener != null) {
                onConfirmListener.onConfirm(targetUser);
            }
            dismiss();
        });
    }

    @Override
    public void show() {
        Context context = getContext();
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        super.show();
    }
}
