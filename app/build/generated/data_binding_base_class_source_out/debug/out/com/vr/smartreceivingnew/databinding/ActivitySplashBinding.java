// Generated by view binder compiler. Do not edit!
package com.vr.smartreceivingnew.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.vr.smartreceivingnew.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySplashBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final TextView loadingText;

  @NonNull
  public final ImageView logoImage;

  @NonNull
  public final ProgressBar progressBar;

  private ActivitySplashBinding(@NonNull RelativeLayout rootView, @NonNull TextView loadingText,
      @NonNull ImageView logoImage, @NonNull ProgressBar progressBar) {
    this.rootView = rootView;
    this.loadingText = loadingText;
    this.logoImage = logoImage;
    this.progressBar = progressBar;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySplashBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySplashBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_splash, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySplashBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.loadingText;
      TextView loadingText = ViewBindings.findChildViewById(rootView, id);
      if (loadingText == null) {
        break missingId;
      }

      id = R.id.logoImage;
      ImageView logoImage = ViewBindings.findChildViewById(rootView, id);
      if (logoImage == null) {
        break missingId;
      }

      id = R.id.progressBar;
      ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
      if (progressBar == null) {
        break missingId;
      }

      return new ActivitySplashBinding((RelativeLayout) rootView, loadingText, logoImage,
          progressBar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
