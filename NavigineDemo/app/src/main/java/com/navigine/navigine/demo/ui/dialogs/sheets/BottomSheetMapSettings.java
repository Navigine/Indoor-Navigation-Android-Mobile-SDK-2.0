package com.navigine.navigine.demo.ui.dialogs.sheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.navigine.navigine.demo.R;

public class BottomSheetMapSettings extends BottomSheetDialogFragment {

    public interface OnSettingsChangedListener {
        void onRotateChanged(boolean enabled);

        void onTiltChanged(boolean enabled);

        void onScrollChanged(boolean enabled);

        void onZoomChanged(boolean enabled);

        void onStickToBorderChanged(boolean enabled);

        void on3dChanged(boolean enabled);
    }

    private static final String ARG_ROTATE = "rotate";
    private static final String ARG_TILT = "tilt";
    private static final String ARG_SCROLL = "scroll";
    private static final String ARG_ZOOM = "zoom";
    private static final String ARG_STICK = "stick";
    private static final String ARG_3D = "3d";

    private OnSettingsChangedListener mListener;

    private SwitchMaterial mTiltSwitch;
    private SwitchMaterial m3dSwitch;

    public static BottomSheetMapSettings newInstance(
            boolean rotate,
            boolean tilt,
            boolean scroll,
            boolean zoom,
            boolean stick,
            boolean is3d,
            OnSettingsChangedListener listener
    ) {
        BottomSheetMapSettings sheet = new BottomSheetMapSettings();
        sheet.mListener = listener;
        Bundle args = new Bundle();
        args.putBoolean(ARG_ROTATE, rotate);
        args.putBoolean(ARG_TILT, tilt);
        args.putBoolean(ARG_SCROLL, scroll);
        args.putBoolean(ARG_ZOOM, zoom);
        args.putBoolean(ARG_STICK, stick);
        args.putBoolean(ARG_3D, is3d);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_map_settings, container, false);

        Bundle args = requireArguments();

        bindSwitch(view, R.id.map_settings__rotate_switch_item, getString(R.string.map_settings_rotate), args.getBoolean(ARG_ROTATE), mListener::onRotateChanged);
        mTiltSwitch = bindSwitch(view, R.id.map_settings__tilt_switch_item, getString(R.string.map_settings_tilt), args.getBoolean(ARG_TILT), enabled -> {
            mListener.onTiltChanged(enabled);
            if (!enabled && m3dSwitch.isChecked()) {
                m3dSwitch.setChecked(false);
            }
        });
        bindSwitch(view, R.id.map_settings__scroll_switch_item, getString(R.string.map_settings_scroll), args.getBoolean(ARG_SCROLL), mListener::onScrollChanged);
        bindSwitch(view, R.id.map_settings__zoom_switch_item, getString(R.string.map_settings_zoom), args.getBoolean(ARG_ZOOM), mListener::onZoomChanged);
        bindSwitch(view, R.id.map_settings__stick_switch_item, getString(R.string.map_settings_stick), args.getBoolean(ARG_STICK), mListener::onStickToBorderChanged);
        m3dSwitch = bindSwitch(view, R.id.map_settings__3d_switch_item, getString(R.string.map_settings_3d), args.getBoolean(ARG_3D), enabled -> {
            if (enabled && !mTiltSwitch.isChecked()) {
                mTiltSwitch.setChecked(true);
            }
            mListener.on3dChanged(enabled);
        });        return view;
    }

    private SwitchMaterial bindSwitch(View root, int containerId, String label, boolean initialValue, Consumer<Boolean> onChange) {
        View container = root.findViewById(containerId);
        ((MaterialTextView) container.findViewById(R.id.map_settings__switch_label)).setText(label);
        SwitchMaterial sw = container.findViewById(R.id.map_settings__switch);
        sw.setChecked(initialValue);
        sw.setOnCheckedChangeListener((btn, isChecked) -> onChange.accept(isChecked));
        return sw;
    }

}
