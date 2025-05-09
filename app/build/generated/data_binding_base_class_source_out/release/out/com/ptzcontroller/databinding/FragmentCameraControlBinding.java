// Generated by view binder compiler. Do not edit!
package com.ptzcontroller.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.ptzcontroller.R;
import com.ptzcontroller.ui.control.JoystickView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentCameraControlBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button btnGotoPreset;

  @NonNull
  public final Button btnPreset1;

  @NonNull
  public final Button btnPreset2;

  @NonNull
  public final Button btnPreset3;

  @NonNull
  public final Button btnPreset4;

  @NonNull
  public final Button btnSavePreset;

  @NonNull
  public final Button btnZoomIn;

  @NonNull
  public final Button btnZoomOut;

  @NonNull
  public final RadioGroup cameraModeGroup;

  @NonNull
  public final TextView cameraModeLabel;

  @NonNull
  public final TextView connectionStatusText;

  @NonNull
  public final RadioButton irModeButton;

  @NonNull
  public final JoystickView joystickView;

  @NonNull
  public final EditText presetNumberInput;

  @NonNull
  public final LinearLayout presetsContainer;

  @NonNull
  public final TextView presetsLabel;

  @NonNull
  public final LinearLayout quickPresetsContainer;

  @NonNull
  public final RadioButton rgbModeButton;

  @NonNull
  public final Switch switchCameraMode;

  @NonNull
  public final TextView tvPanSpeed;

  @NonNull
  public final TextView tvTiltSpeed;

  @NonNull
  public final TextView tvZoomLevel;

  @NonNull
  public final TextView zoomLabel;

  @NonNull
  public final SeekBar zoomSlider;

  private FragmentCameraControlBinding(@NonNull ConstraintLayout rootView,
      @NonNull Button btnGotoPreset, @NonNull Button btnPreset1, @NonNull Button btnPreset2,
      @NonNull Button btnPreset3, @NonNull Button btnPreset4, @NonNull Button btnSavePreset,
      @NonNull Button btnZoomIn, @NonNull Button btnZoomOut, @NonNull RadioGroup cameraModeGroup,
      @NonNull TextView cameraModeLabel, @NonNull TextView connectionStatusText,
      @NonNull RadioButton irModeButton, @NonNull JoystickView joystickView,
      @NonNull EditText presetNumberInput, @NonNull LinearLayout presetsContainer,
      @NonNull TextView presetsLabel, @NonNull LinearLayout quickPresetsContainer,
      @NonNull RadioButton rgbModeButton, @NonNull Switch switchCameraMode,
      @NonNull TextView tvPanSpeed, @NonNull TextView tvTiltSpeed, @NonNull TextView tvZoomLevel,
      @NonNull TextView zoomLabel, @NonNull SeekBar zoomSlider) {
    this.rootView = rootView;
    this.btnGotoPreset = btnGotoPreset;
    this.btnPreset1 = btnPreset1;
    this.btnPreset2 = btnPreset2;
    this.btnPreset3 = btnPreset3;
    this.btnPreset4 = btnPreset4;
    this.btnSavePreset = btnSavePreset;
    this.btnZoomIn = btnZoomIn;
    this.btnZoomOut = btnZoomOut;
    this.cameraModeGroup = cameraModeGroup;
    this.cameraModeLabel = cameraModeLabel;
    this.connectionStatusText = connectionStatusText;
    this.irModeButton = irModeButton;
    this.joystickView = joystickView;
    this.presetNumberInput = presetNumberInput;
    this.presetsContainer = presetsContainer;
    this.presetsLabel = presetsLabel;
    this.quickPresetsContainer = quickPresetsContainer;
    this.rgbModeButton = rgbModeButton;
    this.switchCameraMode = switchCameraMode;
    this.tvPanSpeed = tvPanSpeed;
    this.tvTiltSpeed = tvTiltSpeed;
    this.tvZoomLevel = tvZoomLevel;
    this.zoomLabel = zoomLabel;
    this.zoomSlider = zoomSlider;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentCameraControlBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentCameraControlBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_camera_control, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentCameraControlBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnGotoPreset;
      Button btnGotoPreset = ViewBindings.findChildViewById(rootView, id);
      if (btnGotoPreset == null) {
        break missingId;
      }

      id = R.id.btnPreset1;
      Button btnPreset1 = ViewBindings.findChildViewById(rootView, id);
      if (btnPreset1 == null) {
        break missingId;
      }

      id = R.id.btnPreset2;
      Button btnPreset2 = ViewBindings.findChildViewById(rootView, id);
      if (btnPreset2 == null) {
        break missingId;
      }

      id = R.id.btnPreset3;
      Button btnPreset3 = ViewBindings.findChildViewById(rootView, id);
      if (btnPreset3 == null) {
        break missingId;
      }

      id = R.id.btnPreset4;
      Button btnPreset4 = ViewBindings.findChildViewById(rootView, id);
      if (btnPreset4 == null) {
        break missingId;
      }

      id = R.id.btnSavePreset;
      Button btnSavePreset = ViewBindings.findChildViewById(rootView, id);
      if (btnSavePreset == null) {
        break missingId;
      }

      id = R.id.btnZoomIn;
      Button btnZoomIn = ViewBindings.findChildViewById(rootView, id);
      if (btnZoomIn == null) {
        break missingId;
      }

      id = R.id.btnZoomOut;
      Button btnZoomOut = ViewBindings.findChildViewById(rootView, id);
      if (btnZoomOut == null) {
        break missingId;
      }

      id = R.id.cameraModeGroup;
      RadioGroup cameraModeGroup = ViewBindings.findChildViewById(rootView, id);
      if (cameraModeGroup == null) {
        break missingId;
      }

      id = R.id.cameraModeLabel;
      TextView cameraModeLabel = ViewBindings.findChildViewById(rootView, id);
      if (cameraModeLabel == null) {
        break missingId;
      }

      id = R.id.connectionStatusText;
      TextView connectionStatusText = ViewBindings.findChildViewById(rootView, id);
      if (connectionStatusText == null) {
        break missingId;
      }

      id = R.id.irModeButton;
      RadioButton irModeButton = ViewBindings.findChildViewById(rootView, id);
      if (irModeButton == null) {
        break missingId;
      }

      id = R.id.joystickView;
      JoystickView joystickView = ViewBindings.findChildViewById(rootView, id);
      if (joystickView == null) {
        break missingId;
      }

      id = R.id.presetNumberInput;
      EditText presetNumberInput = ViewBindings.findChildViewById(rootView, id);
      if (presetNumberInput == null) {
        break missingId;
      }

      id = R.id.presetsContainer;
      LinearLayout presetsContainer = ViewBindings.findChildViewById(rootView, id);
      if (presetsContainer == null) {
        break missingId;
      }

      id = R.id.presetsLabel;
      TextView presetsLabel = ViewBindings.findChildViewById(rootView, id);
      if (presetsLabel == null) {
        break missingId;
      }

      id = R.id.quickPresetsContainer;
      LinearLayout quickPresetsContainer = ViewBindings.findChildViewById(rootView, id);
      if (quickPresetsContainer == null) {
        break missingId;
      }

      id = R.id.rgbModeButton;
      RadioButton rgbModeButton = ViewBindings.findChildViewById(rootView, id);
      if (rgbModeButton == null) {
        break missingId;
      }

      id = R.id.switchCameraMode;
      Switch switchCameraMode = ViewBindings.findChildViewById(rootView, id);
      if (switchCameraMode == null) {
        break missingId;
      }

      id = R.id.tvPanSpeed;
      TextView tvPanSpeed = ViewBindings.findChildViewById(rootView, id);
      if (tvPanSpeed == null) {
        break missingId;
      }

      id = R.id.tvTiltSpeed;
      TextView tvTiltSpeed = ViewBindings.findChildViewById(rootView, id);
      if (tvTiltSpeed == null) {
        break missingId;
      }

      id = R.id.tvZoomLevel;
      TextView tvZoomLevel = ViewBindings.findChildViewById(rootView, id);
      if (tvZoomLevel == null) {
        break missingId;
      }

      id = R.id.zoomLabel;
      TextView zoomLabel = ViewBindings.findChildViewById(rootView, id);
      if (zoomLabel == null) {
        break missingId;
      }

      id = R.id.zoomSlider;
      SeekBar zoomSlider = ViewBindings.findChildViewById(rootView, id);
      if (zoomSlider == null) {
        break missingId;
      }

      return new FragmentCameraControlBinding((ConstraintLayout) rootView, btnGotoPreset,
          btnPreset1, btnPreset2, btnPreset3, btnPreset4, btnSavePreset, btnZoomIn, btnZoomOut,
          cameraModeGroup, cameraModeLabel, connectionStatusText, irModeButton, joystickView,
          presetNumberInput, presetsContainer, presetsLabel, quickPresetsContainer, rgbModeButton,
          switchCameraMode, tvPanSpeed, tvTiltSpeed, tvZoomLevel, zoomLabel, zoomSlider);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
