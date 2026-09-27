package com.roomchatapps.Pmishra.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.roomchatapps.Pmishra.R;

public class LoadingDialog {

    private final Context context;
    private Dialog dialog;
    private TextView tvMessage;

    public LoadingDialog(Context context) {
        this.context = context;
        initDialog();
    }

    private void initDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        tvMessage = view.findViewById(R.id.tvLoadingMessage);
    }

    public void show(String message) {
        if (dialog != null && !dialog.isShowing()) {
            if (tvMessage != null && message != null) {
                tvMessage.setText(message);
            }
            try {
                dialog.show();
            } catch (Exception ignored) {}
        }
    }

    public void setMessage(String message) {
        if (tvMessage != null && message != null) {
            tvMessage.setText(message);
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception ignored) {}
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
