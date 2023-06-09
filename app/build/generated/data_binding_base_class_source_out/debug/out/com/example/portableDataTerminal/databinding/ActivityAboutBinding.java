// Generated by view binder compiler. Do not edit!
package com.example.portableDataTerminal.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public final class ActivityAboutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView authorTextView;

  @NonNull
  public final TextView emailTextView;

  @NonNull
  public final ConstraintLayout mainLayout;

  @NonNull
  public final TextView textView;

  @NonNull
  public final TextView versionTextView;

  private ActivityAboutBinding(@NonNull ConstraintLayout rootView, @NonNull TextView authorTextView,
      @NonNull TextView emailTextView, @NonNull ConstraintLayout mainLayout,
      @NonNull TextView textView, @NonNull TextView versionTextView) {
    this.rootView = rootView;
    this.authorTextView = authorTextView;
    this.emailTextView = emailTextView;
    this.mainLayout = mainLayout;
    this.textView = textView;
    this.versionTextView = versionTextView;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAboutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAboutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_about, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAboutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.author_textView;
      TextView authorTextView = ViewBindings.findChildViewById(rootView, id);
      if (authorTextView == null) {
        break missingId;
      }

      id = R.id.email_textView;
      TextView emailTextView = ViewBindings.findChildViewById(rootView, id);
      if (emailTextView == null) {
        break missingId;
      }

      ConstraintLayout mainLayout = (ConstraintLayout) rootView;

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      id = R.id.version_textView;
      TextView versionTextView = ViewBindings.findChildViewById(rootView, id);
      if (versionTextView == null) {
        break missingId;
      }

      return new ActivityAboutBinding((ConstraintLayout) rootView, authorTextView, emailTextView,
          mainLayout, textView, versionTextView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
