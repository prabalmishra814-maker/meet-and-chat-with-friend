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

public class CpInviteSentDialog extends Dialog {

    public CpInviteSentDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_cp_invite_sent_success);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        TextView btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> dismiss());
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
