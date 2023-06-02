// Generated by view binder compiler. Do not edit!
package com.example.portableDataTerminal.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.portableDataTerminal.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button aboutBtn;

  @NonNull
  public final Button choosingDocTypeBtn;

  @NonNull
  public final Button infoBtn;

  @NonNull
  public final ConstraintLayout mainLayout;

  @NonNull
  public final Button syncBtn;

  @NonNull
  public final TextView textView;

  private ActivityMainBinding(@NonNull ConstraintLayout rootView, @NonNull Button aboutBtn,
      @NonNull Button choosingDocTypeBtn, @NonNull Button infoBtn,
      @NonNull ConstraintLayout mainLayout, @NonNull Button syncBtn, @NonNull TextView textView) {
    this.rootView = rootView;
    this.aboutBtn = aboutBtn;
    this.choosingDocTypeBtn = choosingDocTypeBtn;
    this.infoBtn = infoBtn;
    this.mainLayout = mainLayout;
    this.syncBtn = syncBtn;
    this.textView = textView;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.about_btn;
      Button aboutBtn = ViewBindings.findChildViewById(rootView, id);
      if (aboutBtn == null) {
        break missingId;
      }

      id = R.id.choosing_doc_type_btn;
      Button choosingDocTypeBtn = ViewBindings.findChildViewById(rootView, id);
      if (choosingDocTypeBtn == null) {
        break missingId;
      }

      id = R.id.info_btn;
      Button infoBtn = ViewBindings.findChildViewById(rootView, id);
      if (infoBtn == null) {
        break missingId;
      }

      ConstraintLayout mainLayout = (ConstraintLayout) rootView;

      id = R.id.sync_btn;
      Button syncBtn = ViewBindings.findChildViewById(rootView, id);
      if (syncBtn == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      return new ActivityMainBinding((ConstraintLayout) rootView, aboutBtn, choosingDocTypeBtn,
          infoBtn, mainLayout, syncBtn, textView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
