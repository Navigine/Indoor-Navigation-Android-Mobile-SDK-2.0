package com.navigine.navigine.demo.ui.dialogs.sheets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.navigine.navigine.demo.R;

public class BottomSheetRouteFinish extends BottomSheetDialogFragment {

    private MaterialButton mButton = null;

    private Runnable onFinishAction = null;

    private boolean isAlreadyShown = false;

    private BottomSheetRouteFinish() {
    }

    public BottomSheetRouteFinish(Runnable onFinishAction) {
        this.onFinishAction = onFinishAction;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_bottom_route_finish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        setViewsListeners();
    }

    private void initViews(View view) {
        mButton = view.findViewById(R.id.sheet_bottom_route_finish_button);
    }

    private void setViewsListeners() {
        if (mButton != null) mButton.setOnClickListener(v -> dismiss());

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnShowListener(dialog1 -> isAlreadyShown = true);
            dialog.setOnDismissListener(dialog12 -> {
                if (onFinishAction != null) onFinishAction.run();
                isAlreadyShown = false;
            });
        }
    }

    public boolean isShown() {
        return isAlreadyShown;
    }
}
