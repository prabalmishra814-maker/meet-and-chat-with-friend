package com.roomchatapps.Pmishra.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.roomchatapps.Pmishra.R;

public class CpInsufficientGuardDialog extends Dialog {

    public interface OnProtectClickListener {
        void onProtectClick();
    }

    private OnProtectClickListener listener;

    private final com.roomchatapps.Pmishra.models.User targetUser;

    public CpInsufficientGuardDialog(@NonNull Context context, com.roomchatapps.Pmishra.models.User targetUser) {
        super(context);
        this.targetUser = targetUser;
    }

    public CpInsufficientGuardDialog(@NonNull Context context) {
        this(context, null);
    }

    public void setOnProtectClickListener(OnProtectClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_cp_insufficient_guard);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        TextView btnProtect = findViewById(R.id.btnProtect);
        if (btnProtect != null) {
            btnProtect.setOnClickListener(v -> {
                Context context = getContext();
                if (context != null) {
                    android.content.Intent intent = new android.content.Intent(context, com.roomchatapps.Pmishra.GuardRankActivity.class);
                    if (targetUser != null && !android.text.TextUtils.isEmpty(targetUser.getUserName())) {
                        intent.putExtra("userName", targetUser.getUserName());
                    }
                    context.startActivity(intent);
                }
                if (listener != null) {
                    listener.onProtectClick();
                }
                dismiss();
            });
        }
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
