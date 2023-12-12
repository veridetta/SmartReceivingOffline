// Generated by view binder compiler. Do not edit!
package com.vr.smartreceivingnew.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.vr.smartreceivingnew.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemUserBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final LinearLayout btnHapus;

  @NonNull
  public final LinearLayout btnUbah;

  @NonNull
  public final LinearLayout lyiconEdit;

  @NonNull
  public final TextView tvNama;

  @NonNull
  public final TextView tvNoHp;

  @NonNull
  public final TextView tvPassword;

  @NonNull
  public final TextView tvUsername;

  private ItemUserBinding(@NonNull CardView rootView, @NonNull LinearLayout btnHapus,
      @NonNull LinearLayout btnUbah, @NonNull LinearLayout lyiconEdit, @NonNull TextView tvNama,
      @NonNull TextView tvNoHp, @NonNull TextView tvPassword, @NonNull TextView tvUsername) {
    this.rootView = rootView;
    this.btnHapus = btnHapus;
    this.btnUbah = btnUbah;
    this.lyiconEdit = lyiconEdit;
    this.tvNama = tvNama;
    this.tvNoHp = tvNoHp;
    this.tvPassword = tvPassword;
    this.tvUsername = tvUsername;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemUserBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemUserBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_user, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemUserBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnHapus;
      LinearLayout btnHapus = ViewBindings.findChildViewById(rootView, id);
      if (btnHapus == null) {
        break missingId;
      }

      id = R.id.btnUbah;
      LinearLayout btnUbah = ViewBindings.findChildViewById(rootView, id);
      if (btnUbah == null) {
        break missingId;
      }

      id = R.id.lyiconEdit;
      LinearLayout lyiconEdit = ViewBindings.findChildViewById(rootView, id);
      if (lyiconEdit == null) {
        break missingId;
      }

      id = R.id.tvNama;
      TextView tvNama = ViewBindings.findChildViewById(rootView, id);
      if (tvNama == null) {
        break missingId;
      }

      id = R.id.tvNoHp;
      TextView tvNoHp = ViewBindings.findChildViewById(rootView, id);
      if (tvNoHp == null) {
        break missingId;
      }

      id = R.id.tvPassword;
      TextView tvPassword = ViewBindings.findChildViewById(rootView, id);
      if (tvPassword == null) {
        break missingId;
      }

      id = R.id.tvUsername;
      TextView tvUsername = ViewBindings.findChildViewById(rootView, id);
      if (tvUsername == null) {
        break missingId;
      }

      return new ItemUserBinding((CardView) rootView, btnHapus, btnUbah, lyiconEdit, tvNama, tvNoHp,
          tvPassword, tvUsername);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}